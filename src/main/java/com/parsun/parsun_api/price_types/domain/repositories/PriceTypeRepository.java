package com.parsun.parsun_api.price_types.domain.repositories;

import com.parsun.parsun_api.price_types.domain.entities.PriceType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceTypeRepository {
    PriceType save(PriceType priceType);
    Optional<PriceType> findById(UUID id);
    List<PriceType> findAll();
    boolean existsById(UUID id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    boolean isInUseByProducts(UUID id);
    void delete(UUID id);
}
