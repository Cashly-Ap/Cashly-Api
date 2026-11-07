package com.cashly.cashly_api.inventory.warehouses.management.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.inventory.warehouses.management.application.update.request.UpdateWarehouseRequest;
import com.cashly.cashly_api.inventory.warehouses.management.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.create.services.DuplicateWarehouseCodeException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateWarehouseUseCase(WarehouseRepository warehouseRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public WarehouseDetailResponse execute(UUID id,
            UpdateWarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));

        warehouse.update(request.name(), request.address(), request.state(),
                request.municipality(), request.postalCode(), request.active());

        Warehouse saved = warehouseRepository.save(warehouse);
        activityNotifications.record(ActivityEvent.of("INVENTORY_WAREHOUSES",
                "EDIT", "WAREHOUSE", saved.getId(),
                saved.getName(), null));
        return WarehouseDetailResponse.from(saved);
    }
}
