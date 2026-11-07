package com.cashly.cashly_api.catalog.price_types.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.catalog.price_types.application.update.request.UpdatePriceTypeRequest;
import com.cashly.cashly_api.catalog.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.catalog.price_types.domain.entity.PriceType;
import com.cashly.cashly_api.catalog.price_types.domain.repository.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdatePriceTypeUseCase {

    private final PriceTypeRepository priceTypeRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdatePriceTypeUseCase(PriceTypeRepository priceTypeRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.priceTypeRepository = priceTypeRepository;
    }

    @Transactional
    public PriceTypeResponse execute(UUID id, UpdatePriceTypeRequest request) {
        PriceType priceType = priceTypeRepository.findById(id)
                .orElseThrow(() -> new PriceTypeNotFoundException(id));

        if (priceTypeRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicatePriceTypeNameException(request.name());
        }

        priceType.update(request.name(), request.description(),
                request.active());
        PriceType saved = priceTypeRepository.save(priceType);
        activityNotifications.record(ActivityEvent.of("CATALOG_PRICES",
                "EDIT", "PRICE_TYPE", saved.getId(),
                saved.getName(), null));
        return PriceTypeResponse.from(saved);
    }
}
