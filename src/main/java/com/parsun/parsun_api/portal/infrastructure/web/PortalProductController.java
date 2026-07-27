package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.portal.application.dto.response.PortalProductResponse;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.portal.application.usecases.ListProductsUseCase;
import com.parsun.parsun_api.shared.dto.PageResponse;
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
    private final PortalAuthorizationService portalAuthorizationService;

    public PortalProductController(ListProductsUseCase listProductsUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.listProductsUseCase = listProductsUseCase;
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
}
