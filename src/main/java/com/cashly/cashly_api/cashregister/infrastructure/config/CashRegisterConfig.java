package com.cashly.cashly_api.cashregister.infrastructure.config;

import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
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
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cashregister.infrastructure.excel.CashRegisterExcelGenerator;
import com.cashly.cashly_api.payment_methods.domain.repositories.PaymentMethodRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CashRegisterConfig {

    @Bean
    OpenRegisterUseCase openClosureUseCase(CashRegisterRepository closureRepository) {
        return new OpenRegisterUseCase(closureRepository);
    }

    @Bean
    GetActiveRegisterUseCase getActiveClosureUseCase(CashRegisterRepository closureRepository) {
        return new GetActiveRegisterUseCase(closureRepository);
    }

    @Bean
    CloseRegisterUseCase closeClosureUseCase(CashRegisterRepository closureRepository) {
        return new CloseRegisterUseCase(closureRepository);
    }

    @Bean
    GetRegisterHistoryUseCase getClosureHistoryUseCase(CashRegisterRepository closureRepository) {
        return new GetRegisterHistoryUseCase(closureRepository);
    }

    @Bean
    CashMovementService cashMovementService(CashRegisterRepository registerRepository,
                                            PaymentMethodRepository paymentMethodRepository) {
        return new CashMovementService(registerRepository, paymentMethodRepository);
    }

    @Bean
    RecordCashMovementUseCase recordCashMovementUseCase(CashRegisterRepository registerRepository,
                                                        UserRepository userRepository,
                                                        CashMovementService movementService) {
        return new RecordCashMovementUseCase(registerRepository, userRepository, movementService);
    }

    @Bean
    CashRegisterExcelPort cashRegisterExcelPort() {
        return new CashRegisterExcelGenerator();
    }

    @Bean
    GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase(
            CashRegisterRepository repository,
            CashRegisterExcelPort excelPort) {
        return new GenerateCashRegisterExcelUseCaseImpl(repository, excelPort);
    }

    @Bean
    GenerateCashRegisterReportUseCase generateCashRegisterReportUseCase(
            CashRegisterRepository repository,
            CashRegisterReportPort reportPort) {
        return new GenerateCashRegisterReportUseCase(repository, reportPort);
    }

    @Bean
    GenerateCashRegisterRangeReportUseCase generateCashRegisterRangeReportUseCase(
            CashRegisterRepository repository,
            CashRegisterRangeReportPort reportPort) {
        return new GenerateCashRegisterRangeReportUseCase(repository, reportPort);
    }
}
