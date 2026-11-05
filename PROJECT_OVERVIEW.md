# ERP System — Mô tả chi tiết dự án

## 1. Giới thiệu tổng quan

Hệ thống ERP (Enterprise Resource Planning) quy mô vừa, xây dựng theo kiến trúc **Modular Monolith** — nhiều module
nghiệp vụ độc lập cùng chạy trong 1 Spring Boot application, giao tiếp với nhau qua ranh giới rõ ràng (UUID reference,
Spring Application Events) thay vì gọi thẳng Repository/Entity chéo module.

**Mục tiêu dự án:** mô phỏng đúng quy trình vận hành thực tế của một doanh nghiệp thương mại — quản lý kho, mua hàng,
bán hàng, thông báo real-time và truy vết audit — với chất lượng code đủ để đưa vào CV/portfolio khi ứng tuyển vị trí
Backend Java.

---

## 2. Tech Stack

| Thành phần           | Công nghệ                                                                                                       |
|----------------------|-----------------------------------------------------------------------------------------------------------------|
| Ngôn ngữ             | Java 21                                                                                                         |
| Framework            | Spring Boot 3                                                                                                   |
| Bảo mật              | Spring Security + JWT (permission-based authorization), Bucket4j Rate Limiting                                  |
| Caching & In-Memory  | Redis (Spring Data Redis, Cache-Aside Pattern)                                                                  |
| Truy cập dữ liệu     | Spring Data JPA / Hibernate                                                                                     |
| Database             | PostgreSQL 14                                                                                                   |
| Quản lý migration    | Flyway                                                                                                          |
| Mapping DTO ↔ Entity | MapStruct                                                                                                       |
| Build tool           | Maven / Gradle                                                                                                  |
| Container & DevOps   | Docker, Docker Compose, Vercel (Frontend), Render (Backend)                                                     |
| Kiến trúc            | Layered Architecture (Controller → Service → Repository), Modular Monolith                                      |
| Kỹ thuật nâng cao    | Spring AOP, Spring Application Events, Redis Multi-tier Caching, Optimistic Locking, PostgreSQL SEQUENCE, JSONB |

---

## 3. Kiến trúc tổng thể

                     ┌──────────────────────────┐
                     │   Inventory Module       │
                     │ (Category, Product,      │
                     │  Warehouse, ProductStock)│
                     └───────────┬──────────────┘
                UUID reference   │   UUID reference
              (không FK xuyên module)
                ┌────────────────┴────────────────┐
                ▼                                  ▼
    ┌──────────────────────────┐         ┌───────────────────────┐
    │   Purchase Module        │         │    Sales Module       │
    │ Supplier → PurchaseOrder │         │ Customer → SalesOrder │
    │   → GoodsReceipt         │         │   → Delivery          │
    └───────────┬──────────────┘         └───────────┬───────────┘
                │         publishEvent()             │
                └────────────────┬───────────────────┘
                                 ▼
                ┌──────────────────────────────────────────────┐
                │      Notification Module                     │
                │  (Event-Driven, @TransactionalEventListener) │
                └──────────────────────────────────────────────┘
                                 │
                ┌───────────────────────────────┐
                │        Audit Module           │
                │  (AOP, @Auditable + Hibernate │
                │   Dirty-Checking Snapshot)    │
                └───────────────────────────────┘
                                 │
    ┌────────────────────────────────────────────────────────┐
    │                    Auth Module                         │
    │          (User, Role, Permission, JWT)                 │
    └────────────────────────────────────────────────────────┘
                                 │
    ┌────────────────────────────────────────────────────────┐
    │                 Redis Cache Server                     │
    │   (Caching Read-Heavy Catalog, Rate Limiting State)    │
    └────────────────────────────────────────────────────────┘

**Nguyên tắc kiến trúc xuyên suốt:**

- **Module boundary qua UUID thuần**: mọi tham chiếu cross-module (VD: `PurchaseOrder.warehouseId` trỏ sang Inventory)
  chỉ lưu UUID, không dùng `@ManyToOne`/FK vật lý xuyên module — giữ các module độc lập, dễ tách thành microservice sau
  này nếu cần.
