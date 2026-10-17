package com.cashly.cashly_api.shared.application.architecture;

import org.junit.jupiter.api.Test;
import com.cashly.cashly_api.architecture.BusinessModulePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ResponsibilityStructureTest {
    private static final Path MAIN = Path.of("src/main/java/com/cashly/cashly_api");
    private static final Path TEST = Path.of("src/test/java/com/cashly/cashly_api");
    private static final Set<String> DOMAIN_ROLES = Set.of(
            "entity", "repository", "valueobjects", "services", "exceptions", "architecture", "fixtures");
    private static final Set<String> APPLICATION_ROLES = Set.of(
            "use_case", "request", "response", "services", "validation", "model", "mappers", "exceptions",
            "architecture", "fixtures");

    @Test
    void production_sources_have_explicit_responsibility_folders() throws IOException {
        assertThat(sources(MAIN).stream().filter(path -> !hasResponsibility(path)).toList())
                .as("Production modules, layers and operations contain responsibility folders, not loose classes")
                .isEmpty();
    }

    @Test
    void test_sources_have_explicit_responsibility_folders() throws IOException {
        assertThat(sources(TEST).stream().filter(path -> !hasResponsibility(path)).toList())
                .as("Tests follow the same responsibility folders; architecture and fixtures are explicit roles")
                .isEmpty();
    }

    @Test
    void tests_of_named_production_classes_stay_under_the_production_responsibility() throws IOException {
        List<Path> production = sources(MAIN);
        var violations = new ArrayList<Path>();
        for (Path test : sources(TEST)) {
            String type = test.getFileName().toString().replaceFirst("(?:UnitTest|Tests|Test|IT)\\.java$", ".java");
            var matches = production.stream().filter(path -> BusinessModulePaths.owner(path).equals(BusinessModulePaths.owner(test)))
                    .filter(path -> path.getFileName().toString().equals(type)).toList();
            if (!matches.isEmpty() && matches.stream().noneMatch(path -> test.getParent().startsWith(path.getParent()))) {
                violations.add(test);
            }
        }
        assertThat(violations).as("Tests mirror their production responsibility, optionally with a narrower test role").isEmpty();
    }

    @Test
    void rejects_loose_module_layer_and_operation_classes() {
        assertThat(List.of("orders/Order.java", "orders/domain/Order.java",
                        "orders/application/create/CreateOrderUseCase.java",
                        "orders/application/create/from_quote/QuoteOrderRequest.java",
                        "orders/infrastructure/OrderController.java",
                        "orders/infrastructure/persistence/OrderEntity.java")
                .stream().map(Path::of).filter(ResponsibilityStructureTest::hasResponsibility)).isEmpty();
    }

    @Test
    void accepts_concrete_responsibilities_and_nested_origins() {
        assertThat(List.of("orders/domain/entity/Order.java", "orders/domain/repository/OrderRepository.java",
                        "orders/application/create/use_case/CreateOrderUseCase.java",
                        "orders/application/create/from_quote/request/QuoteOrderRequest.java",
                        "orders/application/ports/out/InvoiceCreationGateway.java",
                        "orders/infrastructure/persistence/entities/OrderEntity.java",
                        "orders/infrastructure/integration_tests/SalesMySqlIT.java",
                        "shared/application/architecture/ResponsibilityStructureTest.java",
                        "CashlyApplication.java")
                .stream().map(Path::of).filter(path -> !hasResponsibility(path))).isEmpty();
    }

    private static boolean hasResponsibility(Path path) {
        List<String> parts = List.of(path.toString().replace('\\', '/').split("/"));
        if (parts.size() == 1) {
            return parts.getFirst().equals("CashlyApplication.java");
        }
        if (parts.getFirst().equals("architecture")) {
            return true;
        }
        for (String layer : List.of("domain", "application", "infrastructure")) {
            int index = parts.indexOf(layer);
            if (index < 0) {
                continue;
            }
            if (parts.size() <= index + 2) {
                return false;
            }
            String role = parts.get(parts.size() - 2);
            if (layer.equals("domain")) {
                return DOMAIN_ROLES.contains(parts.get(index + 1));
            }
            if (layer.equals("application")) {
                return APPLICATION_ROLES.contains(role)
                        || ((role.equals("in") || role.equals("out"))
                        && parts.size() > 3 && parts.get(parts.size() - 3).equals("ports"));
            }
            return !role.equals("persistence");
        }
        return parts.size() >= 3;
    }

    private static List<Path> sources(Path root) throws IOException {
        assertThat(root).isDirectory();
        try (var paths = Files.walk(root)) {
            var sources = paths.filter(path -> path.toString().endsWith(".java"))
                    .map(root::relativize).sorted().toList();
            assertThat(sources).as("Structural scan must inspect real sources under %s", root).isNotEmpty();
            return sources;
        }
    }
}
