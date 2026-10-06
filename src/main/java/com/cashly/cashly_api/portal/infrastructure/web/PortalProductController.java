package com.cashly.cashly_api.portal.infrastructure.web;

import com.cashly.cashly_api.portal.application.dto.response.PortalProductResponse;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import com.cashly.cashly_api.portal.application.usecases.GeneratePortalCatalogPdfUseCase;
import com.cashly.cashly_api.portal.application.usecases.ListProductsUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/portal/products")
public class PortalProductController {

    private final ListProductsUseCase listProductsUseCase;
    private final GeneratePortalCatalogPdfUseCase generatePortalCatalogPdfUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public PortalProductController(ListProductsUseCase listProductsUseCase,
                                    GeneratePortalCatalogPdfUseCase generatePortalCatalogPdfUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.listProductsUseCase = listProductsUseCase;
        this.generatePortalCatalogPdfUseCase = generatePortalCatalogPdfUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PortalProductResponse>> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        PageResponse<PortalProductResponse> response = listProductsUseCase.execute(
                customerId, search, categoryId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/catalog/pdf")
    public ResponseEntity<byte[]> downloadCatalogPdf() {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        byte[] pdfBytes = generatePortalCatalogPdfUseCase.execute(customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("catalogo-comercial.pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
