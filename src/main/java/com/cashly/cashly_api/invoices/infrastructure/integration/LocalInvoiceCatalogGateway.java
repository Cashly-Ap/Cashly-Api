package com.cashly.cashly_api.invoices.infrastructure.integration;
import com.cashly.cashly_api.invoices.application.ports.out.InvoiceCatalogGateway;
import com.cashly.cashly_api.products.application.ports.in.StockCatalogQuery;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
@Component
public class LocalInvoiceCatalogGateway implements InvoiceCatalogGateway {
    private final StockCatalogQuery products;
    public LocalInvoiceCatalogGateway(StockCatalogQuery products) { this.products = products; }
    public Optional<Product> findProduct(UUID id) {
        return products.findProduct(id).map(product -> new Product(product.id(), product.name(), product.code()));
    }
}