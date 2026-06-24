package com.ERP_SYSTEM.inventory.service;

import com.ERP_SYSTEM.inventory.dto.request.CreateUnitRequest;
import com.ERP_SYSTEM.inventory.dto.response.UnitResponse;

import java.util.List;
import java.util.UUID;

public interface UnitService {

    UnitResponse create(CreateUnitRequest request);

    UnitResponse update(UUID id, CreateUnitRequest request);

    UnitResponse getById(UUID id);

    List<UnitResponse> getAll();

    void delete(UUID id);

}
