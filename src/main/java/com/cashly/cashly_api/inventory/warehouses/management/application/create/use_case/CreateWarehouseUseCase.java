package com.cashly.cashly_api.inventory.warehouses.management.application.create.use_case;

import com.cashly.cashly_api.inventory.warehouses.management.application.create.request.CreateWarehouseRequest;
import com.cashly.cashly_api.inventory.warehouses.management.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.create.services.DuplicateWarehouseCodeException;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

public class CreateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    public CreateWarehouseUseCase(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public WarehouseDetailResponse execute(CreateWarehouseRequest request) {
        String code = request.code().trim().toUpperCase();
        if (warehouseRepository.existsByCode(code)) {
            throw new DuplicateWarehouseCodeException(code);
        }
        Warehouse warehouse = Warehouse.create(
                code, request.name(), request.address(),
                request.state(), request.municipality(), request.postalCode()
        );
        Warehouse saved = warehouseRepository.save(warehouse);
        return WarehouseDetailResponse.from(saved);
    }
}
