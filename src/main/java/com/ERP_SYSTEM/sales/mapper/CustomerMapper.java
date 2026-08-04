package com.ERP_SYSTEM.sales.mapper;

import com.ERP_SYSTEM.sales.dto.request.CreateCustomerRequest;
import com.ERP_SYSTEM.sales.dto.request.UpdateCustomerRequest;
import com.ERP_SYSTEM.sales.dto.response.CustomerResponse;
import com.ERP_SYSTEM.sales.entity.Customer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {
    CustomerResponse toResponse(Customer customer);

    Customer toEntity(CreateCustomerRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateCustomerRequest request, @MappingTarget Customer customer);
}
