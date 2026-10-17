package com.cashly.cashly_api.finance.treasury.payments.infrastructure.config;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentActorGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.create.use_case.CreatePaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.delete.use_case.DeletePaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case.GetAllPaymentsUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case.GetPaymentByIdUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.update.use_case.UpdatePaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
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
    @Mock private PaymentCustomerGateway customerGateway;
    @Mock private PaymentActorGateway actorGateway;
    @Mock private CashRegisterGateway cashRegisterGateway;

    @Test
    void should_CreateCreatePaymentUseCase() {
        PaymentConfig config = new PaymentConfig();

        CreatePaymentUseCase useCase = config.createPaymentUseCase(
                paymentRepository, customerGateway, actorGateway, cashRegisterGateway);

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
