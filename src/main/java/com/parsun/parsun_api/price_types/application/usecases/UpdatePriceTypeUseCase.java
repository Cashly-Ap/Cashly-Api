package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.request.UpdatePriceTypeRequest;
import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.parsun.parsun_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdatePriceTypeUseCase {

    private final PriceTypeRepository priceTypeRepository;

    public UpdatePriceTypeUseCase(PriceTypeRepository priceTypeRepository) {
        this.priceTypeRepository = priceTypeRepository;
    }

    @Transactional
    public PriceTypeResponse execute(UUID id, UpdatePriceTypeRequest request) {
        PriceType priceType = priceTypeRepository.findById(id)
            .orElseThrow(() -> new PriceTypeNotFoundException(id));

        if (priceTypeRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicatePriceTypeNameException(request.name());
        }

        priceType.update(request.name(), request.description(), request.active());
        PriceType saved = priceTypeRepository.save(priceType);
        return PriceTypeResponse.from(saved);
    }
}
