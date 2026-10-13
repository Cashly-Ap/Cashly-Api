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
    private static final Path SOURCE_ROOT = Path.of("src/main/java/com/cashly/cashly_api");

    @Test
    void domain_should_be_independent_of_application_infrastructure_and_frameworks() throws IOException {
        assertNoImports("/domain/", Pattern.compile(
                "^import (?:com\\.cashly\\.cashly_api\\..*\\.(?:application|infrastructure)\\.|org\\.springframework\\.|jakarta\\.persistence\\.).*"));
    }

    @Test
    void application_output_ports_should_be_independent_of_infrastructure_and_frameworks() throws IOException {
        assertNoImports("/application/ports/out/", Pattern.compile(
                "^import (?:com\\.cashly\\.cashly_api\\..*\\.infrastructure\\.|org\\.springframework\\.|jakarta\\.persistence\\.).*"));
    }

    @Test
    void application_input_ports_should_be_independent_of_infrastructure_and_frameworks() throws IOException {
        assertNoImports("/application/ports/in/", Pattern.compile(
                "^import (?:com\\.cashly\\.cashly_api\\..*\\.infrastructure\\.|org\\.springframework\\.|jakarta\\.persistence\\.).*"));
    }

    @Test
    void application_should_depend_on_other_modules_only_through_input_ports() throws IOException {
        var violations = new ArrayList<String>();
        try (var sources = Files.walk(SOURCE_ROOT)) {
            for (Path source : sources.filter(path -> path.toString().replace('\\', '/').contains("/application/"))
                    .filter(path -> path.toString().endsWith(".java")).toList()) {
                String sourceModule = moduleOf(SOURCE_ROOT.relativize(source));
                for (String line : Files.readAllLines(source)) {
                    var importedModule = importedModule(line);
                    if (importedModule.isEmpty() || importedModule.get().equals(sourceModule)) {
                        continue;
                    }
                    String importedPackage = normalizedImport(line);
                    importedPackage = importedPackage.substring("import ".length(), importedPackage.length() - 1);
                    if (importedPackage.matches("com\\.cashly\\.cashly_api\\..*\\.application\\.ports\\.in\\..*")) {
                        continue;
                    }
                    if (importedPackage.matches("com\\.cashly\\.cashly_api\\..*\\.(domain|application|infrastructure)\\..*")) {
                        violations.add(SOURCE_ROOT.relativize(source) + ": " + line.strip());
                    }
                }
            }
        }
        assertThat(violations).as("Cross-module dependencies must use provider application input ports").isEmpty();
    }

    @Test
    void domain_should_not_depend_on_other_modules_internals() throws IOException {
        var violations = new ArrayList<String>();
        try (var sources = Files.walk(SOURCE_ROOT)) {
            for (Path source : sources.filter(path -> path.toString().replace('\\', '/').contains("/domain/"))
                    .filter(path -> path.toString().endsWith(".java")).toList()) {
                String sourceModule = moduleOf(SOURCE_ROOT.relativize(source));
                for (String line : Files.readAllLines(source)) {
                    var importedModule = importedModule(line);
                    if (importedModule.isEmpty() || importedModule.get().equals(sourceModule)) {
                        continue;
                    }
                    String importedPackage = normalizedImport(line);
                    importedPackage = importedPackage.substring("import ".length(), importedPackage.length() - 1);
                    if (!importedPackage.equals("com.cashly.cashly_api.shared.dto.PageResponse")) {
                        violations.add(SOURCE_ROOT.relativize(source) + ": " + line.strip());
                    }
                }
            }
        }
        assertThat(violations).as("Domain must not depend on another module's internals").isEmpty();
    }

    private String moduleOf(Path source) {
        List<String> segments = List.of(source.toString().replace('\\', '/').split("/"));
        return segments.get(0);
    }

    private java.util.Optional<String> importedModule(String line) {
        var matcher = Pattern.compile("^import com\\.cashly\\.cashly_api\\.([a-z_]+)\\.(.*);$")
                .matcher(normalizedImport(line));
        if (!matcher.matches()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(matcher.group(1));
    }

    private String normalizedImport(String line) {
        return line.strip().replaceFirst("^import static ", "import ");
    }

    private void assertNoImports(String layerPath, Pattern forbiddenImport) throws IOException {
        var violations = new ArrayList<String>();
        try (var sources = Files.walk(SOURCE_ROOT)) {
            for (Path source : sources.filter(path -> path.toString().replace('\\', '/').contains(layerPath))
                    .filter(path -> path.toString().endsWith(".java")).toList()) {
                for (String line : Files.readAllLines(source)) {
                    if (forbiddenImport.matcher(line.strip()).matches()) {
                        violations.add(SOURCE_ROOT.relativize(source) + ": " + line.strip());
                    }
                }
            }
        }
        assertThat(violations).as("Dependencies must point inward").isEmpty();
    }
}
