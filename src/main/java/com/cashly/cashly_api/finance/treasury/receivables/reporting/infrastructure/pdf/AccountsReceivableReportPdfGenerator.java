package com.cashly.cashly_api.finance.treasury.receivables.reporting.infrastructure.pdf;

import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.dto.model.AccountsReceivableReportData;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.ports.out.AccountsReceivableReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.TextAlignment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountsReceivableReportPdfGenerator
        implements AccountsReceivableReportPdfPort {

    private final PdfBranding branding;

    public AccountsReceivableReportPdfGenerator() {
        this(new PdfBranding());
    }

    public AccountsReceivableReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generateReport(AccountsReceivableReportData data) {
        List<FastTablePdfRenderer.Column> columns = List.of(
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.customer"), 4f,
                        TextAlignment.LEFT),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.taxId"), 2.5f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.creditLimit"), 2.5f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.outstandingBalance"),
                        2.5f, TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.receivables.reports.available"), 2.5f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("0-30", 2f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("31-60", 2f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("61-90", 2f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("91-120", 2f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("120+", 2f,
                        TextAlignment.RIGHT));
        List<String[]> rows = new ArrayList<>(data.rows().size());
        for (var row : data.rows()) {
            rows.add(new String[]{row.customerName(),
                    row.rif() != null ? row.rif() : "-",
                    "$ " + formatDecimal(row.creditLimit()),
                    "$ " + formatDecimal(row.pendingBalance()),
                    "$ " + formatDecimal(row.available()),
                    "$ " + formatDecimal(row.age0_30()),
                    "$ " + formatDecimal(row.age31_60()),
                    "$ " + formatDecimal(row.age61_90()),
                    "$ " + formatDecimal(row.age91_120()),
                    "$ " + formatDecimal(row.age120Plus())});
        }
        var summary = data.summary();
        List<String> trailingLines = List.of(
                ReportMessages.text(
                        "finance.treasury.receivables.reports.totalAccountsReceivable")
                        + ": $ " + formatDecimal(summary.totalReceivable()),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.notYetDue")
                        + ": $ " + formatDecimal(summary.notYetDue()),
                ReportMessages
                        .text("finance.treasury.receivables.reports.overdue")
                        + ": $ " + formatDecimal(summary.overdue()),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.customersOverCreditLimit")
                        + ": " + summary.customersExceededCredit());
        String subtitle = ReportMessages
                .text("finance.treasury.receivables.reports.date")
                + data.reportDate().format(ReportMessages.dateFormatter());
        return FastTablePdfRenderer.render(branding, PageSize.A4.rotate(),
                ReportMessages.text(
                        "finance.treasury.receivables.reports.accountsReceivable"),
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
