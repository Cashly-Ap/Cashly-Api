package com.cashly.cashly_api.reports.finance.infrastructure.config;

import com.cashly.cashly_api.reports.finance.application.ports.out.PaymentReportQueryGateway;
import com.cashly.cashly_api.reports.finance.application.ports.out.AccountsReceivableQueryGateway;
import com.cashly.cashly_api.reports.finance.application.ports.out.AccountsReceivableReportPdfPort;
import com.cashly.cashly_api.reports.finance.application.ports.out.CollectionsReportPdfPort;
import com.cashly.cashly_api.reports.finance.application.ports.out.PaymentHistoryReportPdfPort;
import com.cashly.cashly_api.reports.finance.application.ports.out.PaymentPdfPort;
import com.cashly.cashly_api.reports.finance.application.usecases.GenerateAccountsReceivableReportPdfUseCase;
import com.cashly.cashly_api.reports.finance.application.usecases.GenerateCollectionsReportPdfUseCase;
import com.cashly.cashly_api.reports.finance.application.usecases.GeneratePaymentHistoryReportPdfUseCase;
import com.cashly.cashly_api.reports.finance.application.usecases.GeneratePaymentPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancesReportConfig Unit Tests")
class FinancesReportConfigTest {

    @Mock
    private PaymentReportQueryGateway getPaymentByIdUseCase;

    @Mock
    private PaymentPdfPort paymentPdfPort;

    @Mock
    private AccountsReceivableQueryGateway receivablesRepository;

    @Mock
    private AccountsReceivableReportPdfPort accountsReceivableReportPdfPort;

    @Mock
    private PaymentHistoryReportPdfPort paymentHistoryReportPdfPort;

    @Mock
    private CollectionsReportPdfPort collectionsReportPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new FinancesReportConfig();

        var paymentPdfUseCase = config.generatePaymentPdfUseCase(getPaymentByIdUseCase, paymentPdfPort);
        assertNotNull(paymentPdfUseCase);

        var arUseCase = config.generateAccountsReceivableReportPdfUseCase(receivablesRepository, accountsReceivableReportPdfPort);
        assertNotNull(arUseCase);

        var phUseCase = config.generatePaymentHistoryReportPdfUseCase(paymentHistoryReportPdfPort);
        assertNotNull(phUseCase);

        var collUseCase = config.generateCollectionsReportPdfUseCase(collectionsReportPdfPort);
        assertNotNull(collUseCase);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new FinancesReportConfig();

        assertNotNull(config.paymentPdfPort());
        assertNotNull(config.accountsReceivableReportPdfPort());
        assertNotNull(config.paymentHistoryReportPdfPort());
        assertNotNull(config.collectionsReportPdfPort());
    }
}
