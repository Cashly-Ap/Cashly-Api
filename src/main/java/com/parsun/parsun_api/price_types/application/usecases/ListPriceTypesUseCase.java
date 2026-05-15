package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;

import java.util.List;

public class ListPriceTypesUseCase {

    private final PriceTypeRepository priceTypeRepository;

    public ListPriceTypesUseCase(PriceTypeRepository priceTypeRepository) {
        this.priceTypeRepository = priceTypeRepository;
    }

    public List<PriceTypeResponse> execute(String orderBy, String orderDir) {
        return priceTypeRepository.findAll(orderBy, orderDir).stream()
            .map(PriceTypeResponse::from)
            .toList();
    }
}
