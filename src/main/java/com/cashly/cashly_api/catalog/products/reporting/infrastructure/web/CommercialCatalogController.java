package com.cashly.cashly_api.catalog.products.reporting.infrastructure.web;

import com.cashly.cashly_api.catalog.products.reporting.application.dto.query.model.CommercialCatalogQuery;
import com.cashly.cashly_api.catalog.products.reporting.application.generate_commercial_catalog_pdf.use_case.GenerateCommercialCatalogPdfUseCase;
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
@RequestMapping("/api/reports/inventory/commercial-catalog")
public class CommercialCatalogController {

    private final GenerateCommercialCatalogPdfUseCase generateCommercialCatalogPdfUseCase;

    public CommercialCatalogController(
            GenerateCommercialCatalogPdfUseCase generateCommercialCatalogPdfUseCase) {
        this.generateCommercialCatalogPdfUseCase = generateCommercialCatalogPdfUseCase;
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAuthority('CATALOG:EXPORT') and hasAuthority('DOCUMENT_ADMIN:EXPORT')")
    public ResponseEntity<byte[]> getCatalogPdf(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID priceTypeId,
            @RequestParam(required = false) Boolean active) {
        CommercialCatalogQuery query = new CommercialCatalogQuery(search,
                categoryId, priceTypeId, active);
        byte[] pdf = generateCommercialCatalogPdfUseCase.execute(query);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"catalogo-comercial.pdf\"")
                .body(pdf);
    }
}
