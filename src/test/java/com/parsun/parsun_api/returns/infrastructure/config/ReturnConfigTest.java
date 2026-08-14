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
import com.parsun.parsun_api.returns.application.usecases.*;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnConfig Unit Tests")
class ReturnConfigTest {

    @Mock private ReturnRepository returnRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ReturnMapper returnMapper;
    @Mock private PaymentAllocationService paymentAllocationService;
    @Mock private PaymentAllocationRepository allocationRepository;
    @Mock private AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;
    @Mock private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private DebitNoteRepository debitNoteRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private WarehouseStockRepository warehouseStockRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private CashMovementService cashMovementService;

    @Test
    void should_CreateCreateReturnUseCase() {
        ReturnConfig config = new ReturnConfig(returnRepository);

        CreateReturnUseCase useCase = config.createReturnUseCase(
                invoiceRepository, returnMapper, paymentAllocationService,
                allocationRepository, addToCustomerBalanceUseCase,
                creditNoteRepository, debitNoteRepository,
                warehouseStockRepository, stockMovementRepository, cashMovementService);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetAllReturnsUseCase() {
        ReturnConfig config = new ReturnConfig(returnRepository);

        GetAllReturnsUseCase useCase = config.getAllReturnsUseCase(returnMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetReturnByIdUseCase() {
        ReturnConfig config = new ReturnConfig(returnRepository);

        GetReturnByIdUseCase useCase = config.getReturnByIdUseCase(returnMapper);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateDeleteReturnUseCase() {
        ReturnConfig config = new ReturnConfig(returnRepository);

        DeleteReturnUseCase useCase = config.deleteReturnUseCase(
                invoiceRepository, warehouseStockRepository, stockMovementRepository,
                consumeCustomerBalanceUseCase, paymentRepository, allocationRepository,
                creditNoteRepository, debitNoteRepository);

        assertNotNull(useCase);
    }
}
