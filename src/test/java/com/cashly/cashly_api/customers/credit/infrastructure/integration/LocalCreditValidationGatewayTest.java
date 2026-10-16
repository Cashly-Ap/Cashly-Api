package com.cashly.cashly_api.customers.credit.infrastructure.integration;
import com.cashly.cashly_api.payments.credit.application.ports.in.CustomerCreditQuery;
import com.cashly.cashly_api.payments.credit.application.ports.in.CreditStatusSnapshot;
import com.cashly.cashly_api.payments.credit.application.ports.in.OverdueInvoiceSnapshot;
import com.cashly.cashly_api.customers.credit.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.customers.credit.application.services.CustomerCreditEligibilityService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class LocalCreditValidationGatewayTest {
    private final CustomerCreditQuery query = mock(CustomerCreditQuery.class);
    private final LocalCreditValidationGateway gateway = new LocalCreditValidationGateway(query);
    private final UUID id = UUID.randomUUID();
    @Test
    void should_preserve_every_status_field_and_overdue_entry() {
        LocalDate due = LocalDate.now().minusDays(4);
        var overdue = new OverdueInvoiceSnapshot("INV-01", new BigDecimal("150"), due, 4);
        when(query.getCreditStatus(id)).thenReturn(new CreditStatusSnapshot(id, "Customer", "RIF",
                new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("200"), new BigDecimal("700"), List.of(overdue), true, false));
        var result = gateway.getCreditStatus(id);
        assertThat(result.customerId()).isEqualTo(id);
        assertThat(result.customerName()).isEqualTo("Customer");
        assertThat(result.customerRif()).isEqualTo("RIF");
        assertThat(result.creditLimit()).isEqualByComparingTo("1000");
        assertThat(result.pendingBalance()).isEqualByComparingTo("500");
        assertThat(result.customerBalance()).isEqualByComparingTo("200");
        assertThat(result.availableCredit()).isEqualByComparingTo("700");
        assertThat(result.hasOverdue()).isTrue();
        assertThat(result.canCreateCreditInvoice()).isFalse();
        assertThat(result.overdueInvoices()).containsExactly(new OverdueInvoiceResponse("INV-01", new BigDecimal("150"), due, 4));
    }
    @Test
    void should_preserve_global_overdue_query_order() {
        LocalDate due = LocalDate.now().minusDays(1);
        when(query.findAllOverdueInvoices()).thenReturn(List.of(new OverdueInvoiceSnapshot("INV-02", BigDecimal.TEN, due, 1)));
        assertThat(gateway.findAllOverdueInvoices()).containsExactly(new OverdueInvoiceResponse("INV-02", BigDecimal.TEN, due, 1));
        verify(query).findAllOverdueInvoices();
    }
    @Test
    void should_propagate_provider_error_without_changing_message() {
        when(query.getCreditStatus(id)).thenThrow(new IllegalArgumentException("Customer not found: " + id));
        assertThatThrownBy(() -> gateway.getCreditStatus(id)).isInstanceOf(IllegalArgumentException.class).hasMessage("Customer not found: " + id);
    }
    @Test
    void should_keep_credit_eligibility_and_limits_through_gateway() {
        when(query.getCreditStatus(id)).thenReturn(new CreditStatusSnapshot(id, "Customer", "RIF", new BigDecimal("1000"),
                new BigDecimal("600"), BigDecimal.ZERO, new BigDecimal("400"), List.of(), false, true));
        var eligibility = new CustomerCreditEligibilityService(gateway);
        assertThat(eligibility.getCreditLimits(id).creditLimit()).isEqualByComparingTo("1000");
        assertThat(eligibility.getCreditLimits(id).availableCredit()).isEqualByComparingTo("400");
        assertThat(eligibility.isCreditAvailable(id, new BigDecimal("400"))).isTrue();
        assertThat(eligibility.isCreditAvailable(id, new BigDecimal("400.01"))).isFalse();
    }
}
