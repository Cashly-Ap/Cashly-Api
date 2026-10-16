package com.cashly.cashly_api.warehouses.management.application.create;

import com.cashly.cashly_api.warehouses.management.application.create.CreateWarehouseRequest;
import com.cashly.cashly_api.warehouses.management.application.dto.WarehouseDetailResponse;
import com.cashly.cashly_api.warehouses.management.application.create.DuplicateWarehouseCodeException;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
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
