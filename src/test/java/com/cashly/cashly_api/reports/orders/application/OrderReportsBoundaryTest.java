package com.cashly.cashly_api.reports.orders.application;
import com.cashly.cashly_api.reports.orders.application.ports.out.OrderPdfPort;
import com.cashly.cashly_api.reports.orders.application.ports.out.OrdersReportPdfPort;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrderPdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
class OrderReportsBoundaryTest {
    @Test
    void applicationDoesNotDependOnForeignDtosRepositoriesOrAggregates() {
        for (var type : new Class<?>[]{OrderPdfPort.class, OrdersReportPdfPort.class, GenerateOrderPdfUseCaseImpl.class}) {
            Arrays.stream(type.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().matches(".*cashly_api\\.(orders|warehouses)\\..*"), field.toString()));
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> Arrays.stream(method.getGenericParameterTypes()).forEach(parameter -> assertFalse(parameter.getTypeName().contains("cashly_api.orders."), parameter.toString())));
        }
    }
}