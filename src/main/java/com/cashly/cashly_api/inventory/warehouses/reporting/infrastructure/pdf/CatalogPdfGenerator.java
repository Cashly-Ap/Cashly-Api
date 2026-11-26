package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.pdf;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.CatalogPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer.Column;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.layout.properties.TextAlignment;

import java.time.LocalDate;
import java.util.List;

public class CatalogPdfGenerator implements CatalogPdfPort {

    private final PdfBranding branding;

    public CatalogPdfGenerator() {
        this(new PdfBranding());
    }

    public CatalogPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<CatalogReportResponse> items) {
        List<Column> columns = List.of(
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.product"), 3f,
                        TextAlignment.LEFT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.sku"), 2f,
                        TextAlignment.CENTER),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.category"), 2f,
                        TextAlignment.LEFT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.warehouse"), 2f,
                        TextAlignment.LEFT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.type"), 1.5f,
                        TextAlignment.CENTER),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.stock"), 1.5f,
                        TextAlignment.RIGHT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.status"), 1.5f,
                        TextAlignment.CENTER));
        List<String[]> rows = items.stream()
                .map(item -> new String[]{item.productName(), item.sku(),
                        item.categoryName(),
                        item.warehouseName() != null ? item.warehouseName()
                                : "-",
                        Boolean.TRUE.equals(item.composite())
                                ? ReportMessages.text(
                                        "inventory.warehouses.reports.composite")
                                : ReportMessages.text(
                                        "inventory.warehouses.reports.simple"),
                        item.quantity() != null
                                ? item.quantity().toPlainString()
                                : "0",
                        Boolean.TRUE.equals(item.active())
                                ? ReportMessages.text(
                                        "inventory.warehouses.reports.active")
                                : ReportMessages.text(
                                        "inventory.warehouses.reports.inactive")})
                .toList();
        String subtitle = ReportMessages
                .text("inventory.warehouses.reports.generatedOn")
                + LocalDate.now().format(ReportMessages.dateFormatter());
        return FastTablePdfRenderer.render(branding, com.itextpdf.kernel.geom.PageSize.A4.rotate(),
                ReportMessages
                        .text("inventory.warehouses.reports.productsReport"),
                subtitle, columns, rows, List.of(),
                ReportMessages.text("inventory.warehouses.reports.page"),
                ReportMessages.text("inventory.warehouses.reports.of"));
    }
}
