package com.cashly.cashly_api.catalog.price_types.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeInUseException;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.catalog.price_types.application.ports.out.PriceTypeUsageGateway;
import com.cashly.cashly_api.catalog.price_types.domain.repository.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class DeletePriceTypeUseCase {

    private final PriceTypeUsageGateway usageGateway;
    private final PriceTypeRepository priceTypeRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeletePriceTypeUseCase(PriceTypeUsageGateway usageGateway,
            PriceTypeRepository priceTypeRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.usageGateway = usageGateway;
        this.priceTypeRepository = priceTypeRepository;
    }

    @Transactional
    @Audited(module = "catalog", entityType = "PRICE_TYPE", action = "DELETE")
    public void execute(UUID id) {
        if (!priceTypeRepository.existsById(id)) {
            throw new PriceTypeNotFoundException(id);
        }
        if (usageGateway.isInUseByProducts(id)) {
            throw new PriceTypeInUseException(id);
        }
        String priceTypeName = priceTypeRepository.findById(id)
                .map(priceType -> priceType.getName()).orElse(id.toString());
        activityNotifications.record(ActivityEvent.of("CATALOG_PRICES",
                "DELETE", "PRICE_TYPE", id, priceTypeName, null));
        priceTypeRepository.delete(id);
    }
}
