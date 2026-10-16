package com.cashly.cashly_api.invoices.infrastructure.integration;

import com.cashly.cashly_api.invoices.application.ports.out.InvoiceReturnDetailsGateway;
import com.cashly.cashly_api.products.application.ports.in.SalesProductCatalogQuery;
import com.cashly.cashly_api.returns.application.ports.in.InvoiceReturnDetailsQuery;
import com.cashly.cashly_api.warehouses.management.application.ports.in.WarehouseDirectoryQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LocalInvoiceReturnDetailsAdapter implements InvoiceReturnDetailsGateway {
    private final InvoiceReturnDetailsQuery returns;
    private final SalesProductCatalogQuery products;
    private final WarehouseDirectoryQuery warehouses;

    public LocalInvoiceReturnDetailsAdapter(InvoiceReturnDetailsQuery returns, SalesProductCatalogQuery products,
                                            WarehouseDirectoryQuery warehouses) {
        this.returns = returns;
        this.products = products;
        this.warehouses = warehouses;
    }

    @Override
    public List<ReturnDetails> findByInvoiceId(UUID invoiceId) {
        return returns.findByInvoiceId(invoiceId).stream().map(item -> new ReturnDetails(item.id(), item.number(),
                item.type(), item.reason(), item.total(), item.createdAt(), item.createdByName(), item.lines().stream()
                .map(line -> {
                    var product = products.findProduct(line.productId()).orElse(null);
                    return new Line(line.productId(), product == null ? "" : product.name(),
                            product == null ? "" : product.code(), line.quantity(), line.unitPrice(), line.subtotal(),
                            line.allocations().stream().map(allocation -> new Allocation(allocation.warehouseId(),
                                    warehouses.findById(allocation.warehouseId()).map(warehouse -> warehouse.name())
                                            .orElse(""), allocation.allocated())).toList());
                }).toList())).toList();
    }
}
