package com.ERP_SYSTEM.inventory.service;

import com.ERP_SYSTEM.inventory.dto.request.CreateWarehouseRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateWarehouseRequest;
import com.ERP_SYSTEM.inventory.dto.response.WarehouseResponse;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseResponse create(CreateWarehouseRequest request);

    WarehouseResponse update(UUID id, UpdateWarehouseRequest request);

    WarehouseResponse getById(UUID id);

    List<WarehouseResponse> getAll();

    List<WarehouseResponse> getAllActive();

    void delete(UUID id);
}
