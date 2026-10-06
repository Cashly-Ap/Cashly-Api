package com.cashly.cashly_api.warehouses.domain.repositories;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface WarehouseRepository {
    Warehouse save(Warehouse warehouse);
    Optional<Warehouse> findById(UUID id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    boolean hasActiveStock(UUID id);
    Set<UUID> findIdsWithActiveStock(Set<UUID> ids);
    PageResponse<Warehouse> findAllWithFilters(String search, Boolean active,
                                               int page, int size,
                                               String orderBy, String orderDir);
    void delete(UUID id);
}
