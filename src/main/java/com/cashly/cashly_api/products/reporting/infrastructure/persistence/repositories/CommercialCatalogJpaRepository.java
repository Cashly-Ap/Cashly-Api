package com.cashly.cashly_api.products.reporting.infrastructure.persistence.repositories;

import com.cashly.cashly_api.products.reporting.infrastructure.persistence.projections.CommercialCatalogProjection;

import com.cashly.cashly_api.products.infrastructure.persistence.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommercialCatalogJpaRepository extends JpaRepository<ProductEntity, UUID> {

    @Query(value = """
            SELECT DISTINCT
              BIN_TO_UUID(p.id) AS productId,
              p.name AS productName,
              p.code AS sku,
              p.description AS description,
              p.photo AS photo,
              BIN_TO_UUID(c.id) AS categoryId,
              c.name AS categoryName,
              pp.price AS price,
              pt.name AS priceTypeName
            FROM products p
            JOIN categories c ON c.id = p.category_id
            LEFT JOIN product_prices pp ON pp.product_id = p.id
              AND pp.price_type_id = UNHEX(REPLACE(:priceTypeId, '-', ''))
            LEFT JOIN price_types pt ON pt.id = pp.price_type_id
            WHERE (:active IS NULL OR p.active = :active)
              AND (:search IS NULL OR p.name LIKE CONCAT('%', :search, '%'))
              AND (:categoryId IS NULL OR p.category_id = UNHEX(REPLACE(:categoryId, '-', '')))
            ORDER BY c.name ASC, p.name ASC
            """,
            nativeQuery = true)
    List<CommercialCatalogProjection> findCommercialCatalog(
            @Param("search") String search,
            @Param("categoryId") String categoryId,
            @Param("priceTypeId") String priceTypeId,
            @Param("active") Boolean active);
}
