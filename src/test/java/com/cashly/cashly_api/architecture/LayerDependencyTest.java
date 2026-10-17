package com.cashly.cashly_api.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class LayerDependencyTest {
    private static final Pattern REFERENCES = Pattern.compile(
            "\\bcom\\.cashly\\.cashly_api\\.([a-z_]+)(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)+(?:\\.\\*)?");
    private static final Pattern NON_CODE = Pattern.compile(
            "\"\"\"[\\s\\S]*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|/\\*[\\s\\S]*?\\*/|//[^\\r\\n]*");
    private static final Pattern FRAMEWORK = Pattern.compile("\\b(?:org\\.springframework|jakarta\\.persistence)\\.");

    @Test
    void domain_should_be_independent_of_application_infrastructure_and_frameworks() throws IOException {
        assertLayerSources("domain", this::domainLayerViolations);
    }

    @Test
    void application_output_ports_should_be_independent_of_infrastructure_and_frameworks() throws IOException {
        assertLayerSources("application/ports/out", this::portViolations);
    }

    @Test
    void application_input_ports_should_be_independent_of_infrastructure_and_frameworks() throws IOException {
        assertLayerSources("application/ports/in", this::portViolations);
    }

    @Test
    void application_should_depend_on_other_modules_only_through_input_ports() throws IOException {
        assertLayerSources("application", this::applicationOwnershipViolations);
    }

    @Test
    void application_should_not_depend_on_infrastructure() throws IOException {
        assertLayerSources("application", (path, source) -> references(source).stream()
                .filter(type -> type.contains(".infrastructure.")).toList());
    }

    @Test
    void domain_should_not_depend_on_other_modules_internals() throws IOException {
        assertLayerSources("domain", this::domainOwnershipViolations);
    }

    @Test
    void should_detect_forbidden_imports_and_fully_qualified_references_in_nested_capabilities() {
        Path stock = Path.of("warehouses/stock/application/transfer/Transfer.java");
        assertThat(applicationOwnershipViolations(stock,
                "import com.cashly.cashly_api.customers.profile.domain.entity.Customer;"))
                .containsExactly("com.cashly.cashly_api.customers.profile.domain.entity.Customer");
        assertThat(applicationOwnershipViolations(stock,
                "private com.cashly.cashly_api.customers.profile.infrastructure.persistence.CustomerEntity customer;"))
                .containsExactly("com.cashly.cashly_api.customers.profile.infrastructure.persistence.CustomerEntity");
        assertThat(domainLayerViolations(Path.of("warehouses/stock/domain/Stock.java"),
                "import org.springframework.stereotype.Service;")).isNotEmpty();
        assertThat(domainLayerViolations(Path.of("warehouses/stock/domain/Stock.java"),
                "import static com.cashly.cashly_api.inventory.warehouses.stock.application.Helpers.value;"))
                .containsExactly("com.cashly.cashly_api.inventory.warehouses.stock.application.Helpers.value");
        assertThat(portViolations(stock, "import jakarta.persistence.Entity;")).isNotEmpty();
        assertThat(domainOwnershipViolations(Path.of("warehouses/stock/domain/Stock.java"),
                "import com.cashly.cashly_api.catalog.products.domain.entity.Product;"))
                .containsExactly("com.cashly.cashly_api.catalog.products.domain.entity.Product");
    }

    @Test
    void erp_area_grouping_does_not_allow_access_to_sibling_module_internals() {
        assertThat(applicationOwnershipViolations(Path.of("orders/application/create/use_case/Create.java"),
                "import com.cashly.cashly_api.quotes.domain.entity.Quote;"))
                .containsExactly("com.cashly.cashly_api.quotes.domain.entity.Quote");
        assertThat(domainOwnershipViolations(Path.of("finance/billing/invoices/domain/entity/Invoice.java"),
                "import com.cashly.cashly_api.finance.billing.credit_notes.domain.entity.CreditNote;"))
                .containsExactly("com.cashly.cashly_api.finance.billing.credit_notes.domain.entity.CreditNote");
        assertThat(applicationOwnershipViolations(Path.of("orders/application/create/use_case/Create.java"),
                "import com.cashly.cashly_api.quotes.application.ports.in.QuoteQuery;"))
                .isEmpty();
    }

    @Test
    void should_allow_provider_input_ports_sibling_capabilities_and_shared_pagination() {
        Path stock = Path.of("warehouses/stock/application/queries/Availability.java");
        String source = """
                import com.cashly.cashly_api.customers.warehouse_access.application.ports.in.AccessQuery;
                import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
                import com.cashly.cashly_api.shared.dto.PageResponse;
                // import com.cashly.cashly_api.customers.profile.domain.entity.Customer;
                String example = "com.cashly.cashly_api.customers.profile.infrastructure.CustomerEntity";
                """;
        assertThat(applicationOwnershipViolations(stock, source)).isEmpty();
        assertThat(domainOwnershipViolations(Path.of("warehouses/stock/domain/Stock.java"),
                "import com.cashly.cashly_api.shared.dto.PageResponse;")).isEmpty();
        assertThat(portViolations(stock,
                "import com.cashly.cashly_api.customers.warehouse_access.application.ports.in.AccessQuery;")).isEmpty();
    }

    private void assertLayerSources(String layer, DependencyRule rule) throws IOException {
        var paths = CapabilityStructureTest.javaSources(CapabilityStructureTest.SOURCE_ROOT).stream()
                .filter(path -> path.toString().replace('\\', '/').contains("/" + layer + "/")).toList();
        assertThat(paths).as("Layer guard must inspect sources at every capability depth: %s", layer).isNotEmpty();
        var violations = new ArrayList<String>();
        for (Path path : paths) {
            Path relative = CapabilityStructureTest.SOURCE_ROOT.relativize(path);
            rule.violations(relative, Files.readString(path)).forEach(type -> violations.add(relative + ": " + type));
        }
        assertThat(violations).as("Dependencies must point inward through their owning contracts").isEmpty();
    }

    private List<String> domainLayerViolations(Path path, String source) {
        var violations = new ArrayList<>(references(source).stream()
                .filter(type -> type.contains(".application.") || type.contains(".infrastructure.")).toList());
        if (FRAMEWORK.matcher(codeOnly(source)).find()) {
            violations.add("framework dependency");
        }
        return violations;
    }

    private List<String> portViolations(Path path, String source) {
        var violations = new ArrayList<>(references(source).stream()
                .filter(type -> type.contains(".infrastructure.")).toList());
        if (FRAMEWORK.matcher(codeOnly(source)).find()) {
            violations.add("framework dependency");
        }
        return violations;
    }

    private List<String> applicationOwnershipViolations(Path path, String source) {
        String owner = BusinessModulePaths.owner(path);
        return references(source).stream().filter(type -> !moduleOf(type).equals(owner))
                .filter(type -> !type.contains(".application.ports.in."))
                .filter(type -> type.contains(".domain.") || type.contains(".application.") || type.contains(".infrastructure."))
                .toList();
    }

    private List<String> domainOwnershipViolations(Path path, String source) {
        String owner = BusinessModulePaths.owner(path);
        return references(source).stream().filter(type -> !moduleOf(type).equals(owner))
                .filter(type -> !type.equals("com.cashly.cashly_api.shared.dto.PageResponse")
                        && !type.startsWith("com.cashly.cashly_api.shared.dto.PageResponse."))
                .toList();
    }

    private List<String> references(String source) {
        return REFERENCES.matcher(codeOnly(source)).results().map(result -> result.group()).distinct().toList();
    }

    private String moduleOf(String reference) {
        return BusinessModulePaths.ownerOfReference(reference);
    }

    private String codeOnly(String source) {
        return NON_CODE.matcher(source).replaceAll(" ");
    }

    @FunctionalInterface
    private interface DependencyRule {
        List<String> violations(Path source, String contents);
    }
}
