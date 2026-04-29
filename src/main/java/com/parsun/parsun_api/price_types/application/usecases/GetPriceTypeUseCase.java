package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;

import java.util.UUID;

public class GetPriceTypeUseCase {

    private final PriceTypeRepository priceTypeRepository;

    public GetPriceTypeUseCase(PriceTypeRepository priceTypeRepository) {
        this.priceTypeRepository = priceTypeRepository;
    }

    public PriceTypeResponse execute(UUID id) {
        return priceTypeRepository.findById(id)
            .map(PriceTypeResponse::from)
            .orElseThrow(() -> new PriceTypeNotFoundException(id));
    }
}
