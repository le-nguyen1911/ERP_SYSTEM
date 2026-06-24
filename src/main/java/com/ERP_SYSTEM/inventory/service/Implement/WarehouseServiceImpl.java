package com.ERP_SYSTEM.inventory.service.Implement;

import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.inventory.dto.request.CreateWarehouseRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateWarehouseRequest;
import com.ERP_SYSTEM.inventory.dto.response.WarehouseResponse;
import com.ERP_SYSTEM.inventory.entity.Warehouse;
import com.ERP_SYSTEM.inventory.mapper.StockMapper;
import com.ERP_SYSTEM.inventory.repository.ProductStockRepository;
import com.ERP_SYSTEM.inventory.repository.WarehouseRepository;
import com.ERP_SYSTEM.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StockMapper stockMapper;

    private final ProductStockRepository productStockRepository;

    @Override
    @Transactional
    public WarehouseResponse create(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Kho" + request.name() + "đã tồn tại");
        }
        Warehouse warehouse = Warehouse.builder()
                .name(request.name())
                .location(request.location())
                .description(request.description())
                .active(true)
                .build();

        warehouseRepository.save(warehouse);
        return stockMapper.toWarehouseResponse(warehouse);
    }

    @Override
    @Transactional
    public WarehouseResponse update(UUID id, UpdateWarehouseRequest request) {
        Warehouse warehouse = findByWarehouseId(id);
        if (!warehouse.getName().equalsIgnoreCase(request.name())
                && warehouseRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Kho đã tồn tại");
        }
        warehouse.setName(request.name());
        warehouse.setLocation(request.location());
        warehouse.setDescription(request.description());
        warehouse.setActive(request.active());
        warehouseRepository.save(warehouse);
        return stockMapper.toWarehouseResponse(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(UUID id) {
        return stockMapper.toWarehouseResponse(findByWarehouseId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll()
                .stream()
                .map(stockMapper::toWarehouseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllActive() {
        return warehouseRepository.findByActiveTrue()
                .stream()
                .map(stockMapper::toWarehouseResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void delete(UUID id) {
        Warehouse warehouse = findByWarehouseId(id);

        boolean hasStock = productStockRepository
                .existsByWarehouseIdAndQuantityGreaterThan(id, 0);

        if (hasStock) {
            throw new RuntimeException(
                    "Không thể vô hiệu hóa kho đang còn hàng tồn"
            );
        }

        warehouse.setActive(false);
    }

    private Warehouse findByWarehouseId(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho"));
    }
}
