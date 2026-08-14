package com.parsun.parsun_api.payments.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.payments.application.usecases.*;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.services.CreditValidationService;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Bean
    public CreatePaymentUseCase createPaymentUseCase(PaymentRepository paymentRepository,
                                                       CustomerRepository customerRepository,
                                                       UserRepository userRepository,
                                                       CashMovementService cashMovementService) {
        return new CreatePaymentUseCase(paymentRepository, customerRepository, userRepository,
                cashMovementService);
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
                                                InvoiceRepository invoiceRepository,
                                                InvoiceStatusService invoiceStatusService,
                                                CreditNoteRepository creditNoteRepository,
                                                DebitNoteRepository debitNoteRepository,
                                                ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                                                CashMovementService cashMovementService) {
        return new PayInvoiceUseCase(paymentRepository, paymentAllocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository,
                consumeCustomerBalanceUseCase, cashMovementService);
    }

    @Bean
    public PayAllCustomerDebtUseCase payAllCustomerDebtUseCase(PaymentRepository paymentRepository,
                                                                PaymentAllocationRepository paymentAllocationRepository,
                                                                InvoiceRepository invoiceRepository,
                                                                InvoiceStatusService invoiceStatusService,
                                                                CreditNoteRepository creditNoteRepository,
                                                                DebitNoteRepository debitNoteRepository,
                                                                ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                                                                CashMovementService cashMovementService) {
        return new PayAllCustomerDebtUseCase(paymentRepository, paymentAllocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository,
                consumeCustomerBalanceUseCase, cashMovementService);
    }

    @Bean
    public PayPartialDebtUseCase payPartialDebtUseCase(PaymentRepository paymentRepository,
                                                        PaymentAllocationRepository paymentAllocationRepository,
                                                        InvoiceRepository invoiceRepository,
                                                        InvoiceStatusService invoiceStatusService,
                                                        CreditNoteRepository creditNoteRepository,
                                                        DebitNoteRepository debitNoteRepository,
                                                        ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                                                        CashMovementService cashMovementService) {
        return new PayPartialDebtUseCase(paymentRepository, paymentAllocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository,
                consumeCustomerBalanceUseCase, cashMovementService);
    }

    @Bean
    public ApprovePaymentUseCase approvePaymentUseCase(PaymentRepository paymentRepository,
                                                         PaymentAllocationRepository allocationRepository,
                                                         InvoiceRepository invoiceRepository,
                                                         InvoiceStatusService invoiceStatusService,
                                                         NotificationService notificationService,
                                                         CashMovementService cashMovementService) {
        return new ApprovePaymentUseCase(paymentRepository, allocationRepository, invoiceRepository,
                invoiceStatusService, notificationService, cashMovementService);
    }

    @Bean
    public RejectPaymentUseCase rejectPaymentUseCase(PaymentRepository paymentRepository,
                                                       NotificationService notificationService) {
        return new RejectPaymentUseCase(paymentRepository, notificationService);
    }

    @Bean
    public CreditValidationService creditValidationService(CustomerRepository customerRepository,
                                                             CustomerBalanceRepository customerBalanceRepository,
                                                             PaymentRepository paymentRepository,
                                                             PaymentAllocationRepository paymentAllocationRepository,
                                                             InvoiceRepository invoiceRepository,
                                                             CreditNoteRepository creditNoteRepository,
                                                             DebitNoteRepository debitNoteRepository) {
        return new CreditValidationService(customerRepository, customerBalanceRepository,
                paymentRepository, paymentAllocationRepository,
                invoiceRepository, creditNoteRepository, debitNoteRepository);
    }
}
