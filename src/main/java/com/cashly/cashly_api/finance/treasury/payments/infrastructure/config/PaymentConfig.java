package com.cashly.cashly_api.finance.treasury.payments.infrastructure.config;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.approve.use_case.ApprovePaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.create.use_case.CreatePaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.delete.use_case.DeletePaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case.GetAllPaymentsUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case.GetPaymentByIdUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_customer_debt.use_case.PayAllCustomerDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_invoice.use_case.PayInvoiceUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_partial_debt.use_case.PayPartialDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.reject.use_case.RejectPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.update.use_case.UpdatePaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CustomerCreditContextGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.InvoiceCreditLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.DebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentActorGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentNotificationGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in.CustomerCreditQuery;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.services.CustomerCreditQueryService;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
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
