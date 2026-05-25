package com.parsun.parsun_api.finances.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.finances.application.usecases.*;
import com.parsun.parsun_api.finances.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.finances.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.finances.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.*;
import com.parsun.parsun_api.finances.domain.services.CreditValidationService;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories.SpringDataWarehouseRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories.SpringDataWarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinanceConfig {

    @Bean
    public CreatePaymentMethodUseCase createPaymentMethodUseCase(PaymentMethodRepository repository,
                                                                 PaymentMethodJpaRepository jpaRepository) {
        return new CreatePaymentMethodUseCase(repository, jpaRepository);
    }

    @Bean
    public UpdatePaymentMethodUseCase updatePaymentMethodUseCase(PaymentMethodRepository repository) {
        return new UpdatePaymentMethodUseCase(repository);
    }

    @Bean
    public ListPaymentMethodsUseCase listPaymentMethodsUseCase(PaymentMethodRepository repository) {
        return new ListPaymentMethodsUseCase(repository);
    }

    @Bean
    public GetPaymentMethodByIdUseCase getPaymentMethodByIdUseCase(PaymentMethodRepository repository) {
        return new GetPaymentMethodByIdUseCase(repository);
    }

    @Bean
    public TogglePaymentMethodUseCase togglePaymentMethodUseCase(PaymentMethodRepository repository) {
        return new TogglePaymentMethodUseCase(repository);
    }

    @Bean
    public DeletePaymentMethodUseCase deletePaymentMethodUseCase(PaymentMethodRepository repository) {
        return new DeletePaymentMethodUseCase(repository);
    }

    @Bean
    public PaymentAllocationService paymentAllocationService(PaymentRepository paymentRepository,
                                                             PaymentAllocationRepository allocationRepository) {
        return new PaymentAllocationService(paymentRepository, allocationRepository);
    }

    @Bean
    public RegisterPaymentUseCase registerPaymentUseCase(PaymentRepository paymentRepository,
                                                         PaymentAllocationRepository allocationRepository,
                                                         CustomerRepository customerRepository,
                                                         UserRepository userRepository,
                                                         PaymentMethodJpaRepository paymentMethodJpaRepository) {
        return new RegisterPaymentUseCase(paymentRepository, allocationRepository, customerRepository,
                userRepository, paymentMethodJpaRepository);
    }

    @Bean
    public ApplyPaymentToInvoicesUseCase applyPaymentToInvoicesUseCase(PaymentRepository paymentRepository,
                                                                       PaymentAllocationRepository allocationRepository,
                                                                       PaymentAllocationService allocationService,
                                                                       PaymentMethodJpaRepository paymentMethodJpaRepository) {
        return new ApplyPaymentToInvoicesUseCase(paymentRepository, allocationRepository,
                allocationService, paymentMethodJpaRepository);
    }

    @Bean
    public RecordAdvanceUseCase recordAdvanceUseCase(PaymentRepository paymentRepository,
                                                     PaymentAllocationRepository allocationRepository,
                                                     CustomerRepository customerRepository,
                                                     UserRepository userRepository,
                                                     PaymentMethodJpaRepository paymentMethodJpaRepository) {
        return new RecordAdvanceUseCase(paymentRepository, allocationRepository, customerRepository,
                userRepository, paymentMethodJpaRepository);
    }

    @Bean
    public ApplyAdvanceToInvoiceUseCase applyAdvanceToInvoiceUseCase(PaymentRepository paymentRepository,
                                                                     PaymentAllocationRepository allocationRepository,
                                                                     PaymentAllocationService allocationService,
                                                                     PaymentMethodJpaRepository paymentMethodJpaRepository) {
        return new ApplyAdvanceToInvoiceUseCase(paymentRepository, allocationRepository,
                allocationService, paymentMethodJpaRepository);
    }

    @Bean
    public CancelPaymentUseCase cancelPaymentUseCase(PaymentRepository paymentRepository,
                                                     PaymentAllocationRepository allocationRepository,
                                                     PaymentAllocationService allocationService,
                                                     PaymentMethodJpaRepository paymentMethodJpaRepository) {
        return new CancelPaymentUseCase(paymentRepository, allocationRepository,
                allocationService, paymentMethodJpaRepository);
    }

    @Bean
    public GetPaymentsUseCase getPaymentsUseCase(PaymentRepository paymentRepository,
                                                 PaymentMethodJpaRepository paymentMethodJpaRepository) {
        return new GetPaymentsUseCase(paymentRepository, paymentMethodJpaRepository);
    }

    @Bean
    public GetPaymentByIdUseCase getPaymentByIdUseCase(PaymentRepository paymentRepository,
                                                       PaymentAllocationRepository allocationRepository,
                                                       PaymentMethodJpaRepository paymentMethodJpaRepository) {
        return new GetPaymentByIdUseCase(paymentRepository, allocationRepository, paymentMethodJpaRepository);
    }

    @Bean
    public CreditValidationService creditValidationService(CustomerRepository customerRepository,
                                                           PaymentRepository paymentRepository,
                                                           PaymentAllocationRepository paymentAllocationRepository,
                                                           InvoiceRepository invoiceRepository) {
        return new CreditValidationService(customerRepository, paymentRepository, paymentAllocationRepository, invoiceRepository);
    }

    @Bean
    public GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase(CreditValidationService creditValidationService) {
        return new GetCustomerCreditStatusUseCase(creditValidationService);
    }

    @Bean
    public GetOverdueInvoicesUseCase getOverdueInvoicesUseCase(CreditValidationService creditValidationService) {
        return new GetOverdueInvoicesUseCase(creditValidationService);
    }

    @Bean
    public CreateCreditNoteUseCase createCreditNoteUseCase(CreditNoteRepository creditNoteRepository) {
        return new CreateCreditNoteUseCase(creditNoteRepository);
    }

    @Bean
    public ApplyCreditNoteUseCase applyCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                                         InvoiceJpaRepository invoiceJpaRepository) {
        return new ApplyCreditNoteUseCase(creditNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public CancelCreditNoteUseCase cancelCreditNoteUseCase(CreditNoteRepository creditNoteRepository,
                                                           InvoiceJpaRepository invoiceJpaRepository) {
        return new CancelCreditNoteUseCase(creditNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public DeleteCreditNoteUseCase deleteCreditNoteUseCase(CreditNoteRepository creditNoteRepository) {
        return new DeleteCreditNoteUseCase(creditNoteRepository);
    }

    @Bean
    public GetCreditNotesUseCase getCreditNotesUseCase(CreditNoteRepository creditNoteRepository,
                                                       InvoiceJpaRepository invoiceJpaRepository) {
        return new GetCreditNotesUseCase(creditNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public GetCreditNoteByIdUseCase getCreditNoteByIdUseCase(CreditNoteRepository creditNoteRepository,
                                                              InvoiceJpaRepository invoiceJpaRepository) {
        return new GetCreditNoteByIdUseCase(creditNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public CreateDebitNoteUseCase createDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        return new CreateDebitNoteUseCase(debitNoteRepository);
    }

    @Bean
    public ApplyDebitNoteUseCase applyDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                                       InvoiceJpaRepository invoiceJpaRepository) {
        return new ApplyDebitNoteUseCase(debitNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public CancelDebitNoteUseCase cancelDebitNoteUseCase(DebitNoteRepository debitNoteRepository,
                                                         InvoiceJpaRepository invoiceJpaRepository) {
        return new CancelDebitNoteUseCase(debitNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public DeleteDebitNoteUseCase deleteDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        return new DeleteDebitNoteUseCase(debitNoteRepository);
    }

    @Bean
    public GetDebitNotesUseCase getDebitNotesUseCase(DebitNoteRepository debitNoteRepository,
                                                     InvoiceJpaRepository invoiceJpaRepository) {
        return new GetDebitNotesUseCase(debitNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public GetDebitNoteByIdUseCase getDebitNoteByIdUseCase(DebitNoteRepository debitNoteRepository,
                                                           InvoiceJpaRepository invoiceJpaRepository) {
        return new GetDebitNoteByIdUseCase(debitNoteRepository, invoiceJpaRepository);
    }

    @Bean
    public CreateReturnUseCase createReturnUseCase(ReturnRepository returnRepository,
                                                   InvoiceRepository invoiceRepository,
                                                   StockMovementRepository stockMovementRepository,
                                                   SpringDataWarehouseStockRepository warehouseStockJpaRepository,
                                                   CreditNoteRepository creditNoteRepository) {
        return new CreateReturnUseCase(returnRepository, invoiceRepository, stockMovementRepository,
                warehouseStockJpaRepository, creditNoteRepository);
    }

    @Bean
    public CancelReturnUseCase cancelReturnUseCase(ReturnRepository returnRepository,
                                                   ReturnLineJpaRepository returnLineJpaRepository,
                                                   StockMovementRepository stockMovementRepository,
                                                   SpringDataWarehouseStockRepository warehouseStockJpaRepository) {
        return new CancelReturnUseCase(returnRepository, returnLineJpaRepository, stockMovementRepository,
                warehouseStockJpaRepository);
    }

    @Bean
    public GetReturnsUseCase getReturnsUseCase(ReturnRepository returnRepository,
                                               InvoiceJpaRepository invoiceJpaRepository) {
        return new GetReturnsUseCase(returnRepository, invoiceJpaRepository);
    }

    @Bean
    public GetReturnByIdUseCase getReturnByIdUseCase(ReturnRepository returnRepository,
                                                      ReturnLineJpaRepository returnLineJpaRepository,
                                                      InvoiceJpaRepository invoiceJpaRepository,
                                                      SpringDataWarehouseRepository warehouseJpaRepository) {
        return new GetReturnByIdUseCase(returnRepository, returnLineJpaRepository, invoiceJpaRepository,
                warehouseJpaRepository);
    }

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
    public GetPendingPaymentsUseCase getPendingPaymentsUseCase(ReceivablesRepository receivablesRepository) {
        return new GetPendingPaymentsUseCase(receivablesRepository);
    }

    @Bean
    public GetMerchantPaymentsUseCase getMerchantPaymentsUseCase(ReceivablesRepository receivablesRepository) {
        return new GetMerchantPaymentsUseCase(receivablesRepository);
    }

    @Bean
    public GetPaymentHistoryUseCase getPaymentHistoryUseCase(ReceivablesRepository receivablesRepository) {
        return new GetPaymentHistoryUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerPaymentHistoryUseCase getCustomerPaymentHistoryUseCase(ReceivablesRepository receivablesRepository) {
        return new GetCustomerPaymentHistoryUseCase(receivablesRepository);
    }

    @Bean
    public PayInvoiceUseCase payInvoiceUseCase(PaymentRepository paymentRepository,
                                               PaymentAllocationRepository allocationRepository,
                                               InvoiceRepository invoiceRepository,
                                               FileStorageService fileStorageService) {
        return new PayInvoiceUseCase(paymentRepository, allocationRepository, invoiceRepository, fileStorageService);
    }

    @Bean
    public PayAllCustomerDebtUseCase payAllCustomerDebtUseCase(PaymentRepository paymentRepository,
                                                               PaymentAllocationRepository allocationRepository,
                                                               InvoiceRepository invoiceRepository,
                                                               FileStorageService fileStorageService) {
        return new PayAllCustomerDebtUseCase(paymentRepository, allocationRepository, invoiceRepository, fileStorageService);
    }

    @Bean
    public PayPartialDebtUseCase payPartialDebtUseCase(PaymentRepository paymentRepository,
                                                       PaymentAllocationRepository allocationRepository,
                                                       InvoiceRepository invoiceRepository,
                                                       FileStorageService fileStorageService) {
        return new PayPartialDebtUseCase(paymentRepository, allocationRepository, invoiceRepository, fileStorageService);
    }

    @Bean
    public ApprovePaymentUseCase approvePaymentUseCase(PaymentRepository paymentRepository) {
        return new ApprovePaymentUseCase(paymentRepository);
    }

    @Bean
    public RejectPaymentUseCase rejectPaymentUseCase(PaymentRepository paymentRepository) {
        return new RejectPaymentUseCase(paymentRepository);
    }
}
