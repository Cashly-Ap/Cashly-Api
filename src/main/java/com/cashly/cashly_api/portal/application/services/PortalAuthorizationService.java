package com.cashly.cashly_api.portal.application.services;

import com.cashly.cashly_api.customers.domain.entities.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PortalAuthorizationService {

    private final CustomerWarehouseAccessRepository customerWarehouseAccessRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public PortalAuthorizationService(CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
                                      WarehouseStockRepository warehouseStockRepository) {
        this.customerWarehouseAccessRepository = customerWarehouseAccessRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    public UUID getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated customer found");
        }
        String principal = (String) authentication.getPrincipal();
        return UUID.fromString(principal);
    }

    public void requireOwnership(UUID resourceCustomerId, String resourceName) {
        if (!getCurrentCustomerId().equals(resourceCustomerId)) {
            throw new AuthorizationDeniedException(resourceName + " does not belong to current customer");
        }
    }

    public void requireProductAccess(UUID productId) {
        UUID customerId = getCurrentCustomerId();
        var allowedWarehouseIds = customerWarehouseAccessRepository.findByCustomerId(customerId).stream()
                .map(CustomerWarehouseAccess::getWarehouseId)
                .collect(Collectors.toSet());
        boolean available = warehouseStockRepository.findByProductId(productId).stream()
                .anyMatch(stock -> allowedWarehouseIds.contains(stock.getWarehouseId())
                        && stock.getQuantity() - stock.getReserved() > 0);
        if (!available) {
            throw new AuthorizationDeniedException("Product is not available in an assigned warehouse");
        }
    }
}
