package com.cashly.cashly_api.finance.treasury.receivables.reporting.infrastructure.pdf;

import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.dto.model.CollectionsReportData;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.ports.out.CollectionsReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.TextAlignment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CollectionsReportPdfGenerator implements CollectionsReportPdfPort {

    private final PdfBranding branding;

    public CollectionsReportPdfGenerator() {
        this(new PdfBranding());
    }

    public CollectionsReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generateReport(CollectionsReportData data) {
        List<FastTablePdfRenderer.Column> columns = List.of(
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.customer"), 4f,
                        TextAlignment.LEFT),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.taxId"), 2.5f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.invoiceNo"), 2.5f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.dueDate"), 2.5f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.daysOverdue"), 2f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.outstandingBalance"),
                        2.5f, TextAlignment.RIGHT));
        List<String[]> rows = new ArrayList<>(data.rows().size());
        for (var row : data.rows()) {
            rows.add(new String[]{row.customerName(),
                    row.rif() != null ? row.rif() : "-", row.invoiceNumber(),
                    row.dueDate().format(ReportMessages.dateFormatter()),
                    String.valueOf(row.daysOverdue()),
                    "$ " + formatDecimal(row.pendingBalance())});
        }
        var summary = data.summary();
        List<String> trailingLines = List.of(
                ReportMessages.text(
                        "finance.treasury.receivables.reports.totalOverdueInvoices")
                        + ": " + summary.totalInvoices(),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.totalOverdueAmount")
                        + ": $ " + formatDecimal(summary.totalOverdue()),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.030Days")
                        + ": $ " + formatDecimal(summary.age0_30()),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.3160Days")
                        + ": $ " + formatDecimal(summary.age31_60()),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.6190Days")
                        + ": $ " + formatDecimal(summary.age61_90()),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.91120Days")
                        + ": $ " + formatDecimal(summary.age91_120()),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.120Days")
                        + ": $ " + formatDecimal(summary.age120Plus()));
        String subtitle = ReportMessages
                .text("finance.treasury.receivables.reports.date")
                + data.reportDate().format(ReportMessages.dateFormatter());
        return FastTablePdfRenderer.render(branding, PageSize.A4,
                ReportMessages.text(
                        "finance.treasury.receivables.reports.collectionsReport"),
                subtitle, columns, rows, trailingLines,
                ReportMessages
                        .text("finance.treasury.receivables.reports.page"),
                ReportMessages
                        .text("finance.treasury.receivables.reports.of"));
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }
}