- **Event-Driven cho thông báo**: module nghiệp vụ (Purchase/Sales) publish sự kiện qua `ApplicationEventPublisher`,
  không biết và không phụ thuộc vào Notification Module.
- **AOP cho audit trail**: ghi log tự động qua annotation `@Auditable`, không chèn code audit thủ công vào business
  logic.
- **Caching Layer tập trung**: tích hợp Redis làm bộ nhớ đệm cho các truy vấn Read-heavy (Catalog, Product, Category),
  áp dụng chiến lược Cache Eviction khi mutation xảy ra để đảm bảo dữ liệu nhất quán.
- **Không tự ý thay đổi schema/module khác** nếu không thật sự cần thiết — mọi thay đổi DB đều qua Flyway migration có
  đánh số thứ tự, không sửa lại migration cũ đã áp dụng.

---

## 4. Chi tiết từng Module

### 4.1. Inventory Module (nền tảng)

- Quản lý `Category`, `Product`, `Unit`, `Warehouse`, `ProductStock`, `StockTransaction`.
- **Redis Multi-tier Caching**: áp dụng mô hình **Cache-Aside Pattern** cho `Product` và `Category` bằng Spring
  `@Cacheable`, `@CacheEvict`. Giảm áp lực đọc xuống DB và tối ưu tốc độ phản hồi cho API tra cứu danh mục.
- Cung cấp `StockService.processTransaction()` — điểm vào DUY NHẤT để mọi module khác cập nhật tồn kho (IMPORT/EXPORT),
  không module nào được tự ý sửa `ProductStock`.
- Optimistic + Pessimistic Lock kết hợp cho các thao tác nhạy cảm về tồn kho.

### 4.2. Purchase Module

**Luồng nghiệp vụ:**
`Supplier → PurchaseOrder (+ PurchaseOrderItem) → GoodsReceipt (+ GoodsReceiptItem) → Inventory (IMPORT)`

- **State Machine PurchaseOrder**: `DRAFT → PENDING_APPROVAL → APPROVED → SENT_TO_SUPPLIER → GOODS_RECEIVED → CLOSED`,
  nhánh phụ `REJECTED`/`CANCELLED`. Quản lý bằng `Map<Status, Set<Status>>` tập trung, validate 1 chỗ duy nhất.
- **Goods Receipt**: nhận hàng → QC (PASSED/FAILED) → nếu PASSED, tự động gọi Inventory nhập kho. Nếu gọi Inventory lỗi,
  lưu trạng thái `FAILED` + retry (Scheduled Job mỗi 15 phút) thay vì rollback — vì hàng đã thực sự nằm trong kho vật
  lý.
- **Snapshot Pattern**: `PurchaseOrderItem` lưu trùng `productCode/productName/productUnit` tại thời điểm đặt hàng,
  không phụ thuộc dữ liệu Product hiện tại.
- **Claim-Quantity Pattern**: chống race condition khi tạo nhiều `GoodsReceipt` song song cho cùng 1
  `PurchaseOrderItem` — tính tổng số lượng đã "đăng ký" ở mọi phiếu chưa huỷ/QC_FAILED, không chỉ dựa vào số đã nhập kho
  thành công.
- **Optimistic Locking** (`@Version`) cho `PurchaseOrder`/`GoodsReceipt`, xử lý
  `ObjectOptimisticLockingFailureException` tập trung ở `GlobalExceptionHandler`, trả về `409 Conflict`.
- **Document Number**: sinh qua PostgreSQL SEQUENCE (atomic), không dùng random để tránh trùng lặp dưới tải cao.

### 4.3. Sales Module (mirror Purchase, có điều chỉnh theo bản chất nghiệp vụ)

**Luồng nghiệp vụ:** `Customer → SalesOrder (+ SalesOrderItem) → Delivery (+ DeliveryItem) → Inventory (EXPORT)`

