package com.parsun.parsun_api.reports.inventory.infrastructure.persistence;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CommercialCatalogResponse;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogRepositoryPort;
import com.parsun.parsun_api.reports.inventory.application.query.CommercialCatalogQuery;

import java.util.List;
import java.util.UUID;

public class CommercialCatalogRepositoryAdapter implements CommercialCatalogRepositoryPort {

    private final CommercialCatalogJpaRepository jpaRepository;

    public CommercialCatalogRepositoryAdapter(CommercialCatalogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CommercialCatalogResponse> findCatalog(CommercialCatalogQuery query) {
        String search = (query.search() != null && !query.search().isBlank()) ? query.search() : null;
        String categoryId = query.categoryId() != null ? query.categoryId().toString() : null;
        String priceTypeId = query.priceTypeId() != null ? query.priceTypeId().toString() : null;

        return jpaRepository.findCommercialCatalog(search, categoryId, priceTypeId, query.active())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CommercialCatalogResponse toResponse(CommercialCatalogProjection p) {
        return new CommercialCatalogResponse(
                UUID.fromString(p.getProductId()),
                p.getProductName(),
                p.getSku(),
                p.getDescription(),
                p.getPhoto(),
                UUID.fromString(p.getCategoryId()),
                p.getCategoryName(),
                p.getPrice(),
                p.getPriceTypeName()
        );
    }
}
