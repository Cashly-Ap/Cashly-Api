package com.cashly.cashly_api.reports.finance.application;
import com.cashly.cashly_api.reports.finance.application.ports.out.PaymentPdfPort;
import com.cashly.cashly_api.reports.finance.application.usecases.GeneratePaymentPdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
class PaymentReportsBoundaryTest {
    @Test
    void applicationUsesOwnContractsAndDtos() {
        for (var type : new Class<?>[]{PaymentPdfPort.class, GeneratePaymentPdfUseCaseImpl.class}) {
            Arrays.stream(type.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().contains("cashly_api.payments."), field.toString()));
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> Arrays.stream(method.getGenericParameterTypes()).forEach(parameter -> assertFalse(parameter.getTypeName().contains("cashly_api.payments."), parameter.toString())));
        }
    }
}
