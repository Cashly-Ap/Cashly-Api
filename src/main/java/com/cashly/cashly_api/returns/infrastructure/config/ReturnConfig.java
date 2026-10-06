package com.cashly.cashly_api.returns.infrastructure.config;

import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.returns.application.usecases.CreateReturnUseCase;
import com.cashly.cashly_api.returns.application.usecases.DeleteReturnUseCase;
import com.cashly.cashly_api.returns.application.usecases.GetAllReturnsUseCase;
import com.cashly.cashly_api.returns.application.usecases.GetReturnByIdUseCase;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReturnConfig {

    private final ReturnRepository returnRepository;

    public ReturnConfig(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Bean
    public CreateReturnUseCase createReturnUseCase(InvoiceRepository invoiceRepository,
                                                          ReturnMapper returnMapper,
                                                          PaymentAllocationService paymentAllocationService,
                                                          PaymentAllocationRepository allocationRepository,
                                                          AddToCustomerBalanceUseCase addToCustomerBalanceUseCase,
                                                          CreditNoteRepository creditNoteRepository,
                                                          DebitNoteRepository debitNoteRepository,
                                                          WarehouseStockRepository warehouseStockRepository,
                                                          StockMovementRepository stockMovementRepository,
                                                          CashMovementService cashMovementService) {
        return new CreateReturnUseCase(returnRepository, invoiceRepository, returnMapper,
                paymentAllocationService, allocationRepository, addToCustomerBalanceUseCase,
                creditNoteRepository, debitNoteRepository, warehouseStockRepository, stockMovementRepository,
                cashMovementService);
    }

    @Bean
    public GetAllReturnsUseCase getAllReturnsUseCase(ReturnMapper returnMapper) {
        return new GetAllReturnsUseCase(returnRepository, returnMapper);
    }

    @Bean
    public GetReturnByIdUseCase getReturnByIdUseCase(ReturnMapper returnMapper) {
        return new GetReturnByIdUseCase(returnRepository, returnMapper);
    }

    @Bean
    public DeleteReturnUseCase deleteReturnUseCase(InvoiceRepository invoiceRepository,
                                                     WarehouseStockRepository warehouseStockRepository,
                                                     StockMovementRepository stockMovementRepository,
                                                     ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                                                     PaymentRepository paymentRepository,
                                                     PaymentAllocationRepository allocationRepository,
                                                     CreditNoteRepository creditNoteRepository,
                                                     DebitNoteRepository debitNoteRepository) {
        return new DeleteReturnUseCase(returnRepository, invoiceRepository,
                warehouseStockRepository, stockMovementRepository,
                consumeCustomerBalanceUseCase, paymentRepository, allocationRepository,
                creditNoteRepository, debitNoteRepository);
    }
}
