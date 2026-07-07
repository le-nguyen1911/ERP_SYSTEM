package com.ERP_SYSTEM.purchase.mapper;

import com.ERP_SYSTEM.purchase.dto.request.CreateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.request.UpdateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.response.SupplierResponse;
import com.ERP_SYSTEM.purchase.entity.Supplier;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SupplierMapper {
    SupplierResponse toResponse(Supplier supplier);

    Supplier toEntity(CreateSupplierRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateSupplierRequest request, @MappingTarget Supplier supplier);
}
