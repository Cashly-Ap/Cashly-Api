package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.request.CreatePriceTypeRequest;
import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;

public class CreatePriceTypeUseCase {

    private final PriceTypeRepository priceTypeRepository;

    public CreatePriceTypeUseCase(PriceTypeRepository priceTypeRepository) {
        this.priceTypeRepository = priceTypeRepository;
    }

    @Transactional
    public PriceTypeResponse execute(CreatePriceTypeRequest request) {
        if (priceTypeRepository.existsByName(request.name())) {
            throw new DuplicatePriceTypeNameException(request.name());
        }
        PriceType priceType = PriceType.create(request.name(), request.description());
        PriceType saved = priceTypeRepository.save(priceType);
        return PriceTypeResponse.from(saved);
    }
}
