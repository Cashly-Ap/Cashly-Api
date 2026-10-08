package com.cashly.cashly_api.payments.infrastructure.config;

import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.application.usecases.*;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.application.contracts.CustomerCreditContextGateway;
import com.cashly.cashly_api.payments.application.contracts.InvoiceCreditLedgerGateway;
import com.cashly.cashly_api.payments.application.contracts.CreditNoteLedgerGateway;
import com.cashly.cashly_api.payments.application.contracts.DebitNoteLedgerGateway;
import com.cashly.cashly_api.payments.application.contracts.CashRegisterGateway;
import com.cashly.cashly_api.payments.application.contracts.PaymentActorGateway;
import com.cashly.cashly_api.payments.application.contracts.PaymentCustomerGateway;
import com.cashly.cashly_api.payments.application.contracts.PaymentInvoiceGateway;
import com.cashly.cashly_api.payments.application.contracts.PaymentNotificationGateway;
import com.cashly.cashly_api.payments.application.api.CustomerCreditQuery;
import com.cashly.cashly_api.payments.application.services.CustomerCreditQueryService;
import com.cashly.cashly_api.payments.domain.services.PaymentAllocationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Bean
    public CreatePaymentUseCase createPaymentUseCase(PaymentRepository paymentRepository,
                                                       PaymentCustomerGateway customerGateway,
                                                       PaymentActorGateway actorGateway,
                                                       CashRegisterGateway cashRegisterGateway) {
        return new CreatePaymentUseCase(paymentRepository, customerGateway, actorGateway, cashRegisterGateway);
    }

    @Bean
    public GetAllPaymentsUseCase getAllPaymentsUseCase(PaymentRepository paymentRepository) {
        return new GetAllPaymentsUseCase(paymentRepository);
    }

    @Bean
    public GetPaymentByIdUseCase getPaymentByIdUseCase(PaymentRepository paymentRepository) {
        return new GetPaymentByIdUseCase(paymentRepository);
    }

    @Bean
    public UpdatePaymentUseCase updatePaymentUseCase(PaymentRepository paymentRepository) {
        return new UpdatePaymentUseCase(paymentRepository);
    }

    @Bean
    public DeletePaymentUseCase deletePaymentUseCase(PaymentRepository paymentRepository) {
        return new DeletePaymentUseCase(paymentRepository);
    }

    @Bean
    public PaymentAllocationService paymentAllocationService(PaymentRepository paymentRepository,
                                                               PaymentAllocationRepository paymentAllocationRepository) {
        return new PaymentAllocationService(paymentRepository, paymentAllocationRepository);
    }

    @Bean
    public PayInvoiceUseCase payInvoiceUseCase(PaymentRepository paymentRepository,
                                                PaymentAllocationRepository paymentAllocationRepository,
                                                PaymentInvoiceGateway invoiceGateway,
                                                PaymentCustomerGateway customerGateway,
                                                CashRegisterGateway cashRegisterGateway) {
        return new PayInvoiceUseCase(paymentRepository, paymentAllocationRepository,
                invoiceGateway, customerGateway, cashRegisterGateway);
    }

    @Bean
    public PayAllCustomerDebtUseCase payAllCustomerDebtUseCase(PaymentRepository paymentRepository,
                                                                PaymentAllocationRepository paymentAllocationRepository,
                                                                PaymentInvoiceGateway invoiceGateway,
                                                                PaymentCustomerGateway customerGateway,
                                                                CashRegisterGateway cashRegisterGateway,
                                                                PaymentAllocationService paymentAllocationService) {
        return new PayAllCustomerDebtUseCase(paymentRepository, paymentAllocationRepository,
                invoiceGateway, customerGateway, cashRegisterGateway, paymentAllocationService);
    }

    @Bean
    public PayPartialDebtUseCase payPartialDebtUseCase(PaymentRepository paymentRepository,
                                                        PaymentAllocationRepository paymentAllocationRepository,
                                                        PaymentInvoiceGateway invoiceGateway,
                                                        PaymentCustomerGateway customerGateway,
                                                        CashRegisterGateway cashRegisterGateway,
                                                        PaymentAllocationService paymentAllocationService) {
        return new PayPartialDebtUseCase(paymentRepository, paymentAllocationRepository,
                invoiceGateway, customerGateway, cashRegisterGateway, paymentAllocationService);
    }

    @Bean
    public ApprovePaymentUseCase approvePaymentUseCase(PaymentRepository paymentRepository,
                                                         PaymentAllocationRepository allocationRepository,
                                                         PaymentInvoiceGateway invoiceGateway,
                                                         PaymentNotificationGateway notificationGateway,
                                                         CashRegisterGateway cashRegisterGateway,
                                                         PaymentAllocationService paymentAllocationService) {
        return new ApprovePaymentUseCase(paymentRepository, allocationRepository, invoiceGateway,
                notificationGateway, cashRegisterGateway, paymentAllocationService);
    }

    @Bean
    public RejectPaymentUseCase rejectPaymentUseCase(PaymentRepository paymentRepository,
                                                       PaymentNotificationGateway notificationGateway) {
        return new RejectPaymentUseCase(paymentRepository, notificationGateway);
    }

    @Bean
    public CustomerCreditQuery customerCreditQuery(
            CustomerCreditContextGateway customers,
            PaymentRepository payments, PaymentAllocationRepository allocations,
            InvoiceCreditLedgerGateway invoices,
            CreditNoteLedgerGateway credits,
            DebitNoteLedgerGateway debits) {
        return new CustomerCreditQueryService(customers, payments, allocations, invoices, credits, debits);
    }
}
