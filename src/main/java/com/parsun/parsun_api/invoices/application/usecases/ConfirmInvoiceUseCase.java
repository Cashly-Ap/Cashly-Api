package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.shared.services.CreditGuard;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConfirmInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CreditGuard creditGuard;

    public ConfirmInvoiceUseCase(InvoiceRepository invoiceRepository,
                                  WarehouseStockRepository warehouseStockRepository,
                                  StockMovementRepository stockMovementRepository,
                                  CreditGuard creditGuard) {
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.creditGuard = creditGuard;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        if (invoice.getPaymentType() == PaymentType.CREDIT) {
            creditGuard.ensureCreditAvailable(invoice.getCustomerId(), invoice.calculateTotal());
        }

        invoice.confirm();
        deductStockAllocations(invoice);

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceMapper.toDetailResponse(saved);
    }

    private void deductStockAllocations(Invoice invoice) {
        for (InvoiceLine line : invoice.getLines()) {
            for (InvoiceLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            int previousQuantity = stock.getQuantity();
                            int previousReserved = stock.getReserved();
                            stock.deductReserved(allocation.getAllocated().intValue());
                            warehouseStockRepository.save(stock);

                            stockMovementRepository.save(StockMovement.create(
                                    allocation.getWarehouseId(),
                                    line.getProductId(),
                                    MovementType.EXIT,
                                    allocation.getAllocated().intValue(),
                                    previousQuantity,
                                    stock.getQuantity(),
                                    null,
                                    null,
                                    "Factura confirmada"
                            ));
                        });
            }
        }
    }
}
