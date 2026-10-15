package com.ERP_SYSTEM.inventory.service.Implement;

import com.ERP_SYSTEM.audit.annotation.Auditable;
import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.common.cache.CacheNames;
import com.ERP_SYSTEM.common.exception.BadRequestException; // Đã sửa import đúng package
import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.inventory.dto.request.CreateProductRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateProductRequest;
import com.ERP_SYSTEM.inventory.dto.response.ProductResponse;
import com.ERP_SYSTEM.inventory.entity.Category;
import com.ERP_SYSTEM.inventory.entity.Product;
import com.ERP_SYSTEM.inventory.entity.Unit;
import com.ERP_SYSTEM.inventory.mapper.ProductMapper;
import com.ERP_SYSTEM.inventory.repository.CategoryRepository;
import com.ERP_SYSTEM.inventory.repository.ProductRepository;
import com.ERP_SYSTEM.inventory.repository.UnitRepository;
import com.ERP_SYSTEM.inventory.service.ProductService;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;

    @Auditable(
            entityClass = Product.class,
            entityType = "Product",
            action = AuditAction.CREATE,
            module = SourceModule.INVENTORY,
            idExpression = "#result.id"
    )
    @CacheEvict(value = CacheNames.PRODUCTS, allEntries = true)
    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String code = request.code().trim().toUpperCase();
        if (productRepository.findByCode(code).isPresent()) {
            throw new DuplicateResourceException("Mã sản phẩm '" + code + "' đã tồn tại");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không hợp lệ"));

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vị tính không hợp lệ"));

        Product product = Product.builder()
                .code(code)
                .name(request.name().trim())
                .description(request.description() != null ? request.description().trim() : null)
                .price(request.price())
                .category(category)
                .unit(unit)
                .active(true)
                .build();

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Auditable(
            entityClass = Product.class,
            entityType = "Product",
            action = AuditAction.UPDATE,
            module = SourceModule.INVENTORY
    )
    @CacheEvict(value = CacheNames.PRODUCTS, allEntries = true)
    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = findProductById(id);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }

        if (request.unitId() != null) {
            Unit unit = unitRepository.findById(request.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn vị tính"));
            product.setUnit(unit);
        }

        if (request.active() != null) {
            product.setActive(request.active());
        }

        productMapper.updateProductFromRequest(request, product);
        productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    @Cacheable(value = CacheNames.PRODUCTS, key = "#id")
    @Override
    public ProductResponse getById(UUID id) {
        return productMapper.toProductResponse(findProductById(id));
    }

    @Cacheable(value = CacheNames.PRODUCTS, key = "'code:' + #code.trim().toUpperCase()")
    @Override
    public ProductResponse getByCode(String code) {
        Product product = productRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm với mã '" + code + "' không tồn tại"));
        return productMapper.toProductResponse(product);
    }

    @Cacheable(
            value = CacheNames.PRODUCTS,
            key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort"
    )
    @Override
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductResponse);
    }

    @Cacheable(
            value = CacheNames.PRODUCTS,
            key = "'active:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort"
    )
    @Override
    public Page<ProductResponse> getByActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(productMapper::toProductResponse);
    }

    @Override
    public Page<ProductResponse> search(String search, Pageable pageable) {
        return productRepository.searchProducts(search, pageable).map(productMapper::toProductResponse);
    }

    @Auditable(
            entityClass = Product.class,
            entityType = "Product",
            action = AuditAction.STATUS_CHANGE,
            module = SourceModule.INVENTORY
    )
    @CacheEvict(value = CacheNames.PRODUCTS, allEntries = true)
    @Override
    @Transactional
    public ProductResponse deactivate(UUID id) {
        Product product = findProductById(id);
        if (!product.getActive()) {
            throw new BadRequestException("Sản phẩm '" + product.getName() + "' đã ở trạng thái ngừng kinh doanh");
        }
        product.setActive(false);
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @Auditable(
            entityClass = Product.class,
            entityType = "Product",
            action = AuditAction.STATUS_CHANGE,
            module = SourceModule.INVENTORY
    )
    @CacheEvict(value = CacheNames.PRODUCTS, allEntries = true)
    @Override
    @Transactional
    public ProductResponse activate(UUID id) {
        Product product = findProductById(id);
        if (product.getActive()) {
            throw new BadRequestException("Sản phẩm '" + product.getName() + "' đã ở trạng thái đang kinh doanh"); // Sửa thành BadRequestException
        }
        product.setActive(true);
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }
}