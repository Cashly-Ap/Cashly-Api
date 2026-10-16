package com.cashly.cashly_api.returns.infrastructure.persistence.repositories;

import com.cashly.cashly_api.returns.infrastructure.persistence.entities.ReturnLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Set;
import java.util.UUID;

public interface SpringDataReturnsProductReferenceRepository extends Repository<ReturnLineEntity, UUID> {
    @Query(value = "SELECT DISTINCT BIN_TO_UUID(product_id) FROM return_lines WHERE product_id IN :ids", nativeQuery = true)
    Set<UUID> findReferencedProductIds(@Param("ids") Set<UUID> ids);
}