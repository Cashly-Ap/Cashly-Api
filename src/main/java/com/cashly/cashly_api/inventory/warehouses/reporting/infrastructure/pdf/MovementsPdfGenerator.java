package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.pdf;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.MovementsPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer.Column;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.layout.properties.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MovementsPdfGenerator implements MovementsPdfPort {

    private final PdfBranding branding;

    public MovementsPdfGenerator() {
        this(new PdfBranding());
    }

    public MovementsPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<MovementReportResponse> items) {
        DateTimeFormatter dtf = ReportMessages.dateTimeFormatter();
        List<Column> columns = List.of(
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.date"), 2f,
                        TextAlignment.CENTER),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.warehouse"), 2.5f,
                        TextAlignment.LEFT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.product"), 3f,
                        TextAlignment.LEFT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.sku"), 1.5f,
                        TextAlignment.CENTER),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.type"), 2f,
                        TextAlignment.CENTER),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.quantity"), 1.2f,
                        TextAlignment.RIGHT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.previousStock"), 1.5f,
                        TextAlignment.RIGHT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.newStock"), 1.5f,
                        TextAlignment.RIGHT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.notes"), 3f,
                        TextAlignment.LEFT));
        List<String[]> rows = items.stream()
                .map(item -> new String[]{
                        item.createdAt() != null
                                ? item.createdAt().format(dtf)
                                : "-",
                        item.warehouseName(), item.productName(), item.sku(),
                        ReportMessages.code(item.movementType()),
                        String.valueOf(item.quantity()),
                        String.valueOf(item.previousStock()),
                        String.valueOf(item.newStock()),
                        item.notes() != null ? item.notes() : "-"})
                .toList();
        String subtitle = ReportMessages
                .text("inventory.warehouses.reports.generatedOn")
                + LocalDate.now().format(ReportMessages.dateFormatter());
        return FastTablePdfRenderer.render(branding, com.itextpdf.kernel.geom.PageSize.A4.rotate(),
                ReportMessages.text(
                        "inventory.warehouses.reports.stockMovementHistory"),
                subtitle, columns, rows, List.of(),
                ReportMessages.text("inventory.warehouses.reports.page"),
                ReportMessages.text("inventory.warehouses.reports.of"));
    }

}
