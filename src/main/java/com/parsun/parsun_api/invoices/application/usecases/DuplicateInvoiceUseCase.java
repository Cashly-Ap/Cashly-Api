package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuplicateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public DuplicateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                    WarehouseStockRepository warehouseStockRepository,
                                    StockMovementRepository stockMovementRepository) {
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id) {
        Invoice original = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        int year = LocalDate.now().getYear();
        int sequence = invoiceRepository.getNextSequenceForYear(year);
        InvoiceNumber newNumber = InvoiceNumber.of(year, sequence);

        List<InvoiceLine> copiedLines = original.getLines().stream()
                .map(line -> {
                    List<InvoiceLineAllocation> copiedAllocations = applyStockAllocations(line);
                    return InvoiceLine.create(
                            line.getProductId(),
                            line.getProductName(),
                            line.getProductCode(),
                            line.getQuantity(),
                            line.getUnitPrice(),
                            copiedAllocations
                    );
                })
                .toList();

        Invoice duplicate = Invoice.create(
                newNumber,
                original.getCustomerId(),
                original.getCustomerName(),
                original.getCustomerRif(),
                original.getCreatedBy(),
                original.getCreatedByName(),
                null,
                null,
                original.getPaymentType(),
                original.getNotes(),
                copiedLines
        );

        Invoice saved = invoiceRepository.save(duplicate);
        return InvoiceMapper.toDetailResponse(saved);
    }

    private List<InvoiceLineAllocation> applyStockAllocations(InvoiceLine line) {
        List<InvoiceLineAllocation> allocations = new ArrayList<>();
        for (InvoiceLineAllocation allocation : line.getAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock not found for warehouse=" + allocation.getWarehouseId() + " product=" + line.getProductId()));

            int previousStock = stock.getQuantity();
            stock.removeStock(allocation.getAllocated().intValue());
            int newStock = stock.getQuantity();

            warehouseStockRepository.save(stock);

            stockMovementRepository.save(StockMovement.create(
                    allocation.getWarehouseId(),
                    line.getProductId(),
                    MovementType.EXIT,
                    allocation.getAllocated().intValue(),
                    previousStock,
                    newStock,
                    null,
                    null,
                    "Invoice duplicated"
            ));

            allocations.add(InvoiceLineAllocation.create(allocation.getWarehouseId(), allocation.getAllocated()));
        }
        return allocations;
    }
}
