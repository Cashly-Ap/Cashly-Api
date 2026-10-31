package com.cashly.cashly_api.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPackageConventionTest {
    private static final List<String> AMBIGUOUS_PACKAGES = List.of("api",
            "contracts", "port", "query", "usecases");
    private static final List<String> PORT_DIRECTIONS = List.of("in", "out");
    private static final List<String> PERSISTENCE_ROLES = List.of("entities",
            "repositories", "adapters", "queries", "mappers", "projections");

    @Test
    void should_use_cash_register_for_the_cash_register_module_package()
            throws IOException {
        assertThat(productionPaths().stream()
                .filter(path -> segments(path).contains("cashregister")))
                .as("The cash register module package is cash_register")
                .isEmpty();
    }

    @Test
    void should_use_short_infrastructure_paths() throws IOException {
        assertThat(productionPaths().stream()
                .filter(this::hasDirectionalInfrastructureWrapper))
                .as("HTTP, security and output adapters belong directly under infrastructure responsibilities")
                .isEmpty();
    }

    @Test
    void should_group_persistence_sources_by_responsibility()
            throws IOException {
        var paths = productionPaths().stream()
                .filter(path -> segments(path).contains("persistence"))
                .toList();
        assertThat(paths)
                .as("Persistence guard must inspect actual persistence sources")
                .isNotEmpty();
        assertThat(
                paths.stream().filter(this::hasUnclassifiedPersistencePackage))
                .as("Persistence sources require an explicit responsibility package")
                .isEmpty();
    }

    @Test
    void should_keep_only_interfaces_in_persistence_repositories()
            throws IOException, ClassNotFoundException {
        var paths = productionPaths().stream().filter(path -> path.toString()
                .replace('\\', '/').contains("/persistence/repositories/"))
                .toList();
        assertThat(paths).as(
                "Repository guard must inspect actual Spring Data interfaces")
                .isNotEmpty();
        var violations = new ArrayList<Path>();
        for (Path path : paths) {
            String relativeName = path.toString().replace('\\', '.')
                    .replace('/', '.').replaceFirst("\\.java$", "");
            Class<?> type = Class.forName(
                    "com.cashly.cashly_api." + relativeName, false,
                    getClass().getClassLoader());
            if (!type.isInterface()) {
                violations.add(path);
            }
        }
        assertThat(violations).as(
                "Concrete repository implementations belong in persistence/adapters")
                .isEmpty();
    }

    @Test
    void should_use_operation_packages_and_directional_application_ports()
            throws IOException {
        var paths = productionPaths().stream()
                .filter(path -> segments(path).contains("application"))
                .toList();
        assertThat(paths)
                .as("Application guard must inspect nested capability packages")
                .isNotEmpty();
        assertThat(paths.stream().filter(this::hasAmbiguousApplicationPackage))
                .as("Application operations need capability names; public and required ports need in/out direction")
                .isEmpty();
    }

    @Test
    void should_reject_legacy_and_ambiguous_path_fixtures() {
        assertThat(hasDirectionalInfrastructureWrapper(Path.of(
                "warehouses/stock/infrastructure/adapters/out/persistence/adapters/StockAdapter.java")))
                .isTrue();
        assertThat(hasDirectionalInfrastructureWrapper(Path.of(
                "auth/authentication/infrastructure/adapters/in/security/Filter.java")))
                .isTrue();
        assertThat(hasAmbiguousApplicationPackage(
                Path.of("customers/credit/application/ports/Gateway.java")))
                .isTrue();
        assertThat(hasAmbiguousApplicationPackage(
                Path.of("payments/collection/application/usecases/Pay.java")))
                .isTrue();
        assertThat(hasUnclassifiedPersistencePackage(Path.of(
                "warehouses/stock/infrastructure/persistence/StockEntity.java")))
                .isTrue();
    }

    @Test
    void should_accept_nested_capabilities_and_existing_output_roles() {
        assertThat(hasDirectionalInfrastructureWrapper(Path.of(
                "warehouses/stock/infrastructure/web/StockController.java")))
                .isFalse();
        assertThat(hasDirectionalInfrastructureWrapper(Path.of(
                "shared/storage/infrastructure/adapters/LocalFileStorageService.java")))
                .isFalse();
        assertThat(hasAmbiguousApplicationPackage(Path.of(
                "customers/credit/application/ports/in/CustomerCreditQuery.java")))
                .isFalse();
        assertThat(hasAmbiguousApplicationPackage(Path.of(
                "warehouses/stock/application/ports/out/StockCatalogGateway.java")))
                .isFalse();
        assertThat(hasUnclassifiedPersistencePackage(Path.of(
                "payments/collection/infrastructure/persistence/entities/PaymentEntity.java")))
                .isFalse();
    }

    private List<Path> productionPaths() throws IOException {
        return CapabilityStructureTest
                .javaSources(CapabilityStructureTest.SOURCE_ROOT).stream()
                .map(CapabilityStructureTest.SOURCE_ROOT::relativize).toList();
    }

    private List<String> segments(Path path) {
        return List.of(path.toString().replace('\\', '/').split("/"));
    }

    private boolean hasUnclassifiedPersistencePackage(Path path) {
        List<String> parts = segments(path);
        int index = parts.indexOf("persistence");
        return index >= 0 && (index + 1 >= parts.size()
                || !PERSISTENCE_ROLES.contains(parts.get(index + 1)));
    }

    private boolean hasAmbiguousApplicationPackage(Path path) {
        List<String> parts = segments(path);
        int index = parts.indexOf("application");
        if (index < 0 || index + 1 >= parts.size()) {
            return false;
        }
        if (AMBIGUOUS_PACKAGES.contains(parts.get(index + 1))) {
            return true;
        }
        int ports = parts.indexOf("ports");
        return ports > index && (ports + 1 >= parts.size()
                || !PORT_DIRECTIONS.contains(parts.get(ports + 1)));
    }

    private boolean hasDirectionalInfrastructureWrapper(Path path) {
        List<String> parts = segments(path);
        int index = parts.indexOf("infrastructure");
        return index >= 0 && index + 2 < parts.size()
                && parts.get(index + 1).equals("adapters")
                && PORT_DIRECTIONS.contains(parts.get(index + 2));
    }
}
