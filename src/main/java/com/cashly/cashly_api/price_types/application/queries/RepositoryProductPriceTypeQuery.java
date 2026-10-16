package com.cashly.cashly_api.price_types.application.queries;

import com.cashly.cashly_api.price_types.application.ports.in.ProductPriceTypeQuery;
import com.cashly.cashly_api.price_types.domain.PriceTypeRepository;
import java.util.Optional;
import java.util.UUID;

public class RepositoryProductPriceTypeQuery implements ProductPriceTypeQuery {
    private final PriceTypeRepository repository;

    public RepositoryProductPriceTypeQuery(PriceTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Entry> findById(UUID id) {
        return repository.findById(id).map(entity -> new Entry(entity.getId(), entity.getName()));
    }
}
