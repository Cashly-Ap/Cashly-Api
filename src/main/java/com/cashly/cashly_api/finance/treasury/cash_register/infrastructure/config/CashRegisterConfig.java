package com.cashly.cashly_api.finance.treasury.cash_register.infrastructure.config;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterActorGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterPaymentMethodGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterExcelPort;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterReportPort;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterRangeReportPort;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.close.use_case.CloseRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.excel.use_case.GenerateCashRegisterExcelUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.excel.services.GenerateCashRegisterExcelUseCaseImpl;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.session.use_case.GenerateCashRegisterReportUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.range.use_case.GenerateCashRegisterRangeReportUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.queries.use_case.GetActiveRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.queries.use_case.GetRegisterHistoryUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.open.use_case.OpenRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case.RecordCashMovementUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.excel.CashRegisterExcelGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CashRegisterConfig {

    @Bean
    OpenRegisterUseCase openClosureUseCase(
            CashRegisterRepository closureRepository,
            ActivityNotificationRecorder activityNotifications) {
        return new OpenRegisterUseCase(closureRepository, activityNotifications);
    }

    @Bean
    GetActiveRegisterUseCase getActiveClosureUseCase(
            CashRegisterRepository closureRepository,
            CashRegisterQueryGateway queries) {
        return new GetActiveRegisterUseCase(closureRepository, queries);
    }

    @Bean
    CloseRegisterUseCase closeClosureUseCase(
            CashRegisterRepository closureRepository,
            CashRegisterQueryGateway queries,
            ActivityNotificationRecorder activityNotifications) {
        return new CloseRegisterUseCase(closureRepository, queries, activityNotifications);
    }

    @Bean
    GetRegisterHistoryUseCase getClosureHistoryUseCase(
            CashRegisterRepository closureRepository,
            CashRegisterQueryGateway queries) {
        return new GetRegisterHistoryUseCase(closureRepository, queries);
    }

    @Bean
    CashMovementService cashMovementService(
            CashRegisterRepository registerRepository,
            CashRegisterPaymentMethodGateway paymentMethods) {
        return new CashMovementService(registerRepository, paymentMethods);
    }

    @Bean
    RecordCashMovementUseCase recordCashMovementUseCase(
            CashRegisterRepository registerRepository,
            CashRegisterActorGateway actors,
            CashMovementService movementService,
            ActivityNotificationRecorder activityNotifications) {
        return new RecordCashMovementUseCase(registerRepository, actors,
                movementService, activityNotifications);
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
        return new GenerateCashRegisterReportUseCase(repository, queries,
                reportPort);
    }

    @Bean
    GenerateCashRegisterRangeReportUseCase generateCashRegisterRangeReportUseCase(
            CashRegisterQueryGateway repository,
            CashRegisterRangeReportPort reportPort) {
        return new GenerateCashRegisterRangeReportUseCase(repository,
                reportPort);
    }
}
