package com.cashly.cashly_api.price_types.application.queries;

import com.cashly.cashly_api.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.price_types.domain.PriceTypeRepository;

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
