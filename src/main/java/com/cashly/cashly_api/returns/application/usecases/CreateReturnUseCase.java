package com.cashly.cashly_api.returns.application.usecases;

import com.cashly.cashly_api.returns.application.dto.request.CreateReturnRequest;
import com.cashly.cashly_api.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.returns.domain.entities.CancelledAllocation;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnLine;
import com.cashly.cashly_api.returns.domain.entities.ReturnNumber;
import com.cashly.cashly_api.returns.domain.entities.ReturnType;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;
import jakarta.transaction.Transactional;

import com.cashly.cashly_api.returns.application.contracts.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnInvoiceQueryGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnPaymentSettlementGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnBalanceGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnCashGateway;
import com.cashly.cashly_api.returns.application.contracts.ReturnInventoryGateway;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateReturnUseCase {

    private final ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway;
    private final ReturnRepository returnRepository;
    private final ReturnResponseAssembler responses;
    private final ReturnInvoiceQueryGateway invoiceQueries;
    private final ReturnPaymentSettlementGateway paymentSettlements;
    private final ReturnBalanceGateway balances;
    private final ReturnInventoryGateway inventoryGateway;
    private final ReturnCashGateway cash;
    public CreateReturnUseCase(ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway, ReturnRepository returnRepository,
            ReturnResponseAssembler responses, ReturnInvoiceQueryGateway invoiceQueries, ReturnPaymentSettlementGateway paymentSettlements,
            ReturnBalanceGateway balances, ReturnInventoryGateway inventoryGateway, ReturnCashGateway cash) {
        this.invoiceAdjustmentGateway = invoiceAdjustmentGateway; this.returnRepository = returnRepository;
        this.responses = responses; this.invoiceQueries = invoiceQueries; this.paymentSettlements = paymentSettlements;
        this.balances = balances; this.inventoryGateway = inventoryGateway; this.cash = cash;
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
                request.invoiceId(),
                request.customerId(),
                ReturnType.valueOf(request.returnType()),
                request.reason(),
                total,
                lines,
                authenticatedUserId,
                userName
        );

        Return saved = returnRepository.save(return_);
        cash.record(new ReturnCashGateway.Entry(saved.getTotal(), saved.getId().value(), saved.getReturnNumber().value(), authenticatedUserId, userName));

        inventoryGateway.restoreStock(ReturnInventoryGateway.Command.from(saved, null));

        invoiceAdjustmentGateway.apply(saved);
        var invoice = invoiceQueries.requireById(return_.getInvoiceId());



        BigDecimal totalPaid = paymentSettlements.allocatedAmount(return_.getInvoiceId());

        if (return_.getReturnType() == ReturnType.AUTOMATIC) {
            List<CancelledAllocation> snapshot = paymentSettlements.capture(return_.getInvoiceId());
            paymentSettlements.cancelInvoicePayments(return_.getInvoiceId());
            saved = returnRepository.save(rebuildReturnWithCancelledAllocations(saved, snapshot));
        }

        BigDecimal totalCreditNotes = invoiceQueries.creditNoteAmounts(return_.getInvoiceId()).stream()
                .map(c -> c != null ? c : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitNotes = invoiceQueries.debitNoteAmounts(return_.getInvoiceId()).stream()
                .map(d -> d != null ? d : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal effectiveTotal = invoice.calculateTotal(totalCreditNotes, totalDebitNotes);
        if (effectiveTotal.compareTo(BigDecimal.ZERO) < 0) {
            effectiveTotal = BigDecimal.ZERO;
        }
        BigDecimal surplus = totalPaid.subtract(effectiveTotal);

        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            balances.credit(new ReturnBalanceGateway.Command(return_.getCustomerId(), surplus, return_.getId().value(),
                    return_.getReturnNumber().value(), invoice.number(), authenticatedUserId));
        }

        return responses.toResponse(saved);
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