- Đối xứng thiết kế với Purchase nhưng **loại bỏ Quality Check** (hàng xuất kho đã qua kiểm soát chất lượng lúc nhập).
- **2 lớp validate trước khi xuất kho** (khác biệt cốt lõi so với Purchase):
    1. Không vượt số lượng đã bán (claim-quantity, tương tự Purchase).
    2. Không vượt tồn kho thực tế — gọi `StockService.getStockByProduct()` đọc tồn kho trước khi cho phép tạo Delivery,
       tránh tạo phiếu "treo" vô nghĩa nếu retry cũng sẽ luôn thất bại.
- State Machine đổi tên phù hợp ngữ nghĩa bán hàng: `SENT_TO_SUPPLIER → CONFIRMED`, `GOODS_RECEIVED → DELIVERED`.
- `DeliveryStatus` có thêm bước `DELIVERED` (xác nhận thủ công khách đã nhận hàng) tách biệt với `EXPORTED` (đã trừ kho
  tự động).

### 4.4. Notification Module (Event-Driven Architecture)

- CRUD độc lập hoàn toàn — không phụ thuộc Purchase/Sales để hoạt động.
- Purchase/Sales publish các Event POJO (`PurchaseOrderApprovedEvent`, `SalesOrderRejectedEvent`,
  `GoodsReceiptImportFailedEvent`...) sở hữu bởi chính module phát sinh sự kiện.
- `PurchaseEventListener`/`SalesEventListener` (thuộc Notification Module) lắng nghe qua
  `@TransactionalEventListener(phase = AFTER_COMMIT)` — đảm bảo chỉ tạo thông báo khi transaction nghiệp vụ chính đã
  commit thành công, tránh thông báo sai sự thật nếu giao dịch bị rollback.
- Bulk update (`@Modifying` JPQL) cho `markAllAsRead()` — tối ưu hiệu năng khi đánh dấu hàng loạt thông báo, không cần
  load từng Entity.
- Bảo mật kiểu **resource-level authorization**: không dùng `@PreAuthorize` theo permission, mà kiểm tra
  `recipientId == currentUserId` ở tầng Service.
- Tổng chi phí tích hợp vào code cũ: ~26 dòng thay đổi trên 4 file `ServiceImpl` đã có, không đổi business logic.

### 4.5. Audit Module (Aspect-Oriented Programming)

- Ghi log tự động mọi thay đổi dữ liệu qua annotation `@Auditable` gắn lên method Service — không sửa nội dung method
  gốc.
- Kỹ thuật lõi: "mượn" **Hibernate Loaded State** (cấu trúc nội bộ phục vụ Dirty Checking) để lấy `old_value` mà không
  cần thêm 1 câu SELECT riêng.
- Xử lý an toàn Lazy Loading + Circular Reference bằng cách "làm sạch" snapshot: entity lồng nhau chỉ lấy `id`, bỏ qua
  Collection.
- Lưu `old_value`/`new_value` dạng JSONB — linh hoạt cho nhiều loại Entity khác nhau mà không cần thiết kế cột cố định.
- Lỗi ghi Audit không được phép làm rollback nghiệp vụ chính (try-catch nội bộ trong Aspect).

### 4.6. Auth Module

- Permission-based authorization (`hasAuthority('SUPPLIER_CREATE')`...) thay vì role-based thô — cho phép gán permission
  lẻ linh hoạt.
- JWT Authentication Filter tự giải mã token, nạp danh sách quyền vào `SecurityContextHolder`.

---

## 5. Các vấn đề kỹ thuật đã gặp và giải pháp (Bug Fixes / Lessons Learned)

