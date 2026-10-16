package com.cashly.cashly_api.customers.balance.application.services;
import com.cashly.cashly_api.customers.balance.application.ports.in.InvoiceBalanceConsumption;
import com.cashly.cashly_api.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.customers.balance.domain.entity.BalanceTransaction;
import com.cashly.cashly_api.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.balance.domain.valueobjects.BalanceType;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class InvoiceBalanceConsumptionServiceTest {
    private final CustomerBalanceRepository balances = mock(CustomerBalanceRepository.class);
    private final InvoiceBalanceConsumptionService service = new InvoiceBalanceConsumptionService(new ConsumeCustomerBalanceUseCase(balances));
    private final InvoiceBalanceConsumption.Command command = new InvoiceBalanceConsumption.Command(UUID.randomUUID(), new BigDecimal("200"),
            UUID.randomUUID(), "INV-2026-0001", UUID.randomUUID());
    @Test
    void should_consume_balance_and_preserve_all_invoice_reference_fields() {
        var balance = CustomerBalance.create(command.customerId());
        balance.addToBalance(new BigDecimal("750"));
        when(balances.findByCustomerId(command.customerId())).thenReturn(Optional.of(balance));
        service.consume(command);
        assertThat(balance.getTotalBalance()).isEqualByComparingTo("550");
        verify(balances).save(balance);
        var transaction = ArgumentCaptor.forClass(BalanceTransaction.class);
        verify(balances).saveTransaction(transaction.capture());
        assertThat(transaction.getValue().getCustomerId()).isEqualTo(command.customerId());
        assertThat(transaction.getValue().getType()).isEqualTo(BalanceType.INVOICES);
        assertThat(transaction.getValue().getAmount()).isEqualByComparingTo("-200");
        assertThat(transaction.getValue().getReferenceType()).isEqualTo(BalanceType.INVOICES);
        assertThat(transaction.getValue().getReferenceId()).isEqualTo(command.invoiceId());
        assertThat(transaction.getValue().getNumber()).isEqualTo(command.invoiceNumber());
        assertThat(transaction.getValue().getReferenceNumber()).isNull();
        assertThat(transaction.getValue().getCreatedBy()).isEqualTo(command.actorId());
    }
    @Test
    void should_preserve_missing_balance_error_without_writes() {
        assertThatThrownBy(() -> service.consume(command)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Customer balance not found for customer id: " + command.customerId());
        verify(balances, never()).save(any());
        verify(balances, never()).saveTransaction(any());
    }
}
