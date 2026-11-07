package com.cashly.cashly_api.inventory.warehouses.management.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.inventory.warehouses.management.application.delete.services.WarehouseHasActiveStockException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteWarehouseUseCase(WarehouseRepository warehouseRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
        if (warehouseRepository.hasActiveStock(id)) {
            throw new WarehouseHasActiveStockException(id);
        }
        warehouse.deactivate();
        activityNotifications.record(ActivityEvent.of("INVENTORY_WAREHOUSES",
                "DELETE", "WAREHOUSE", warehouse.getId(),
                warehouse.getName(), null));
        warehouseRepository.save(warehouse);
    }
}
