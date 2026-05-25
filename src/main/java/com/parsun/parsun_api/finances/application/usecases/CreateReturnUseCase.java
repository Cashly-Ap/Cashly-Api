package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.finances.application.dto.response.ReturnDetailResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.entities.ReturnLine;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.finances.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnType;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories.SpringDataWarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateReturnUseCase {

    private final ReturnRepository returnRepository;
    private final InvoiceRepository invoiceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SpringDataWarehouseStockRepository warehouseStockJpaRepository;
    private final CreditNoteRepository creditNoteRepository;

    public CreateReturnUseCase(ReturnRepository returnRepository,
                               InvoiceRepository invoiceRepository,
                               StockMovementRepository stockMovementRepository,
                               SpringDataWarehouseStockRepository warehouseStockJpaRepository,
                               CreditNoteRepository creditNoteRepository) {
        this.returnRepository = returnRepository;
        this.invoiceRepository = invoiceRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.warehouseStockJpaRepository = warehouseStockJpaRepository;
        this.creditNoteRepository = creditNoteRepository;
    }

    @Transactional
    public ReturnDetailResponse execute(CreateReturnRequest request, UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(request.invoiceId()))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + request.invoiceId()));

        int year = LocalDate.now().getYear();
        int sequence = returnRepository.getNextSequenceForYear(year);
        ReturnNumber returnNumber = ReturnNumber.of(year, sequence);

        ReturnType returnType = ReturnType.valueOf(request.returnType());

        Return returnEntity = Return.create(
                returnNumber,
                request.invoiceId(),
                request.customerId(),
                returnType,
                request.reason(),
                authenticatedUserId,
                "User"
        );

        Return savedReturn = returnRepository.save(returnEntity);

        List<ReturnLine> savedLines;
        BigDecimal totalReturnAmount;

        if (returnType == ReturnType.AUTOMATIC) {
            UUID warehouseId = request.lines().getFirst().warehouseId();
            savedLines = new ArrayList<>();
            totalReturnAmount = BigDecimal.ZERO;

            for (var invoiceLine : invoice.getLines()) {
                ReturnLine line = ReturnLine.create(
                        savedReturn.getId().value(),
                        invoiceLine.getId().value(),
                        invoiceLine.getProductId(),
                        invoiceLine.getQuantity(),
                        warehouseId,
                        invoiceLine.getUnitPrice()
                );
                ReturnLine savedLine = returnRepository.saveLine(line);
                savedLines.add(savedLine);
                totalReturnAmount = totalReturnAmount.add(savedLine.getLineTotal());

                createStockMovementEntry(warehouseId, invoiceLine.getProductId(),
                        invoiceLine.getQuantity().intValue(), "Return: " + returnNumber.value());
            }
        } else {
            savedLines = new ArrayList<>();
            totalReturnAmount = BigDecimal.ZERO;

            for (var lineReq : request.lines()) {
                ReturnLine line = ReturnLine.create(
                        savedReturn.getId().value(),
                        lineReq.invoiceLineId(),
                        lineReq.productId(),
                        lineReq.quantity(),
                        lineReq.warehouseId(),
                        lineReq.unitPrice()
                );
                ReturnLine savedLine = returnRepository.saveLine(line);
                savedLines.add(savedLine);
                totalReturnAmount = totalReturnAmount.add(savedLine.getLineTotal());

                createStockMovementEntry(lineReq.warehouseId(), lineReq.productId(),
                        lineReq.quantity().intValue(), "Return: " + returnNumber.value());
            }

            int cnYear = LocalDate.now().getYear();
            int cnSequence = creditNoteRepository.getNextSequenceForYear(cnYear);
            var cnNumber = CreditNoteNumber.of(cnYear, cnSequence);

            var creditNote = com.parsun.parsun_api.finances.domain.entities.CreditNote.create(
                    cnNumber,
                    request.customerId(),
                    request.invoiceId(),
                    totalReturnAmount,
                    "Credit note for return " + returnNumber.value(),
                    authenticatedUserId,
                    "User"
            );
            creditNoteRepository.save(creditNote);
        }

        return FinanceMapper.toReturnDetailResponse(savedReturn, savedLines,
                invoice.getInvoiceNumber().value(), invoice.getCustomerName(), invoice.getCustomerRif(), null);
    }

    private void createStockMovementEntry(UUID warehouseId, UUID productId, int quantity, String notes) {
        var stockOpt = warehouseStockJpaRepository.findByWarehouseIdAndProductId(warehouseId, productId);

        int previousStock;
        int newStock;

        if (stockOpt.isPresent()) {
            var stock = stockOpt.get();
            previousStock = stock.getQuantity();
            newStock = previousStock + quantity;
            stock.setQuantity(newStock);
            stock.setLastMovementAt(java.time.LocalDateTime.now());
            stock.setUpdatedAt(java.time.LocalDateTime.now());
            warehouseStockJpaRepository.save(stock);
        } else {
            previousStock = 0;
            newStock = quantity;
            var newStockEntity = new WarehouseStockEntity();
            newStockEntity.setId(UUID.randomUUID());
            newStockEntity.setWarehouseId(warehouseId);
            newStockEntity.setProductId(productId);
            newStockEntity.setQuantity(quantity);
            newStockEntity.setReserved(0);
            newStockEntity.setLastMovementAt(java.time.LocalDateTime.now());
            newStockEntity.setUpdatedAt(java.time.LocalDateTime.now());
            warehouseStockJpaRepository.save(newStockEntity);
        }

        StockMovement movement = StockMovement.create(
                warehouseId, productId, MovementType.ENTRY,
                quantity, previousStock, newStock,
                null, null, notes
        );
        stockMovementRepository.save(movement);
    }
}
