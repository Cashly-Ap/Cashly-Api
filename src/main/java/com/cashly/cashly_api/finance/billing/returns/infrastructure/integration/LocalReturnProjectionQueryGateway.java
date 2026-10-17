package com.cashly.cashly_api.finance.billing.returns.infrastructure.integration;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnProjectionQueryGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.ReturnInvoiceQuery;
import com.cashly.cashly_api.inventory.warehouses.management.application.ports.in.WarehouseDirectoryQuery;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
@Component
public class LocalReturnProjectionQueryGateway implements ReturnProjectionQueryGateway {
    private final ReturnInvoiceQuery invoices;
    private final WarehouseDirectoryQuery warehouses;
    public LocalReturnProjectionQueryGateway(ReturnInvoiceQuery invoices, WarehouseDirectoryQuery warehouses) {
        this.invoices = invoices; this.warehouses = warehouses;
    }
    public Optional<InvoiceLabels> findInvoice(UUID id) {
        return invoices.findById(id).map(invoice -> new InvoiceLabels(invoice.number(), invoice.customerName(), invoice.customerRif()));
    }
    public Optional<String> findWarehouseName(UUID id) { return warehouses.findById(id).map(warehouse -> warehouse.name()); }
}
