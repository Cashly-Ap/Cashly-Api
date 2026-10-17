package com.cashly.cashly_api.finance.billing.invoices.application.ports.out;
import java.util.Optional;
import java.util.UUID;
public interface InvoiceCatalogGateway {
    Optional<Product> findProduct(UUID productId);
    record Product(UUID id, String name, String code) {}
}