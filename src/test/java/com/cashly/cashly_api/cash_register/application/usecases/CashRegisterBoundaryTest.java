package com.cashly.cashly_api.cash_register.application.usecases;

import com.cashly.cashly_api.cash_register.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cash_register.application.services.CashMovementService;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class CashRegisterBoundaryTest {
    @Test
    void should_keep_domain_repository_free_of_application_reporting_types() {
        var dependencies = Arrays.stream(CashRegisterRepository.class.getMethods())
                .map(method -> method.getGenericReturnType().getTypeName())
                .filter(type -> type.contains(".application.") || type.contains("org.springframework")).toList();
        assertTrue(dependencies.isEmpty(), dependencies.toString());
    }

    @Test
    void should_return_neutral_history_pagination() throws Exception {
        assertEquals("com.cashly.cashly_api.shared.dto.PageResponse", GetRegisterHistoryUseCase.class
                .getMethod("execute", int.class, int.class, String.class, java.time.LocalDate.class,
                        java.time.LocalDate.class).getReturnType().getName());
    }

    @Test
    void should_keep_cash_movement_application_dependencies_inside_cashregister() {
        for (Class<?> type : java.util.List.of(CashMovementService.class, RecordCashMovementUseCase.class)) {
            for (var constructor : type.getConstructors()) {
                for (var parameter : constructor.getParameterTypes()) {
                    assertTrue(parameter.getName().startsWith("com.cashly.cashly_api.cash_register."),
                            type.getSimpleName() + " depends on " + parameter.getName());
                }
            }
        }
    }
}
