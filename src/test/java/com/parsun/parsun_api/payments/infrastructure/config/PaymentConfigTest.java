package com.parsun.parsun_api.payments.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.payments.application.usecases.*;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentConfig Unit Tests")
class PaymentConfigTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentAllocationRepository paymentAllocationRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceStatusService invoiceStatusService;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private DebitNoteRepository debitNoteRepository;
    @Mock private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;
    @Mock private CustomerBalanceRepository customerBalanceRepository;

    @Test
    void should_CreateCreatePaymentUseCase() {
        PaymentConfig config = new PaymentConfig();

        CreatePaymentUseCase useCase = config.createPaymentUseCase(
                paymentRepository, customerRepository, userRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetAllPaymentsUseCase() {
        PaymentConfig config = new PaymentConfig();

        GetAllPaymentsUseCase useCase = config.getAllPaymentsUseCase(paymentRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetPaymentByIdUseCase() {
        PaymentConfig config = new PaymentConfig();

        GetPaymentByIdUseCase useCase = config.getPaymentByIdUseCase(paymentRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateUpdatePaymentUseCase() {
        PaymentConfig config = new PaymentConfig();

        UpdatePaymentUseCase useCase = config.updatePaymentUseCase(paymentRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateDeletePaymentUseCase() {
        PaymentConfig config = new PaymentConfig();

        DeletePaymentUseCase useCase = config.deletePaymentUseCase(paymentRepository);

        assertNotNull(useCase);
    }
}
