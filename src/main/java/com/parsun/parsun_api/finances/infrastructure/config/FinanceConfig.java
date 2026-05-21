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
import com.parsun.parsun_api.finances.domain.services.PaymentAllocationService;
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
    public ApplyCreditNoteUseCase applyCreditNoteUseCase(CreditNoteRepository creditNoteRepository) {
        return new ApplyCreditNoteUseCase(creditNoteRepository);
    }

    @Bean
    public CancelCreditNoteUseCase cancelCreditNoteUseCase(CreditNoteRepository creditNoteRepository) {
        return new CancelCreditNoteUseCase(creditNoteRepository);
    }

    @Bean
    public GetCreditNotesUseCase getCreditNotesUseCase(CreditNoteRepository creditNoteRepository) {
        return new GetCreditNotesUseCase(creditNoteRepository);
    }

    @Bean
    public CreateDebitNoteUseCase createDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        return new CreateDebitNoteUseCase(debitNoteRepository);
    }

    @Bean
    public ApplyDebitNoteUseCase applyDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        return new ApplyDebitNoteUseCase(debitNoteRepository);
    }

    @Bean
    public CancelDebitNoteUseCase cancelDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        return new CancelDebitNoteUseCase(debitNoteRepository);
    }

    @Bean
    public GetDebitNotesUseCase getDebitNotesUseCase(DebitNoteRepository debitNoteRepository) {
        return new GetDebitNotesUseCase(debitNoteRepository);
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
}
