package com.cashly.cashly_api.receivables.infrastructure.config;

import com.cashly.cashly_api.receivables.application.usecases.GetCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetCustomerInvoicesUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetPaymentHistoryUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetReceivablesSummaryUseCase;
import com.cashly.cashly_api.receivables.application.usecases.SearchCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.domain.repositories.ReceivablesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceivablesConfig Unit Tests")
class ReceivablesConfigTest {

    @Mock
    private ReceivablesRepository receivablesRepository;

    @Test
    void should_CreateGetReceivablesSummaryUseCase() {
        ReceivablesConfig config = new ReceivablesConfig();

        GetReceivablesSummaryUseCase useCase =
                config.getReceivablesSummaryUseCase(receivablesRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateSearchCustomerDebtUseCase() {
        ReceivablesConfig config = new ReceivablesConfig();

        SearchCustomerDebtUseCase useCase =
                config.searchCustomerDebtUseCase(receivablesRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetCustomerDebtUseCase() {
        ReceivablesConfig config = new ReceivablesConfig();

        GetCustomerDebtUseCase useCase =
                config.getCustomerDebtUseCase(receivablesRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetCustomerInvoicesUseCase() {
        ReceivablesConfig config = new ReceivablesConfig();

        GetCustomerInvoicesUseCase useCase =
                config.getCustomerInvoicesUseCase(receivablesRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetPaymentHistoryUseCase() {
        ReceivablesConfig config = new ReceivablesConfig();

        GetPaymentHistoryUseCase useCase =
                config.getPaymentHistoryUseCase(receivablesRepository);

        assertNotNull(useCase);
    }
}
