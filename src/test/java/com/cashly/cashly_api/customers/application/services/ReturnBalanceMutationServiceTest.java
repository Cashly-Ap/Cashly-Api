package com.cashly.cashly_api.customers.application.services;
import com.cashly.cashly_api.customers.application.ports.in.ReturnBalanceMutation;
import com.cashly.cashly_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.domain.entities.CustomerBalance;
import com.cashly.cashly_api.customers.domain.entities.BalanceTransaction;
import com.cashly.cashly_api.customers.domain.repositories.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.BalanceType;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReturnBalanceMutationServiceTest {
    @Test
    void should_credit_and_debit_real_balance_with_exact_return_reference_metadata() {
        var repository = mock(CustomerBalanceRepository.class);
        var service = new ReturnBalanceMutationService(new AddToCustomerBalanceUseCase(repository), new ConsumeCustomerBalanceUseCase(repository));
        UUID customerId = UUID.randomUUID(); UUID returnId = UUID.randomUUID(); UUID actorId = UUID.randomUUID();
        var balance = CustomerBalance.create(customerId); balance.addToBalance(BigDecimal.TEN);
        when(repository.findByCustomerId(customerId)).thenReturn(Optional.of(balance));
        service.credit(new ReturnBalanceMutation.Command(customerId, new BigDecimal("20"), returnId, "RET-2026-0001", "INV-2026-00001", actorId));
        assertThat(balance.getTotalBalance()).isEqualByComparingTo("30");
        service.debit(new ReturnBalanceMutation.Command(customerId, new BigDecimal("5"), returnId, "REV-RET-2026-0001", "INV-2026-00001", actorId));
        assertThat(balance.getTotalBalance()).isEqualByComparingTo("25");
        var captured = ArgumentCaptor.forClass(BalanceTransaction.class);
        verify(repository, times(2)).saveTransaction(captured.capture());
        assertThat(captured.getAllValues()).extracting(BalanceTransaction::getAmount).containsExactly(new BigDecimal("20"), new BigDecimal("-5"));
        assertThat(captured.getAllValues()).extracting(BalanceTransaction::getNumber).containsExactly("RET-2026-0001", "REV-RET-2026-0001");
        for (var transaction : captured.getAllValues()) {
            assertThat(transaction.getCustomerId()).isEqualTo(customerId);
            assertThat(transaction.getType()).isEqualTo(BalanceType.RETURNS);
            assertThat(transaction.getReferenceType()).isEqualTo(BalanceType.RETURNS);
            assertThat(transaction.getReferenceId()).isEqualTo(returnId);
            assertThat(transaction.getReferenceNumber()).isEqualTo("INV-2026-00001");
            assertThat(transaction.getCreatedBy()).isEqualTo(actorId);
        }
    }
}
