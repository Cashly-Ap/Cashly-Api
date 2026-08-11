package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.request.CancelInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.ReferenceType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CancelInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationService notificationService;

    public CancelInvoiceUseCase(InvoiceRepository invoiceRepository,
                                 WarehouseStockRepository warehouseStockRepository,
                                 StockMovementRepository stockMovementRepository,
                                 NotificationService notificationService) {
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, CancelInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        InvoiceStatus previousStatus = invoice.getStatus();
        invoice.cancel(request.reason());
        revertStockAllocations(invoice, previousStatus);

        Invoice saved = invoiceRepository.save(invoice);
        notificationService.notifyCustomerInvoiceCancelled(
                saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value(), request.reason());
        return InvoiceMapper.toDetailResponse(saved);
    }

    private void revertStockAllocations(Invoice invoice, InvoiceStatus previousStatus) {
        boolean wasConfirmed = previousStatus != InvoiceStatus.DRAFT;

        for (InvoiceLine line : invoice.getLines()) {
            for (InvoiceLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            if (wasConfirmed) {
                                int previous = stock.getQuantity();
                                stock.addStock(allocation.getAllocated().intValue());
                                warehouseStockRepository.save(stock);

                                stockMovementRepository.save(StockMovement.createWithReference(
                                        allocation.getWarehouseId(),
                                        line.getProductId(),
                                        MovementType.CANCEL_ENTRY,
                                        allocation.getAllocated().intValue(),
                                        previous,
                                        stock.getQuantity(),
                                        null,
                                        null,
                                        ReferenceType.INVOICE,
                                        invoice.getId().value(),
                                        invoice.getInvoiceNumber().value(),
                                        "Invoice cancelled"
                                ));
                            } else {
                                stock.releaseReserved(allocation.getAllocated().intValue());
                                warehouseStockRepository.save(stock);
                            }
                        });
            }
        }
    }
}