| Vấn đề                                                            | Nguyên nhân                                                                                                                                 | Giải pháp                                                                                                      |
|-------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| Trùng `po_number` dưới tải cao                                    | Dùng `ThreadLocalRandom` sinh số ngẫu nhiên                                                                                                 | Chuyển sang PostgreSQL `SEQUENCE` — atomic ở mức DB engine                                                     |
| Vượt số lượng khi tạo nhiều `GoodsReceipt` song song              | Chỉ kiểm tra `receivedQuantity` (chỉ phản ánh phần đã import thành công), bỏ sót các phiếu đang "treo" chưa xử lý xong                      | Thêm query `sumActiveClaimedQuantity` tính tổng số lượng đang được xử lý ở mọi phiếu chưa huỷ/QC_FAILED        |
| `could not determine data type of parameter $5` (PostgreSQL JDBC) | Tham số `LocalDate` so sánh với cột `TIMESTAMP WITH TIME ZONE` trong JPQL `(:param IS NULL OR ...)`, driver không đủ ngữ cảnh suy luận kiểu | Đổi tham số search sang `LocalDateTime` / `ZonedDateTime` khớp đúng kiểu cột DB                                |
| `chk_po_delivery_date` CHECK constraint fail khi update đơn cũ    | CHECK re-evaluate trên toàn dòng mỗi lần UPDATE, kể cả khi field đó không đổi                                                               | DTO Update bắt buộc `@Future` + client luôn gửi lại ngày hợp lệ                                                |
| Partial Success khi gọi Inventory giữa chừng lỗi                  | Vòng lặp gọi từng item tuần tự, item trước đã thành công nhưng tổng thể vẫn báo FAILED, retry sẽ gọi lại từ đầu gây trùng lặp               | Đã xác định rủi ro, đề xuất giải pháp (cờ đánh dấu từng item / idempotency key), tạm chấp nhận ở giai đoạn đầu |
| Role/Permission mới không tự áp dụng cho DB đã có dữ liệu         | `DataInitializer` chỉ tạo Role khi chưa tồn tại (`findByName().isEmpty()`)                                                                  | Cần Flyway migration riêng để `INSERT` permission vào `role_permissions` cho dữ liệu production đã tồn tại     |

---

## 6. Danh sách API (tổng hợp)

| Module         | Số lượng endpoint | Ghi chú                                     |
|----------------|-------------------|---------------------------------------------|
| Supplier       | 6                 | CRUD + search                               |
| Purchase Order | 15                | CRUD + item management + 6 state-transition |
| Goods Receipt  | 7                 | Create, QC, retry, cancel                   |
| Customer       | 6                 | Mirror Supplier                             |
| Sales Order    | 15                | Mirror Purchase Order                       |
| Delivery       | 8                 | Create, mark-as-delivered, retry, cancel    |
| Notification   | 6                 | Không cần permission, chỉ cần đăng nhập     |
| Audit Log      | 3                 | Chỉ ADMIN                                   |

---

## 7. Bảo mật (Security)

- JWT Bearer Token cho mọi request (trừ `/api/auth/login`).
- Permission chi tiết theo từng hành động (VD: `PURCHASE_CREATE`, `PURCHASE_APPROVE`, `PURCHASE_CANCEL` tách biệt), gán
  vào Role qua bảng `role_permissions`.
- Row-level authorization bổ sung cho dữ liệu cá nhân hoá (Notification) — không thể biểu diễn bằng permission thông
  thường.
- `GlobalExceptionHandler` tập trung xử lý: `ResourceNotFoundException` (404), `DuplicateResourceException` (409),
  `AccessDeniedException` (403), `ObjectOptimisticLockingFailureException` (409), validation errors (400).

---

## 8. Điểm nhấn kỹ thuật dùng cho CV/Phỏng vấn

1. **Redis Caching & Latency Optimization**: Áp dụng Redis Caching giúp giảm thời gian phản hồi (Latency Reduction) lên
   tới **95.5%** đối với các API đọc danh mục sản phẩm/kho hàng, đồng thời thiết lập cơ chế Cache Invalidation tự động
   khi có thao tác ghi/sửa.
2. **Event-Driven Architecture**: Tích hợp hệ thống thông báo real-time vào 2 module nghiệp vụ có sẵn chỉ với ~26 dòng
   thay đổi, không ảnh hưởng business logic hiện tại — minh chứng cụ thể cho lợi ích của decoupling qua Spring
   Application Events.
3. **AOP tự động hoá Audit Trail**: Dùng `@Around` Advice kết hợp khai thác trực tiếp Hibernate Loaded State để lấy
   snapshot dữ liệu trước/sau mà không tốn thêm query SELECT riêng.
4. **Concurrency Control có chủ đích**: Chuyển đổi từ Pessimistic sang Optimistic Locking sau khi cân nhắc đánh đổi thực
   tế, xử lý UX cho người dùng khi xung đột xảy ra (`409 Conflict`).
