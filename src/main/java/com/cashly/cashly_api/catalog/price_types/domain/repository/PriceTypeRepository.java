package com.cashly.cashly_api.catalog.price_types.domain.repository;

import com.cashly.cashly_api.catalog.price_types.domain.entity.PriceType;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PriceTypeRepository {
    PriceType save(PriceType priceType);
    Optional<PriceType> findById(UUID id);
    PageResponse<PriceType> findAllWithFilters(String search, Boolean active,
            int page, int size, String orderBy, String orderDir);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    void delete(UUID id);
}
