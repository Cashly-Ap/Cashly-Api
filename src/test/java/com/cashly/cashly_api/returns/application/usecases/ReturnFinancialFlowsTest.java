package com.cashly.cashly_api.returns.application.usecases;
import com.cashly.cashly_api.returns.application.ports.out.*;
import com.cashly.cashly_api.returns.application.dto.request.CreateReturnRequest;
import com.cashly.cashly_api.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.returns.domain.entities.*;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReturnFinancialFlowsTest {
    private final ReturnRepository repository = mock(ReturnRepository.class);
    private final ReturnInvoiceAdjustmentGateway adjustments = mock(ReturnInvoiceAdjustmentGateway.class);
    private final ReturnInvoiceQueryGateway invoices = mock(ReturnInvoiceQueryGateway.class);
    private final ReturnPaymentSettlementGateway payments = mock(ReturnPaymentSettlementGateway.class);
    private final ReturnBalanceGateway balances = mock(ReturnBalanceGateway.class);
    private final ReturnInventoryGateway inventory = mock(ReturnInventoryGateway.class);
    private final ReturnCashGateway cash = mock(ReturnCashGateway.class);
    private final ReturnResponseAssembler mapper = mock(ReturnResponseAssembler.class);
    private final UUID invoiceId = UUID.randomUUID(), customerId = UUID.randomUUID(), creator = UUID.randomUUID();
    @BeforeEach
    void setup() {
        when(invoices.creditNoteAmounts(invoiceId)).thenReturn(Arrays.asList(new BigDecimal("10"), null));
        when(invoices.debitNoteAmounts(invoiceId)).thenReturn(Arrays.asList(new BigDecimal("5"), null));
        when(payments.allocatedAmount(invoiceId)).thenReturn(new BigDecimal("90"));
    }
    @Test
    void should_preserve_notes_surplus_and_operation_order_for_manual_return() {
        when(repository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoices.requireById(invoiceId)).thenReturn(new ReturnInvoiceQueryGateway.InvoiceSnapshot("INV-01", false, new BigDecimal("80"), true));
        var useCase = new CreateReturnUseCase(adjustments, repository, mapper, invoices, payments, balances, inventory, cash);
        useCase.execute(request("MANUAL"), creator, "Operator");
        var ordered = inOrder(cash, inventory, adjustments, invoices, payments, balances);
        ordered.verify(cash).record(any());
        ordered.verify(inventory).restoreStock(any());
        ordered.verify(adjustments).apply(any());
        ordered.verify(invoices).requireById(invoiceId);
        ordered.verify(payments).allocatedAmount(invoiceId);
        ordered.verify(invoices).creditNoteAmounts(invoiceId);
        ordered.verify(invoices).debitNoteAmounts(invoiceId);
        ordered.verify(balances).credit(argThat(command -> command.amount().compareTo(new BigDecimal("15")) == 0
                && command.customerId().equals(customerId) && command.invoiceNumber().equals("INV-01") && command.actorId().equals(creator)));
        verify(repository, times(1)).save(any());
        verify(payments, never()).cancelInvoicePayments(any());
    }
    @Test
    void should_clamp_negative_effective_total_before_crediting_surplus() {
        when(repository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoices.requireById(invoiceId)).thenReturn(new ReturnInvoiceQueryGateway.InvoiceSnapshot("INV-01", false, new BigDecimal("2"), true));
        new CreateReturnUseCase(adjustments, repository, mapper, invoices, payments, balances, inventory, cash).execute(request("MANUAL"), creator, "Operator");
        verify(balances).credit(argThat(command -> command.amount().compareTo(new BigDecimal("90")) == 0));
    }
    @Test
    void should_reverse_before_restoring_payments_and_debit_using_return_creator_identity() {
        Return returned = returned();
        when(repository.findById(returned.getId())).thenReturn(Optional.of(returned));
        when(invoices.findById(invoiceId)).thenReturn(Optional.of(new ReturnInvoiceQueryGateway.InvoiceSnapshot("INV-01", false, new BigDecimal("80"), true)));
        UUID deletingActor = UUID.randomUUID();
        new DeleteReturnUseCase(adjustments, repository, invoices, inventory, payments, balances).execute(returned.getId().value(), deletingActor, "Deleting Operator");
        var ordered = inOrder(inventory, adjustments, payments, balances, repository);
        ordered.verify(inventory).reverseStock(any());
        ordered.verify(payments).allocatedAmount(invoiceId);
        ordered.verify(adjustments).reverse(returned, new BigDecimal("90"), new BigDecimal("5"), new BigDecimal("10"));
        ordered.verify(payments).restore(returned.getCancelledAllocations());
        ordered.verify(payments).allocatedAmount(invoiceId);
        ordered.verify(balances).debit(argThat(command -> command.amount().compareTo(new BigDecimal("15")) == 0
                && command.actorId().equals(creator) && !command.actorId().equals(deletingActor)
                && command.returnNumber().equals("REV-" + returned.getReturnNumber().value())));
        ordered.verify(repository).delete(returned.getId());
    }
    private CreateReturnRequest request(String type) {
        return new CreateReturnRequest(invoiceId, customerId, type, "Returned item", List.of(new CreateReturnRequest.ReturnLineRequest(
                UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE, new BigDecimal("20"), new BigDecimal("20"), null, List.of())));
    }
    private Return returned() {
        var line = ReturnLine.create(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE, new BigDecimal("20"), new BigDecimal("20"), List.of());
        return Return.reconstitute(ReturnId.generate(), ReturnNumber.of(2026, 1), invoiceId, customerId, ReturnType.MANUAL, "Returned item",
                new BigDecimal("20"), List.of(line), List.of(), creator, "Creator", LocalDateTime.now(), LocalDateTime.now());
    }
}
