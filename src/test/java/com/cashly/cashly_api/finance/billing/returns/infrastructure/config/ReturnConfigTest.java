package com.cashly.cashly_api.finance.billing.returns.infrastructure.config;

import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInventoryGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceQueryGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnPaymentSettlementGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnBalanceGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnCashGateway;
import com.cashly.cashly_api.finance.billing.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.finance.billing.returns.application.create.use_case.CreateReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.delete.use_case.DeleteReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.list.use_case.GetAllReturnsUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.get.use_case.GetReturnByIdUseCase;
import com.cashly.cashly_api.finance.billing.returns.domain.repository.ReturnRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnConfig Unit Tests")
class ReturnConfigTest {

    @Mock private ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway;
    @Mock private ReturnRepository returnRepository;
    @Mock private ReturnResponseAssembler returnMapper;
    @Mock private ReturnInventoryGateway inventoryGateway;
    @Mock private ReturnInvoiceQueryGateway invoices;
    @Mock private ReturnPaymentSettlementGateway payments;
    @Mock private ReturnBalanceGateway balances;
    @Mock private ReturnCashGateway cash;

    @Test
    void should_CreateCreateReturnUseCase() {
        ReturnConfig config = new ReturnConfig(returnRepository);

        CreateReturnUseCase useCase = config.createReturnUseCase(invoiceAdjustmentGateway, returnMapper, invoices, payments, balances, inventoryGateway, cash);

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

        DeleteReturnUseCase useCase = config.deleteReturnUseCase(invoiceAdjustmentGateway, invoices, inventoryGateway, payments, balances);

        assertNotNull(useCase);
    }
}
