package com.cashly.cashly_api.price_types.infrastructure.queries;

import com.cashly.cashly_api.price_types.application.api.ProductPriceTypeExistenceQuery;
import com.cashly.cashly_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import com.cashly.cashly_api.price_types.infrastructure.persistence.repositories.SpringDataPriceTypeRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaProductPriceTypeExistenceQuery implements ProductPriceTypeExistenceQuery {
    private final SpringDataPriceTypeRepository priceTypes;

    public JpaProductPriceTypeExistenceQuery(SpringDataPriceTypeRepository priceTypes) {
        this.priceTypes = priceTypes;
    }

    @Override
    public Set<UUID> existingIds(List<UUID> ids) {
        return priceTypes.findAllById(ids).stream().map(PriceTypeEntity::getId).collect(Collectors.toSet());
    }
}
