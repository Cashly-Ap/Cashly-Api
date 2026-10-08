package com.cashly.cashly_api.cashregister.infrastructure.config;

import com.cashly.cashly_api.cashregister.application.contracts.CashRegisterActorGateway;
import com.cashly.cashly_api.cashregister.application.contracts.CashRegisterPaymentMethodGateway;
import com.cashly.cashly_api.cashregister.application.port.CashRegisterExcelPort;
import com.cashly.cashly_api.cashregister.application.port.CashRegisterReportPort;
import com.cashly.cashly_api.cashregister.application.port.CashRegisterRangeReportPort;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.application.usecases.CloseRegisterUseCase;
import com.cashly.cashly_api.cashregister.application.usecases.GenerateCashRegisterExcelUseCase;
import com.cashly.cashly_api.cashregister.application.usecases.GenerateCashRegisterExcelUseCaseImpl;
import com.cashly.cashly_api.cashregister.application.usecases.GenerateCashRegisterReportUseCase;
import com.cashly.cashly_api.cashregister.application.usecases.GenerateCashRegisterRangeReportUseCase;
import com.cashly.cashly_api.cashregister.application.usecases.GetActiveRegisterUseCase;
import com.cashly.cashly_api.cashregister.application.usecases.GetRegisterHistoryUseCase;
import com.cashly.cashly_api.cashregister.application.usecases.OpenRegisterUseCase;
import com.cashly.cashly_api.cashregister.application.usecases.RecordCashMovementUseCase;
import com.cashly.cashly_api.cashregister.application.contracts.CashRegisterQueryGateway;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cashregister.infrastructure.excel.CashRegisterExcelGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CashRegisterConfig {

    @Bean
    OpenRegisterUseCase openClosureUseCase(CashRegisterRepository closureRepository) {
        return new OpenRegisterUseCase(closureRepository);
    }

    @Bean
    GetActiveRegisterUseCase getActiveClosureUseCase(CashRegisterRepository closureRepository, CashRegisterQueryGateway queries) {
        return new GetActiveRegisterUseCase(closureRepository, queries);
    }

    @Bean
    CloseRegisterUseCase closeClosureUseCase(CashRegisterRepository closureRepository, CashRegisterQueryGateway queries) {
        return new CloseRegisterUseCase(closureRepository, queries);
    }

    @Bean
    GetRegisterHistoryUseCase getClosureHistoryUseCase(CashRegisterRepository closureRepository, CashRegisterQueryGateway queries) {
        return new GetRegisterHistoryUseCase(closureRepository, queries);
    }

    @Bean
    CashMovementService cashMovementService(CashRegisterRepository registerRepository,
                                            CashRegisterPaymentMethodGateway paymentMethods) {
        return new CashMovementService(registerRepository, paymentMethods);
    }

    @Bean
    RecordCashMovementUseCase recordCashMovementUseCase(CashRegisterRepository registerRepository,
                                                        CashRegisterActorGateway actors,
                                                        CashMovementService movementService) {
        return new RecordCashMovementUseCase(registerRepository, actors, movementService);
    }

    @Bean
    CashRegisterExcelPort cashRegisterExcelPort() {
        return new CashRegisterExcelGenerator();
    }

    @Bean
    GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase(
            CashRegisterQueryGateway repository,
            CashRegisterExcelPort excelPort) {
        return new GenerateCashRegisterExcelUseCaseImpl(repository, excelPort);
    }

    @Bean
    GenerateCashRegisterReportUseCase generateCashRegisterReportUseCase(
            CashRegisterRepository repository, CashRegisterQueryGateway queries,
            CashRegisterReportPort reportPort) {
        return new GenerateCashRegisterReportUseCase(repository, queries, reportPort);
    }

    @Bean
    GenerateCashRegisterRangeReportUseCase generateCashRegisterRangeReportUseCase(
            CashRegisterQueryGateway repository,
            CashRegisterRangeReportPort reportPort) {
        return new GenerateCashRegisterRangeReportUseCase(repository, reportPort);
    }
}
