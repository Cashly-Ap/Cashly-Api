package com.cashly.cashly_api.price_types.application.usecases;

import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeInUseException;
import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeletePriceTypeUseCase {

    private final PriceTypeRepository priceTypeRepository;

    public DeletePriceTypeUseCase(PriceTypeRepository priceTypeRepository) {
        this.priceTypeRepository = priceTypeRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (!priceTypeRepository.existsById(id)) {
            throw new PriceTypeNotFoundException(id);
        }
        if (priceTypeRepository.isInUseByProducts(id)) {
            throw new PriceTypeInUseException(id);
        }
        priceTypeRepository.delete(id);
    }
}
