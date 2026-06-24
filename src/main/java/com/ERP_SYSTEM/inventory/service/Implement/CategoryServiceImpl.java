package com.ERP_SYSTEM.inventory.service.Implement;

import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.inventory.dto.request.CreateCategoryRequest;
import com.ERP_SYSTEM.inventory.dto.response.CategoryResponse;
import com.ERP_SYSTEM.inventory.entity.Category;
import com.ERP_SYSTEM.inventory.mapper.ProductMapper;
import com.ERP_SYSTEM.inventory.repository.CategoryRepository;
import com.ERP_SYSTEM.inventory.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Tên danh mục  " + request.name() + " đã tồn tại");
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        categoryRepository.save(category);
        return productMapper.toCategoryResponse(category);
    }


    @Override
    @Transactional
    public CategoryResponse update(UUID id, CreateCategoryRequest request) {
        Category category = findCategoryById(id);
        if (!category.getName().equalsIgnoreCase(request.name())
                && categoryRepository.existsByName(request.name().trim())
        ) {
            throw new DuplicateResourceException("Tên danh mục  " + request.name() + " đã tồn tại");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        categoryRepository.save(category);
        return productMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        return productMapper.toCategoryResponse(findCategoryById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(productMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Category category = findCategoryById(id);
        if (!category.getProducts().isEmpty()) {
            throw new RuntimeException("Không thể xoá Danh mục đang có"
                    + category.getProducts().size()
                    + "sản phẩm. Vui lòng xoá hoặc chuyển sản phẩm sang danh mục mới");
        }
        categoryRepository.delete(category);
    }

    private Category findCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
