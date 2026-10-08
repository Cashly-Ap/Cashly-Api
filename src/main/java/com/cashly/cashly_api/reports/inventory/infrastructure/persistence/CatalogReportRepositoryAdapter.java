package com.cashly.cashly_api.reports.inventory.infrastructure.persistence;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.reports.inventory.application.port.CatalogReportRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.query.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public class CatalogReportRepositoryAdapter implements CatalogReportRepositoryPort {

    private final CatalogReportJpaRepository catalogReportJpaRepository;

    public CatalogReportRepositoryAdapter(CatalogReportJpaRepository catalogReportJpaRepository) {
        this.catalogReportJpaRepository = catalogReportJpaRepository;
    }

    @Override
    public PageResponse<CatalogReportResponse> findCatalog(CatalogReportQuery query) {
        PageRequest pageable = PageRequest.of(query.page(), query.size());
        Page<CatalogReportProjection> page = catalogReportJpaRepository.findCatalog(
                query.search(),
                query.categoryId() != null ? query.categoryId().toString() : null,
                query.warehouseId() != null ? query.warehouseId().toString() : null,
                query.composite(),
                query.withStock(),
                pageable
        );
        Page<CatalogReportResponse> mapped = page.map(this::toResponse);
        return PageResponseMapper.from(mapped);
    }

    private CatalogReportResponse toResponse(CatalogReportProjection p) {
        return new CatalogReportResponse(
                UUID.fromString(p.getProductId()),
                p.getProductName(),
                p.getSku(),
                UUID.fromString(p.getCategoryId()),
                p.getCategoryName(),
                p.getComposite(),
                UUID.fromString(p.getWarehouseId()),
                p.getWarehouseName(),
                p.getQuantity(),
                p.getActive()
        );
    }
}
