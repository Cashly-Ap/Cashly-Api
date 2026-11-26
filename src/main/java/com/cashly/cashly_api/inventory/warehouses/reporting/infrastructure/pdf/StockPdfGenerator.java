package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.pdf;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer.Column;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.layout.properties.TextAlignment;

import java.time.LocalDate;
import java.util.List;

public class StockPdfGenerator implements StockPdfPort {

    private final PdfBranding branding;

    public StockPdfGenerator() {
        this(new PdfBranding());
    }

    public StockPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<StockReportResponse> items) {
        List<Column> columns = List.of(
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
                        .text("inventory.warehouses.reports.category"), 2f,
                        TextAlignment.LEFT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.currentStock"), 1.5f,
                        TextAlignment.RIGHT),
                new Column(ReportMessages
                        .text("inventory.warehouses.reports.status"), 1.5f,
                        TextAlignment.CENTER));
        List<String[]> rows = items.stream()
                .map(item -> new String[]{item.warehouseName(),
                        item.productName(), item.sku(), item.categoryName(),
                        String.valueOf(item.quantity()),
                        ReportMessages.code(item.status())})
                .toList();
        String subtitle = ReportMessages
                .text("inventory.warehouses.reports.generatedOn")
                + LocalDate.now().format(ReportMessages.dateFormatter());
        String trailingNote = ReportMessages
                .text("inventory.warehouses.reports.totalListedProducts")
                + items.size();
        return FastTablePdfRenderer.render(branding, com.itextpdf.kernel.geom.PageSize.A4.rotate(),
                ReportMessages
                        .text("inventory.warehouses.reports.warehouseStock"),
                subtitle, columns, rows, List.of(trailingNote),
                ReportMessages.text("inventory.warehouses.reports.page"),
                ReportMessages.text("inventory.warehouses.reports.of"));
    }

}
