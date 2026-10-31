package com.cashly.cashly_api.finance.billing.invoices.application.issue.architecture;

import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceIssuanceArchitectureTest {
    private static final Path APPLICATION = Path.of(
            "src/main/java/com/cashly/cashly_api/finance/billing/invoices/application");
    private static final Pattern NON_CODE = Pattern.compile(
            "\"(?:\\\\.|[^\"\\\\])*\"|/\\*[\\s\\S]*?\\*/|//[^\\r\\n]*");
    private static final Pattern CONFIRM = Pattern
            .compile("\\.\\s*confirm\\s*\\(");

    @Test
    void only_shared_issuance_may_transition_an_invoice_to_confirmed()
            throws IOException {
        try (var paths = Files.walk(APPLICATION)) {
            var sources = paths
                    .filter(path -> path.toString().endsWith(".java")).toList();
            assertThat(sources)
                    .as("Guard must inspect production application sources")
                    .isNotEmpty();
            var violations = new java.util.ArrayList<Path>();
            for (var source : sources) {
                if (!source.startsWith(APPLICATION.resolve("issue"))
                        && containsConfirmation(Files.readString(source))) {
                    violations.add(APPLICATION.relativize(source));
                }
            }
            assertThat(violations).as(
                    "Invoice confirmation must have one shared orchestration owner")
                    .isEmpty();
        }
    }

    @Test
    void detects_a_duplicated_confirmation_even_with_whitespace() {
        assertThat(containsConfirmation("invoice . confirm ( );")).isTrue();
    }

    @Test
    void ignores_confirmation_mentions_in_comments_and_messages() {
        assertThat(containsConfirmation(
                "// invoice.confirm();\nString text = \"invoice.confirm()\";"))
                .isFalse();
    }

    @Test
    void validation_does_not_apply_financial_or_inventory_writes()
            throws IOException {
        String validator = Files.readString(APPLICATION
                .resolve("issue/validation/InvoiceIssueValidator.java"));
        assertThat(containsMutation(validator)).as(
                "Validation may check conditions but must not apply effects")
                .isFalse();
    }

    @Test
    void detects_a_financial_write_hidden_in_a_validator() {
        assertThat(containsMutation("balances.consumeInvoiceBalance(command);"))
                .isTrue();
    }

    private static boolean containsMutation(String source) {
        return Pattern.compile(
                "\\.\\s*(?:consumeInvoiceBalance|settleCashInvoice|recordInvoice|save|deduct|confirmInvoice)\\s*\\(")
                .matcher(NON_CODE.matcher(source).replaceAll(" ")).find();
    }

    private static boolean containsConfirmation(String source) {
        return CONFIRM.matcher(NON_CODE.matcher(source).replaceAll(" ")).find();
    }
}
