package com.parsun.parsun_api.price_types.infrastructure.persistence.repositories;

import com.parsun.parsun_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface SpringDataPriceTypeRepository extends JpaRepository<PriceTypeEntity, UUID> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    @Query("""
        SELECT COUNT(pp) > 0 FROM ProductPriceEntity pp
        WHERE pp.priceType.id = :priceTypeId
        """)
    boolean isInUseByProducts(@Param("priceTypeId") UUID priceTypeId);

    @Query("""
        SELECT DISTINCT pp.priceType.id FROM ProductPriceEntity pp
        WHERE pp.priceType.id IN :priceTypeIds
        """)
    Set<UUID> findIdsInUseByProducts(@Param("priceTypeIds") Set<UUID> priceTypeIds);
}
