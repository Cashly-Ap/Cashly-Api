package com.parsun.parsun_api.returns.infrastructure.config;

import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.returns.application.mappers.ReturnMapper;
import com.parsun.parsun_api.returns.application.usecases.CreateReturnUseCase;
import com.parsun.parsun_api.returns.application.usecases.DeleteReturnUseCase;
import com.parsun.parsun_api.returns.application.usecases.GetAllReturnsUseCase;
import com.parsun.parsun_api.returns.application.usecases.GetReturnByIdUseCase;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
