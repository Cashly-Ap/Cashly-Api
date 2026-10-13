package com.cashly.cashly_api.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPackageConventionTest {
    private static final Path SOURCE_ROOT = Path.of("src/main/java/com/cashly/cashly_api");
    private static final List<String> AMBIGUOUS_PACKAGES = List.of("api", "contracts", "port", "query");
    private static final List<String> PORT_DIRECTIONS = List.of("in", "out");
    private static final List<String> PERSISTENCE_ROLES = List.of(
            "entities", "repositories", "adapters", "queries", "mappers", "projections");

    @Test
    void should_use_cash_register_for_the_cash_register_module_package() throws IOException {
        try (var sources = Files.walk(SOURCE_ROOT)) {
            var violations = sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> SOURCE_ROOT.relativize(path).getName(0).toString().equals("cashregister"))
                    .map(SOURCE_ROOT::relativize)
                    .toList();

            assertThat(violations).as("The cash register module package is cash_register").isEmpty();
        }
    }

    @Test
    void should_use_controllers_for_http_input_adapters() throws IOException {
        try (var sources = Files.walk(SOURCE_ROOT)) {
            var violations = sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().replace('\\', '/').contains("/adapters/in/web/"))
                    .map(SOURCE_ROOT::relativize)
                    .toList();

            assertThat(violations).as("HTTP input adapters belong in controllers").isEmpty();
        }
    }

    @Test
    void should_group_persistence_sources_by_responsibility() throws IOException {
        try (var sources = Files.walk(SOURCE_ROOT)) {
            var violations = sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(this::hasUnclassifiedPersistencePackage)
                    .map(SOURCE_ROOT::relativize)
                    .toList();

            assertThat(violations).as("Persistence sources require an explicit responsibility package").isEmpty();
        }
    }

    private boolean hasUnclassifiedPersistencePackage(Path source) {
        List<String> segments = List.of(SOURCE_ROOT.relativize(source).toString().replace('\\', '/').split("/"));
        int persistenceIndex = segments.indexOf("persistence");
        return persistenceIndex >= 0 && (persistenceIndex + 1 >= segments.size()
                || !PERSISTENCE_ROLES.contains(segments.get(persistenceIndex + 1)));
    }

    @Test
    void should_keep_only_interfaces_in_persistence_repositories() throws IOException, ClassNotFoundException {
        var violations = new java.util.ArrayList<Path>();
        try (var sources = Files.walk(SOURCE_ROOT)) {
            for (Path source : sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().replace('\\', '/')
                            .contains("/persistence/repositories/")).toList()) {
                String relativeName = SOURCE_ROOT.relativize(source).toString()
                        .replace('\\', '.').replace('/', '.').replaceFirst("\\.java$", "");
                Class<?> repository = Class.forName("com.cashly.cashly_api." + relativeName,
                        false, getClass().getClassLoader());
                if (!repository.isInterface()) {
                    violations.add(SOURCE_ROOT.relativize(source));
                }
            }
        }

        assertThat(violations).as("Concrete repository implementations belong in persistence/adapters").isEmpty();
    }

    @Test
    void should_use_distinct_packages_for_offered_capabilities_required_ports_and_queries() throws IOException {
        try (var sources = Files.walk(SOURCE_ROOT)) {
            var violations = sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(this::hasAmbiguousApplicationPackage)
                    .map(SOURCE_ROOT::relativize)
                    .toList();

            assertThat(violations).as("Application ports require in/out direction; query inputs belong in dto/query")
                    .isEmpty();
        }
    }

    @Test
    void should_declare_input_or_output_direction_for_infrastructure_adapters() throws IOException {
        try (var sources = Files.walk(SOURCE_ROOT)) {
            var violations = sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(this::hasUndirectedAdapterPackage)
                    .map(SOURCE_ROOT::relativize)
                    .toList();

            assertThat(violations).as("Infrastructure adapters require an explicit in/out direction").isEmpty();
        }
    }

    private boolean hasAmbiguousApplicationPackage(Path source) {
        List<String> segments = List.of(SOURCE_ROOT.relativize(source).toString().replace('\\', '/').split("/"));
        int applicationIndex = segments.indexOf("application");
        if (applicationIndex < 0 || applicationIndex + 1 >= segments.size()) {
            return false;
        }
        String applicationPackage = segments.get(applicationIndex + 1);
        if (AMBIGUOUS_PACKAGES.contains(applicationPackage)) {
            return true;
        }
        return applicationPackage.equals("ports") && (applicationIndex + 2 >= segments.size()
                || !PORT_DIRECTIONS.contains(segments.get(applicationIndex + 2)));
    }

    private boolean hasUndirectedAdapterPackage(Path source) {
        List<String> segments = List.of(SOURCE_ROOT.relativize(source).toString().replace('\\', '/').split("/"));
        int infrastructureIndex = segments.indexOf("infrastructure");
        return infrastructureIndex >= 0 && infrastructureIndex + 1 < segments.size()
                && segments.get(infrastructureIndex + 1).equals("adapters")
                && (infrastructureIndex + 2 >= segments.size()
                || !PORT_DIRECTIONS.contains(segments.get(infrastructureIndex + 2)));
    }
}
