package com.cashly.cashly_api.warehouses.reporting.infrastructure.web;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.CatalogPdfQuery;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.CatalogReportQuery;
import com.cashly.cashly_api.warehouses.reporting.application.generate_catalog_pdf.GenerateCatalogPdfUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.get_catalog_report.GetCatalogReportUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports/inventory/catalog")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
public class CatalogReportController {

    private final GetCatalogReportUseCase getCatalogReportUseCase;
    private final GenerateCatalogPdfUseCase generateCatalogPdfUseCase;

    public CatalogReportController(
            GetCatalogReportUseCase getCatalogReportUseCase,
            GenerateCatalogPdfUseCase generateCatalogPdfUseCase) {
        this.getCatalogReportUseCase = getCatalogReportUseCase;
        this.generateCatalogPdfUseCase = generateCatalogPdfUseCase;
    }

    @GetMapping
    public PageResponse<CatalogReportResponse> getCatalog(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) Boolean composite,
            @RequestParam(required = false) Boolean withStock,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        CatalogReportQuery query = new CatalogReportQuery(
                search, categoryId, warehouseId, composite, withStock, lowStock, page, size);
        return getCatalogReportUseCase.execute(query);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getCatalogPdf(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) Boolean composite,
            @RequestParam(required = false) Boolean withStock,
            @RequestParam(required = false) Boolean lowStock) {
        CatalogPdfQuery query = new CatalogPdfQuery(
                search, categoryId, warehouseId, composite, withStock, lowStock);
        byte[] pdf = generateCatalogPdfUseCase.execute(query);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-productos.pdf\"")
                .body(pdf);
    }
}
