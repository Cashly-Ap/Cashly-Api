package com.cashly.cashly_api.catalog.price_types.application.delete.use_case;

import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeInUseException;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.catalog.price_types.application.ports.out.PriceTypeUsageGateway;
import com.cashly.cashly_api.catalog.price_types.domain.repository.PriceTypeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeletePriceTypeUseCase {

    private final PriceTypeUsageGateway usageGateway;
    private final PriceTypeRepository priceTypeRepository;

    public DeletePriceTypeUseCase(PriceTypeUsageGateway usageGateway,
            PriceTypeRepository priceTypeRepository) {
        this.usageGateway = usageGateway;
        this.priceTypeRepository = priceTypeRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (!priceTypeRepository.existsById(id)) {
            throw new PriceTypeNotFoundException(id);
        }
        if (usageGateway.isInUseByProducts(id)) {
            throw new PriceTypeInUseException(id);
        }
        priceTypeRepository.delete(id);
    }
}
