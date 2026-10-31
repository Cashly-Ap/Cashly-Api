package com.cashly.cashly_api.shared.infrastructure.i18n;

import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.pdf.StockPdfGenerator;
import com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.excel.StockExcelGenerator;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.excel.CashRegisterExcelGenerator;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import static org.assertj.core.api.Assertions.assertThat;

class ReportTranslationTest {
    @AfterEach
    void reset() {
        LocaleContextHolder.resetLocaleContext();
    }
    @Test
    void samePdfGeneratorUsesEachRequestedLanguageWithoutChangingUserData()
            throws Exception {
        var generator = new StockPdfGenerator();
        var items = List.of(new StockReportResponse("Almacén del usuario",
                "Producto del usuario", "SKU-ES", "Categoría del usuario", 12,
                "Con Stock"));
        for (var language : List.of("es", "en", "es")) {
            LocaleContextHolder.setLocale(Locale.forLanguageTag(language));
            try (var pdf = new PdfDocument(new PdfReader(
                    new ByteArrayInputStream(generator.generatePdf(items))))) {
                String text = PdfTextExtractor
                        .getTextFromPage(pdf.getFirstPage());
                assertThat(text).contains(language.equals("en")
                        ? "Warehouse stock"
                        : "Stock de Almacenes");
                assertThat(text).contains(
                        language.equals("en") ? "In stock" : "Con stock");
                assertThat(text).contains("Almacén del usuario",
                        "Producto del usuario", "Categoría del usuario");
            }
        }
    }
    @Test
    void sameExcelGeneratorTranslatesSheetHeadersAndStatusesPerRequest()
            throws Exception {
        var generator = new StockExcelGenerator();
        var items = List.of(new StockReportResponse("Mi almacén", "Mi producto",
                "SKU", "Mi categoría", 3, "Sin Stock"));
        for (var language : List.of("en", "es")) {
            LocaleContextHolder.setLocale(Locale.forLanguageTag(language));
            try (var workbook = new XSSFWorkbook(
                    new ByteArrayInputStream(generator.generateExcel(items)))) {
                var sheet = workbook.getSheetAt(0);
                assertThat(sheet.getSheetName()).isEqualTo(language.equals("en")
                        ? "Warehouse stock"
                        : "Stock de Almacenes");
                assertThat(sheet.getRow(3).getCell(0).getStringCellValue())
                        .isEqualTo(language.equals("en")
                                ? "Warehouse"
                                : "Almacén");
                assertThat(sheet.getRow(4).getCell(5).getStringCellValue())
                        .isEqualTo(language.equals("en")
                                ? "Out of stock"
                                : "Sin stock");
                assertThat(sheet.getRow(4).getCell(1).getStringCellValue())
                        .isEqualTo("Mi producto");
            }
        }
    }
    @Test
    void translatedCashRegisterGroupingPreservesStableModuleIdentifiers()
            throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        var row = CashRegisterExportRow.of(LocalDate.of(2026, 10, 29), "FAC",
                "INV-1", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, "Cliente original", "VENTAS",
                1);
        try (var workbook = new XSSFWorkbook(
                new ByteArrayInputStream(new CashRegisterExcelGenerator()
                        .generateExcel(List.of(row))))) {
            var sheet = workbook.getSheetAt(0);
            var content = new StringBuilder();
            for (var current : sheet)
                for (var cell : current)
                    content.append(cell.toString()).append(' ');
            assertThat(content).contains("INV-1", "Cliente original", "SALES");
        }
    }
    @Test
    void everyModuleHasMatchingSpanishAndEnglishKeys() throws Exception {
        try (var paths = Files.walk(Path.of("src/main/resources/modules"))) {
            for (var spanish : paths
                    .filter(path -> path.toString().endsWith("_es.properties"))
                    .toList()) {
                var english = Path.of(spanish.toString()
                        .replace("_es.properties", "_en.properties"));
                assertThat(english).exists();
                var es = new Properties();
                var en = new Properties();
                try (var reader = Files.newBufferedReader(spanish)) {
                    es.load(reader);
                }
                try (var reader = Files.newBufferedReader(english)) {
                    en.load(reader);
                }
                assertThat(en.stringPropertyNames()).as(spanish.toString())
                        .isEqualTo(es.stringPropertyNames());
                for (String key : es.stringPropertyNames()) {
                    assertThat(new MessageTranslator().translate(key,
                            Locale.ENGLISH)).isNotBlank();
                    assertThat(new MessageTranslator().translate(key,
                            Locale.forLanguageTag("es"))).isNotBlank();
                }
            }
        }
    }
    @Test
    void unsupportedRequestLanguageFallsBackToSpanishAndRespectsQualityWeights() {
        assertThat(RequestLanguageFilter.requestedLanguage("fr-FR,de;q=0.7"))
                .isEqualTo("es");
        assertThat(
                RequestLanguageFilter.requestedLanguage("es;q=0.5,en-US;q=0.9"))
                .isEqualTo("en");
        assertThat(RequestLanguageFilter.requestedLanguage("en;q=0,es;q=0.8"))
                .isEqualTo("es");
        assertThat(RequestLanguageFilter.requestedLanguage(null))
                .isEqualTo("es");
    }
}
