package com.cashly.cashly_api.inventory.warehouses.management.application.create.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.inventory.warehouses.management.application.create.request.CreateWarehouseRequest;
import com.cashly.cashly_api.inventory.warehouses.management.application.dto.response.WarehouseDetailResponse;
import com.cashly.cashly_api.inventory.warehouses.management.application.create.services.DuplicateWarehouseCodeException;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class CreateWarehouseUseCase {

    private final WarehouseRepository warehouseRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public CreateWarehouseUseCase(WarehouseRepository warehouseRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    @Audited(module = "inventory", entityType = "WAREHOUSE", action = "CREATE")
    public WarehouseDetailResponse execute(CreateWarehouseRequest request) {
        String code = request.code().trim().toUpperCase();
        if (warehouseRepository.existsByCode(code)) {
            throw new DuplicateWarehouseCodeException(code);
        }
        Warehouse warehouse = Warehouse.create(code, request.name(),
                request.address(), request.state(), request.municipality(),
                request.postalCode());
        Warehouse saved = warehouseRepository.save(warehouse);
        activityNotifications.record(ActivityEvent.of("INVENTORY_WAREHOUSES",
                "CREATE", "WAREHOUSE", saved.getId(),
                saved.getName(), null));
        return WarehouseDetailResponse.from(saved);
    }
}
