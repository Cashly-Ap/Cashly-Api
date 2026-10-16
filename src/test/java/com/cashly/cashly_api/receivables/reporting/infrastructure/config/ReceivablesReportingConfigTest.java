package com.cashly.cashly_api.receivables.reporting.infrastructure.config;

import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableQueryGateway;
import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableReportPdfPort;
import com.cashly.cashly_api.receivables.reporting.application.ports.out.CollectionsReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ReceivablesReportingConfigTest {

    @Mock
    private AccountsReceivableQueryGateway receivablesRepository;

    @Mock
    private AccountsReceivableReportPdfPort accountsReceivableReportPdfPort;

    @Mock
    private CollectionsReportPdfPort collectionsReportPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new ReceivablesReportingConfig();


        var arUseCase = config.generateAccountsReceivableReportPdfUseCase(receivablesRepository, accountsReceivableReportPdfPort);
        assertNotNull(arUseCase);


        var collUseCase = config.generateCollectionsReportPdfUseCase(collectionsReportPdfPort);
        assertNotNull(collUseCase);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new ReceivablesReportingConfig();

        assertNotNull(config.accountsReceivableReportPdfPort());
        assertNotNull(config.collectionsReportPdfPort());
    }
}
