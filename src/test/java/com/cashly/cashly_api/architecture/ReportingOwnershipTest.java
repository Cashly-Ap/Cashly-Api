package com.cashly.cashly_api.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ReportingOwnershipTest {
    private static final List<Path> ROOTS = List.of(CapabilityStructureTest.SOURCE_ROOT,
            Path.of("src/test/java/com/cashly/cashly_api"));
    private static final Set<String> OWNERS = Set.of("invoices", "orders", "quotes", "purchases",
            "products", "warehouses", "payments", "receivables", "cash_register");
    private static final Set<String> DOCUMENT_OWNERS = Set.of("invoices", "orders", "quotes", "purchases",
            "products", "warehouses", "payments", "receivables");
    private static final Set<String> SHARED_PDF = Set.of("PdfBranding", "PdfStatusColors");
    private static final Pattern GLOBAL_REFERENCE = Pattern.compile("\\bcom\\.cashly\\.cashly_api\\.reports(?:\\.|\\b)");

    @Test
    void should_remove_global_reports_packages_and_references_in_production_and_tests() throws IOException {
        var violations = new ArrayList<String>();
        for (Path root : ROOTS) {
            for (Path source : CapabilityStructureTest.javaSources(root)) {
                Path path = root.relativize(source);
                if (path.getName(0).toString().equals("reports") || GLOBAL_REFERENCE.matcher(Files.readString(source)).find()) {
                    violations.add(source.toString());
                }
            }
        }
        assertThat(violations).as("No global reports package or imports may survive relocation").isEmpty();
    }

    @Test
    void should_keep_document_generation_and_visual_helpers_in_their_approved_owner() throws IOException {
        var violations = new ArrayList<String>();
        for (Path root : ROOTS) {
            for (Path source : CapabilityStructureTest.javaSources(root)) {
                Path path = root.relativize(source);
                if (!hasReportingOwner(path)) violations.add(source.toString());
            }
        }
        assertThat(violations).as("Document generators belong to module/reporting; only named visual helpers are shared").isEmpty();
    }

    @Test
    void should_have_application_infrastructure_and_tests_for_every_document_report_owner() throws IOException {
        var missing = new ArrayList<String>();
        for (String owner : DOCUMENT_OWNERS) {
            for (String layer : List.of("application", "infrastructure")) {
                Path directory = ROOTS.get(0).resolve(BusinessModulePaths.modulePath(owner)).resolve("reporting").resolve(layer);
                if (!hasJavaSources(directory)) missing.add(directory.toString());
            }
            Path tests = ROOTS.get(1).resolve(BusinessModulePaths.modulePath(owner)).resolve("reporting");
            if (!hasJavaSources(tests)) missing.add(tests.toString());
        }
        assertThat(missing).as("All eight owners must contain actual application, infrastructure and tests").isEmpty();
    }

    @Test
    void should_reject_technical_roots_and_generators_outside_reporting() {
        assertThat(List.of("reports/stock/infrastructure/pdf/StockPdfGenerator.java",
                "warehouses/stock/infrastructure/excel/StockExcelGenerator.java",
                "invoices/infrastructure/pdf/InvoicePdfGenerator.java",
                "shared/infrastructure/pdf/InvoicePdfGenerator.java",
                "unknown/reporting/application/Report.java").stream().map(Path::of).filter(this::hasReportingOwner))
                .isEmpty();
    }

    @Test
    void should_allow_owner_reporting_and_only_shared_visual_helpers() {
        assertThat(List.of("warehouses/reporting/infrastructure/excel/StockExcelGenerator.java",
                "payments/reporting/infrastructure/web/PaymentReportsController.java",
                "shared/infrastructure/pdf/PdfBranding.java",
                "shared/infrastructure/pdf/PdfStatusColorsTest.java",
                "cash_register/reporting/application/get_summary/GetSummary.java").stream().map(Path::of)
                .allMatch(this::hasReportingOwner)).isTrue();
    }

    private boolean hasReportingOwner(Path path) {
        path = BusinessModulePaths.withoutArea(path);
        String normalized = path.toString().replace('\\', '/');
        String name = path.getFileName().toString().replaceFirst("(?:Test)?\\.java$", "");
        if (path.getName(0).toString().equals("reports")) return false;
        if (normalized.contains("/reporting/")) {
            return path.getNameCount() > 2 && path.getName(1).toString().equals("reporting")
                    && OWNERS.contains(path.getName(0).toString());
        }
        if (normalized.contains("/pdf/") || normalized.contains("/excel/")) {
            return normalized.startsWith("shared/infrastructure/pdf/") && SHARED_PDF.contains(name);
        }
        return true;
    }

    private boolean hasJavaSources(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) return false;
        try (var paths = Files.walk(directory)) {
            return paths.anyMatch(path -> path.toString().endsWith(".java"));
        }
    }
}
