package com.parsun.parsun_api.returns.application.usecases;

import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.returns.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.returns.application.dto.response.ReturnResponse;
import com.parsun.parsun_api.returns.application.mappers.ReturnMapper;
import com.parsun.parsun_api.returns.domain.entities.CancelledAllocation;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnLineAllocation;
import com.parsun.parsun_api.returns.domain.entities.ReturnNumber;
import com.parsun.parsun_api.returns.domain.entities.ReturnType;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.ReferenceType;
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

public class CreateReturnUseCase {

    private final ReturnRepository returnRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReturnMapper returnMapper;
    private final PaymentAllocationService paymentAllocationService;
    private final PaymentAllocationRepository allocationRepository;
    private final AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public CreateReturnUseCase(ReturnRepository returnRepository,
                                InvoiceRepository invoiceRepository,
                                ReturnMapper returnMapper,
                                PaymentAllocationService paymentAllocationService,
                                PaymentAllocationRepository allocationRepository,
                                AddToCustomerBalanceUseCase addToCustomerBalanceUseCase,
                                CreditNoteRepository creditNoteRepository,
                                DebitNoteRepository debitNoteRepository,
                                WarehouseStockRepository warehouseStockRepository,
                                StockMovementRepository stockMovementRepository) {
        this.returnRepository = returnRepository;
        this.invoiceRepository = invoiceRepository;
        this.returnMapper = returnMapper;
        this.paymentAllocationService = paymentAllocationService;
        this.allocationRepository = allocationRepository;
        this.addToCustomerBalanceUseCase = addToCustomerBalanceUseCase;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public ReturnResponse execute(CreateReturnRequest request, UUID authenticatedUserId, String userName) {
        int year = LocalDate.now().getYear();
        int sequence = returnRepository.getNextSequenceForYear(year);
        ReturnNumber returnNumber = ReturnNumber.of(year, sequence);

        List<ReturnLine> lines = request.lines().stream()
                .map(ReturnMapper::toLine)
                .toList();

        BigDecimal total = lines.stream()
                .map(ReturnLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Return return_ = Return.create(
                returnNumber,
                InvoiceId.of(request.invoiceId()),
                request.customerId(),
                ReturnType.valueOf(request.returnType()),
                request.reason(),
                total,
                lines,
                authenticatedUserId,
                userName
        );

        Return saved = returnRepository.save(return_);

        
        restoreStockFromReturn(saved);

        Invoice invoice = invoiceRepository.findById(return_.getInvoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException(return_.getInvoiceId().value()));

        invoice.applyReturn(saved);

        invoiceRepository.save(invoice);

        BigDecimal totalPaid = allocationRepository.getAllocatedAmountByInvoiceId(return_.getInvoiceId().value());

        if (return_.getReturnType() == ReturnType.AUTOMATIC) {
            List<CancelledAllocation> snapshot = captureCancelledAllocations(return_.getInvoiceId().value());
            cancelInvoicePayments(return_.getInvoiceId().value());
            saved = rebuildReturnWithCancelledAllocations(saved, snapshot);
        }

        BigDecimal totalCreditNotes = creditNoteRepository.findByInvoiceId(return_.getInvoiceId().value()).stream()
                .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = debitNoteRepository.findByInvoiceId(return_.getInvoiceId().value()).stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal effectiveTotal = invoice.calculateTotal(totalCreditNotes, totalDebitNotes);
        if (effectiveTotal.compareTo(BigDecimal.ZERO) < 0) {
            effectiveTotal = BigDecimal.ZERO;
        }
        BigDecimal surplus = totalPaid.subtract(effectiveTotal);

        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            addToCustomerBalanceUseCase.execute(
                return_.getCustomerId(),
                BalanceType.RETURNS,
                surplus,
                BalanceType.RETURNS,
                return_.getId().value(),
                return_.getReturnNumber().value(),
                invoice.getInvoiceNumber().value(),
                authenticatedUserId
            );
        }

        return returnMapper.toResponse(saved);
    }

    
    private void restoreStockFromReturn(Return return_) {
        for (ReturnLine line : return_.getLines()) {
            for (ReturnLineAllocation allocation : line.getAllocations()) {
                
                int quantity = allocation.getAllocated().intValue();
                
                
                if (quantity <= 0) {
                    continue;
                }

                WarehouseStock stock = warehouseStockRepository
                        .findByWarehouseIdAndProductIdForUpdate(allocation.getWarehouseId(), line.getProductId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Stock not found for warehouse=" + allocation.getWarehouseId() 
                                + " product=" + line.getProductId()));

                int previousStock = stock.getQuantity();
                stock.addStock(quantity);
                warehouseStockRepository.save(stock);

                stockMovementRepository.save(StockMovement.createWithReference(
                        allocation.getWarehouseId(),
                        line.getProductId(),
                        MovementType.RETURN_ENTRY,
                        quantity,
                        previousStock,
                        stock.getQuantity(),
                        null,
                        null,
                        ReferenceType.RETURN,
                        return_.getId().value(),
                        return_.getReturnNumber().value(),
                        "Return: " + return_.getReturnNumber().value()
                ));
            }
        }
    }

    private void cancelInvoicePayments(UUID invoiceId) {
        allocationRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentAllocation::getPaymentId)
                .distinct()
                .forEach(pid -> paymentAllocationService.cancelPayment(pid, "Return created"));
    }

    private List<CancelledAllocation> captureCancelledAllocations(UUID invoiceId) {
        return allocationRepository.findByInvoiceId(invoiceId).stream()
                .map(a -> CancelledAllocation.create(a.getPaymentId(), a.getInvoiceId(), a.getAllocatedAmount()))
                .toList();
    }

    private Return rebuildReturnWithCancelledAllocations(Return original, List<CancelledAllocation> cancelledAllocations) {
        return Return.reconstitute(
                original.getId(),
                original.getReturnNumber(),
                original.getInvoiceId(),
                original.getCustomerId(),
                original.getReturnType(),
                original.getReason(),
                original.getTotal(),
                new ArrayList<>(original.getLines()),
                cancelledAllocations,
                original.getCreatedBy(),
                original.getCreatedByName(),
                original.getCreatedAt(),
                original.getUpdatedAt()
        );
    }
}
