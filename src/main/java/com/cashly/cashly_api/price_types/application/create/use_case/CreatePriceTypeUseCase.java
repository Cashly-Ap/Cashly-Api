package com.cashly.cashly_api.price_types.application.create.use_case;

import com.cashly.cashly_api.price_types.application.create.request.CreatePriceTypeRequest;
import com.cashly.cashly_api.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.cashly.cashly_api.price_types.domain.entity.PriceType;
import com.cashly.cashly_api.price_types.domain.repository.PriceTypeRepository;
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
