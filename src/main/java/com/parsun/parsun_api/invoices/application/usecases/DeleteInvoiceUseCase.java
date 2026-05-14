package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public DeleteInvoiceUseCase(InvoiceRepository invoiceRepository,
                                 OrderRepository orderRepository,
                                 WarehouseStockRepository warehouseStockRepository,
                                 StockMovementRepository stockMovementRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        invoice.delete();
        revertLinkedOrder(invoice);
        revertStockAllocations(invoice);

        invoiceRepository.deleteById(InvoiceId.of(id));
    }

    private void revertLinkedOrder(Invoice invoice) {
        if (invoice.getOrderId() != null) {
            Order order = orderRepository.findById(OrderId.of(invoice.getOrderId()))
                    .orElseThrow(() -> new OrderNotFoundException(invoice.getOrderId()));
            order.revertFromInvoiced();
            orderRepository.save(order);
        }
    }

    private void revertStockAllocations(Invoice invoice) {
        for (InvoiceLine line : invoice.getLines()) {
            for (InvoiceLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            int previous = stock.getQuantity();
                            stock.addStock(allocation.getAllocated().intValue());
                            warehouseStockRepository.save(stock);

                            stockMovementRepository.save(StockMovement.create(
                                    allocation.getWarehouseId(),
                                    line.getProductId(),
                                    MovementType.ENTRY,
                                    allocation.getAllocated().intValue(),
                                    previous,
                                    stock.getQuantity(),
                                    null,
                                    null,
                                    "Invoice deleted"
                            ));
                        });
            }
        }
    }
}
