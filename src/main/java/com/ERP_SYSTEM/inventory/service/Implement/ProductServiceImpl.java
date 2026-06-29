package com.ERP_SYSTEM.inventory.service.Implement;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse create(CreateProductRequest request) {
        String code = request.code().trim().toUpperCase();
        if (productRepository.findByCode(code).isPresent()) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại");
        }
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục Không hợp lệ"));
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn vị tính không hợp lệ"));
        Product product = Product.builder()
                .code(code)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(category)
                .unit(unit)
                .active(true)
                .build();
        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = findProductById(id);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Khong tìm thấy danh mục"));
            product.setCategory(category);
        }
        if (request.unitId() != null) {
            Unit unit = unitRepository.findById(request.unitId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị"));
            product.setUnit(unit);
        }
        productMapper.updateProductFromRequest(request, product);
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        return productMapper.toProductResponse(findProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getByCode(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getByActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(productMapper::toProductResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String search, Pageable pageable) {
        return productRepository.searchProducts(search, pageable).map(productMapper::toProductResponse);
    }

    @Override
    @Transactional
    public ProductResponse deactivate(UUID id) {
        Product product = findProductById(id);
        if (!product.getActive()) {
            throw new RuntimeException("Sản phẩm" + product.getName() + "Đang ở trạng thái ngừng kinh doanh");
        }
        product.setActive(false);
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm tháy sản phẩm"));
    }
}