5. **Domain-Driven thinking**: Nhận diện đúng khác biệt bản chất giữa 2 nghiệp vụ tưởng như đối xứng (Purchase có QC,
   Sales không có QC; Sales cần validate tồn kho thực tế, Purchase thì không).
6. **Database & Cache Infrastructure**: Partial Unique Index, Composite Index, JSONB cho dữ liệu bán cấu trúc,
   PostgreSQL SEQUENCE cho document numbering, tích hợp Redis container qua Docker Compose.

---

## 9. Cấu trúc thư mục dự án

com.ERP_SYSTEM
├── common/            (BaseEntity, exception, response, SequenceRepository)
├── auth/               (User, Role, Permission, JWT, Security Config)
├── inventory/          (Category, Product, Warehouse, ProductStock, StockTransaction)
├── purchase/
│ ├── entity/ (base, enums)
│ ├── dto/ (request, response)
│ ├── mapper/
│ ├── repository/
│ ├── service/ (impl)
│ ├── controller/
│ └── event/
├── sales/               (cấu trúc tương tự purchase)
├── notification/
│ ├── entity/ (enums)
│ ├── dto/
│ ├── mapper/
│ ├── repository/
│ ├── service/ (impl)
│ ├── controller/
│ └── listener/
└── audit/
├── entity/ (enums)
├── annotation/  (@Auditable)
├── aspect/      (AuditAspect)
├── dto/
├── repository/
├── service/ (impl)
└── controller/


---

## 10. Migration History (Flyway)

| Version | Nội dung                                                                           |
|---------|------------------------------------------------------------------------------------|
| V1      | Khởi tạo schema Auth                                                               |
| V2      | Khởi tạo schema Inventory                                                          |
| V3      | Khởi tạo schema Purchase                                                           |
| V4      | Thêm SEQUENCE cho document number (po/gr/pr) + cột `rejection_reason` cho Purchase |
| V5      | Loại bỏ Purchase Requisition (bảng + cột `requisition_id`)                         |
| V6      | Khởi tạo schema Sales                                                              |
| V7      | Thêm `created_by_id` cho PurchaseOrder/SalesOrder + tạo bảng `notification`        |
| V8      | Thêm `update_at` cho notification                                                  |
| V9      | Tạo bảng `audit_log`                                                               |

---

## 11. Tối ưu hiệu năng & Bảo mật nâng cao

- **Multi-tier Caching với Redis**: Áp dụng Caching cho danh mục Product và Category giúp giảm độ trễ truy xuất dữ
  liệu (Latency Reduction) lên tới **95.5%** cho các thao tác Read-heavy.
- **Bucket4j Rate Limiting**: Tích hợp thuật toán Token Bucket ở tầng Controller / Security Filter giúp ngăn chặn hành
  vi brute-force và phòng chống Spam/DDoS API.
- **Soft-Delete Policy**: Thiết lập chính sách xóa mềm (Soft-delete) cho các danh mục Category và Unit để duy trì tính
  vẹn toàn dữ liệu lịch sử (Data Integrity) trên toàn hệ thống.

---

## 12. Triển khai & Vận hành (DevOps & Deployment)

- **Docker Compose Orchestration**: Đóng gói ứng dụng multi-container gồm Spring Boot App, PostgreSQL (
  `postgres:14-alpine`) kèm persistent volumes và Redis instance.
- **Container Health Check**: Cấu hình health checks linh hoạt giữa các containers đảm bảo backend chỉ khởi tạo khi
  Database và Redis Cache sẵn sàng.
- **Live Environments**:
    - **Frontend**: Triển khai trên **Vercel
      ** ([erp-system-zeta-lime.vercel.app](https://erp-system-zeta-lime.vercel.app)).
    - **Backend**: Hosted trên hạ tầng đám mây (Render free-tier) với quy trình CI/CD tự động từ repo
      GitHub ([github.com/le-nguyen1911/ERP_SYSTEM](https://github.com/le-nguyen1911/ERP_SYSTEM)).

---

*Tài liệu này tổng hợp toàn bộ quá trình thiết kế và xây dựng hệ thống, dùng làm tài liệu t