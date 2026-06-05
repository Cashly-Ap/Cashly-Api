package com.parsun.parsun_api.receivables.infrastructure.config;

import com.parsun.parsun_api.receivables.application.usecases.*;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.shared.services.CreditValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceivablesConfig {

    @Bean
    public GetReceivablesSummaryUseCase getReceivablesSummaryUseCase(ReceivablesRepository receivablesRepository) {
        return new GetReceivablesSummaryUseCase(receivablesRepository);
    }

    @Bean
    public SearchCustomerDebtUseCase searchCustomerDebtUseCase(ReceivablesRepository receivablesRepository) {
        return new SearchCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerDebtUseCase getCustomerDebtUseCase(ReceivablesRepository receivablesRepository) {
        return new GetCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerInvoicesUseCase getCustomerInvoicesUseCase(ReceivablesRepository receivablesRepository) {
        return new GetCustomerInvoicesUseCase(receivablesRepository);
    }

    @Bean
    public PayInvoiceUseCase payInvoiceUseCase(PaymentRepository paymentRepository,
                                                PaymentAllocationRepository paymentAllocationRepository,
                                                InvoiceRepository invoiceRepository,
                                                InvoiceStatusService invoiceStatusService,
                                                CreditNoteRepository creditNoteRepository,
                                                DebitNoteRepository debitNoteRepository) {
        return new PayInvoiceUseCase(paymentRepository, paymentAllocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository);
    }

    @Bean
    public PayAllCustomerDebtUseCase payAllCustomerDebtUseCase(PaymentRepository paymentRepository,
                                                                PaymentAllocationRepository paymentAllocationRepository,
                                                                InvoiceRepository invoiceRepository,
                                                                InvoiceStatusService invoiceStatusService,
                                                                CreditNoteRepository creditNoteRepository,
                                                                DebitNoteRepository debitNoteRepository) {
        return new PayAllCustomerDebtUseCase(paymentRepository, paymentAllocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository);
    }

    @Bean
    public PayPartialDebtUseCase payPartialDebtUseCase(PaymentRepository paymentRepository,
                                                        PaymentAllocationRepository paymentAllocationRepository,
                                                        InvoiceRepository invoiceRepository,
                                                        InvoiceStatusService invoiceStatusService,
                                                        CreditNoteRepository creditNoteRepository,
                                                        DebitNoteRepository debitNoteRepository) {
        return new PayPartialDebtUseCase(paymentRepository, paymentAllocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository);
    }

    @Bean
    public GetPaymentHistoryUseCase getPaymentHistoryUseCase(ReceivablesRepository receivablesRepository) {
        return new GetPaymentHistoryUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase(CreditValidationService creditValidationService) {
        return new GetCustomerCreditStatusUseCase(creditValidationService);
    }

    @Bean
    public GetOverdueInvoicesUseCase getOverdueInvoicesUseCase(CreditValidationService creditValidationService) {
        return new GetOverdueInvoicesUseCase(creditValidationService);
    }
}
