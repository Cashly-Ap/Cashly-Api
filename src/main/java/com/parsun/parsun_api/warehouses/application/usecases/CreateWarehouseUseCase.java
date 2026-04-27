package com.parsun.parsun_api.warehouses.application.usecases;

import com.parsun.parsun_api.warehouses.application.dto.request.CreateWarehouseRequest;
import com.parsun.parsun_api.warehouses.application.dto.response.WarehouseDetailResponse;
import com.parsun.parsun_api.warehouses.application.exceptions.DuplicateWarehouseCodeException;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
