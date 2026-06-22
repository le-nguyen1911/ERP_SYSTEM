package com.ERP_SYSTEM.inventory.mapper;

import com.ERP_SYSTEM.inventory.dto.request.UpdateProductRequest;
import com.ERP_SYSTEM.inventory.dto.response.CategoryResponse;
import com.ERP_SYSTEM.inventory.dto.response.ProductResponse;
import com.ERP_SYSTEM.inventory.dto.response.UnitResponse;
import com.ERP_SYSTEM.inventory.entity.Category;
import com.ERP_SYSTEM.inventory.entity.Product;
import com.ERP_SYSTEM.inventory.entity.Unit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {
    CategoryResponse toCategoryResponse(Category category);

    UnitResponse toUnitResponse(Unit unit);

    ProductResponse toProductResponse(Product product);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE

    )
    void updateProductFromRequest(
            UpdateProductRequest request, @MappingTarget Product product
    );
}
