package com.cashly.cashly_api.finance.billing.invoices.application.architecture;

import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.InvoicePaymentService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.confirm.use_case.ConfirmInvoiceUseCase;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class InvoiceAuthorizationBoundaryTest {
    @Test
    void should_orchestrate_authorization_through_invoice_owned_gateways() {
        var dependencies = Arrays
                .stream(InvoiceIssueValidator.class.getConstructors()[0]
                        .getParameterTypes())
                .map(Class::getName).toList();
        assertTrue(
                dependencies.stream().allMatch(type -> type.startsWith(
                        "com.cashly.cashly_api.finance.billing.invoices.")),
                dependencies.toString());
    }
    @Test
    void should_confirm_without_foreign_repositories_or_concrete_services() {
        var dependencies = Arrays
                .stream(ConfirmInvoiceUseCase.class.getConstructors()[0]
                        .getParameterTypes())
                .map(Class::getName).toList();
        assertTrue(
                dependencies.stream().allMatch(type -> type.startsWith(
                        "com.cashly.cashly_api.finance.billing.invoices.")),
                dependencies.toString());
    }
    @Test
    void should_authorize_without_foreign_entities_in_method_signature() {
        assertTrue(Arrays
                .stream(InvoiceIssueValidator.class.getDeclaredMethods())
                .flatMap(method -> Arrays.stream(method.getParameterTypes()))
                .noneMatch(type -> type.getName()
                        .startsWith("com.cashly.cashly_api.platform.auth.")));
    }
}
