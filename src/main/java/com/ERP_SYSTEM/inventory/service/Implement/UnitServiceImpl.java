package com.ERP_SYSTEM.inventory.service.Implement;

import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.inventory.dto.request.CreateUnitRequest;
import com.ERP_SYSTEM.inventory.dto.response.UnitResponse;
import com.ERP_SYSTEM.inventory.entity.Unit;
import com.ERP_SYSTEM.inventory.mapper.ProductMapper;
import com.ERP_SYSTEM.inventory.repository.UnitRepository;
import com.ERP_SYSTEM.inventory.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;


    @Override
    @Transactional
    public UnitResponse create(CreateUnitRequest request) {
        if (unitRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Đơn vị tính" + request.name() + "đã tồn tại");
        }
        Unit unit = Unit.builder()
                .name(request.name())
                .description(request.description())
                .build();
        unitRepository.save(unit);
        return productMapper.toUnitResponse(unit);
    }

    @Override
    @Transactional
    public UnitResponse update(UUID id, CreateUnitRequest request) {
        Unit unit = findUnitById(id);

        if (!unit.getName().equalsIgnoreCase(request.name())
                && unitRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Tên đơn vị tính đã tồn tại: " + request.name());
        }
        unit.setName(request.name());
        unit.setDescription(request.description());
        unitRepository.save(unit);
        return productMapper.toUnitResponse(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse getById(UUID id) {
        return productMapper.toUnitResponse(findUnitById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getAll() {
        return unitRepository.findAll()
                .stream()
                .map(productMapper::toUnitResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Unit unit = findUnitById(id);
        boolean hasProducts = unitRepository.existsProductByUnitId(id);
        if (!unit.getName().isEmpty()) {
            throw new RuntimeException("Đơn vị không được tìm thấy");
        }
        if (hasProducts) {
            throw new RuntimeException(
                    "Không thể xóa đơn vị tính đang được "
                            + "sử dụng bởi sản phẩm."
            );
        }

        unitRepository.delete(unit);

    }

    private Unit findUnitById(UUID id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
    }
}
