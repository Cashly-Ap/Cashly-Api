package com.cashly.cashly_api.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityStructureTest {
    static final Path SOURCE_ROOT = Path
            .of("src/main/java/com/cashly/cashly_api");
    private static final Path TEST_ROOT = Path
            .of("src/test/java/com/cashly/cashly_api");
    private static final Pattern PACKAGE = Pattern
            .compile("(?m)^package\\s+([a-zA-Z0-9_.]+)\\s*;");
    private static final Map<String, Set<String>> CAPABILITIES = Map.of("auth",
            Set.of("authentication", "token", "user", "role", "department",
                    "authorization", "mfa", "preferences"),
            "warehouses", Set.of("management", "stock", "reporting"),
            "customers",
            Set.of("profile", "balance", "credit", "warehouse_access"),
            "payments", Set.of("collection", "credit", "reporting"),
            "cash_register", Set.of("sessions", "reporting"));
    private static final Set<String> COMPOSITION_SOURCES = Set.of(
            "warehouses/infrastructure/config/WarehouseConfig.java",
            "customers/infrastructure/config/CustomerConfig.java",
            "payments/infrastructure/config/PaymentConfig.java",
            "cash_register/infrastructure/config/CashRegisterConfig.java",
            "cash_register/application/ports/out/CashRegisterQueryGateway.java",
            "cash_register/infrastructure/persistence/adapters/CashRegisterRepositoryAdapter.java");

    @Test
    void should_place_existing_business_capabilities_before_layers()
            throws IOException {
        var paths = javaSources(SOURCE_ROOT).stream()
                .map(SOURCE_ROOT::relativize).toList();
        assertThat(paths.stream()
                .filter(path -> !hasApprovedCapabilityOwner(path)))
                .as("Real capabilities precede layers; only named mixed composition sources stay at module root")
                .isEmpty();
        for (var owner : CAPABILITIES.entrySet()) {
            var present = paths.stream().filter(path -> path.getNameCount() > 2)
                    .filter(path -> BusinessModulePaths.owner(path)
                            .equals(owner.getKey()))
                    .map(path -> BusinessModulePaths.withoutArea(path)
                            .getName(1).toString())
                    .filter(owner.getValue()::contains)
                    .collect(Collectors.toSet());
            assertThat(present).as(
                    "Capability scan for %s must not be empty or incomplete",
                    owner.getKey())
                    .containsExactlyInAnyOrderElementsOf(owner.getValue());
        }
    }

    @Test
    void should_match_declared_packages_to_production_and_test_paths()
            throws IOException {
        var violations = new ArrayList<String>();
        for (Path root : List.of(SOURCE_ROOT, TEST_ROOT)) {
            for (Path source : javaSources(root)) {
                var declaration = PACKAGE.matcher(Files.readString(source));
                Path parentPath = root.relativize(source).getParent();
                String parent = parentPath == null
                        ? ""
                        : "." + parentPath.toString().replace('\\', '.')
                                .replace('/', '.');
                String expected = "com.cashly.cashly_api" + parent;
                if (!declaration.find()
                        || !declaration.group(1).equals(expected)) {
                    violations.add(source + " must declare " + expected);
                }
            }
        }
        assertThat(violations)
                .as("Packages must match paths in both nonempty source roots")
                .isEmpty();
    }

    @Test
    void should_reject_technical_or_unknown_submodule_path_fixtures() {
        assertThat(hasApprovedCapabilityOwner(
                Path.of("warehouses/domain/Warehouse.java"))).isFalse();
        assertThat(hasApprovedCapabilityOwner(
                Path.of("customers/application/create/CreateCustomer.java")))
                .isFalse();
        assertThat(hasApprovedCapabilityOwner(
                Path.of("payments/unknown/application/Collect.java")))
                .isFalse();
        assertThat(hasApprovedCapabilityOwner(
                Path.of("cash_register/infrastructure/UnreviewedAdapter.java")))
                .isFalse();
    }

    @Test
    void should_accept_read_only_capabilities_and_explicit_composition_sources() {
        assertThat(hasApprovedCapabilityOwner(Path
                .of("warehouses/stock/application/queries/StockSummary.java")))
                .isTrue();
        assertThat(hasApprovedCapabilityOwner(Path.of(
                "customers/warehouse_access/application/grant/GrantAccess.java")))
                .isTrue();
        assertThat(hasApprovedCapabilityOwner(Path.of(
                "cash_register/reporting/application/queries/Summary.java")))
                .isTrue();
        assertThat(hasApprovedCapabilityOwner(Path.of(
                "products/application/photos/UploadProductPhotoUseCase.java")))
                .isTrue();
        assertThat(hasApprovedCapabilityOwner(Path
                .of("warehouses/infrastructure/config/WarehouseConfig.java")))
                .isTrue();
    }

    static List<Path> javaSources(Path root) throws IOException {
        assertThat(root).as("Architecture source root must exist")
                .isDirectory();
        try (var files = Files.walk(root)) {
            var sources = files
                    .filter(path -> path.toString().endsWith(".java")).sorted()
                    .toList();
            assertThat(sources)
                    .as("Architecture source scan must not be empty: %s", root)
                    .isNotEmpty();
            return sources;
        }
    }

    private boolean hasApprovedCapabilityOwner(Path path) {
        path = BusinessModulePaths.withoutArea(path);
        String normalized = path.toString().replace('\\', '/');
        Set<String> capabilities = CAPABILITIES.get(path.getName(0).toString());
        return capabilities == null || COMPOSITION_SOURCES.contains(normalized)
                || (path.getNameCount() > 2
                        && capabilities.contains(path.getName(1).toString()));
    }
}
