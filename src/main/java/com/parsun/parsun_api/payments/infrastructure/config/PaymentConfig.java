package com.parsun.parsun_api.payments.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
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
                                                      UserRepository userRepository) {
        return new CreatePaymentUseCase(paymentRepository, customerRepository, userRepository);
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
    public CreditValidationService creditValidationService(CustomerRepository customerRepository,
                                                            PaymentRepository paymentRepository,
                                                            PaymentAllocationRepository paymentAllocationRepository,
                                                            InvoiceRepository invoiceRepository,
                                                            CreditNoteRepository creditNoteRepository,
                                                            DebitNoteRepository debitNoteRepository) {
        return new CreditValidationService(customerRepository, paymentRepository, paymentAllocationRepository,
                invoiceRepository, creditNoteRepository, debitNoteRepository);
    }
}
