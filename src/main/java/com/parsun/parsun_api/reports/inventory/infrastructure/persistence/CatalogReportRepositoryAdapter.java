package com.parsun.parsun_api.reports.inventory.infrastructure.persistence;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.parsun.parsun_api.reports.inventory.application.port.CatalogReportRepositoryPort;
import com.parsun.parsun_api.reports.inventory.application.query.CatalogReportQuery;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
                query.categoryId(),
                query.warehouseId(),
                query.composite(),
                query.withStock(),
                query.lowStock(),
                pageable
        );
        Page<CatalogReportResponse> mapped = page.map(this::toResponse);
        return PageResponse.from(mapped);
    }

    private CatalogReportResponse toResponse(CatalogReportProjection p) {
        return new CatalogReportResponse(
                p.getProductId(),
                p.getProductName(),
                p.getSku(),
                p.getCategoryId(),
                p.getCategoryName(),
                p.getComposite(),
                p.getWarehouseId(),
                p.getWarehouseName(),
                p.getQuantity(),
                p.getMinStock(),
                p.getActive()
        );
    }
}
