package com.cashly.cashly_api.catalog.price_types.application.create.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.price_types.application.create.request.CreatePriceTypeRequest;
import com.cashly.cashly_api.catalog.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.cashly.cashly_api.catalog.price_types.domain.entity.PriceType;
import com.cashly.cashly_api.catalog.price_types.domain.repository.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class CreatePriceTypeUseCase {

    private final PriceTypeRepository priceTypeRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public CreatePriceTypeUseCase(PriceTypeRepository priceTypeRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.priceTypeRepository = priceTypeRepository;
    }

    @Transactional
    @Audited(module = "catalog", entityType = "PRICE_TYPE", action = "CREATE")
    public PriceTypeResponse execute(CreatePriceTypeRequest request) {
        if (priceTypeRepository.existsByName(request.name())) {
            throw new DuplicatePriceTypeNameException(request.name());
        }
        PriceType priceType = PriceType.create(request.name(),
                request.description());
        PriceType saved = priceTypeRepository.save(priceType);
        activityNotifications.record(ActivityEvent.of("CATALOG_PRICES",
                "CREATE", "PRICE_TYPE", saved.getId(),
                saved.getName(), null));
        return PriceTypeResponse.from(saved);
    }
}
