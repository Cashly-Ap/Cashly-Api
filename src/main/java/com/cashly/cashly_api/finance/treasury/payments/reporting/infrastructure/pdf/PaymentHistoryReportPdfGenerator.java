package com.cashly.cashly_api.finance.treasury.payments.reporting.infrastructure.pdf;

import com.cashly.cashly_api.finance.treasury.payments.reporting.application.dto.model.PaymentHistoryReportData;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.ports.out.PaymentHistoryReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.TextAlignment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryReportPdfGenerator
        implements
            PaymentHistoryReportPdfPort {

    private final PdfBranding branding;

    public PaymentHistoryReportPdfGenerator() {
        this(new PdfBranding());
    }

    public PaymentHistoryReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generateReport(PaymentHistoryReportData data) {
        List<FastTablePdfRenderer.Column> columns = List.of(
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.payments.reports.paymentNo"), 2.5f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.payments.reports.customerLabel"), 4f,
                        TextAlignment.LEFT),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.payments.reports.type"), 2f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.payments.reports.method"), 2f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.payments.reports.amount"), 2.5f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.payments.reports.dateLabel"), 3f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column(ReportMessages.text(
                        "finance.treasury.payments.reports.status"), 2f,
                        TextAlignment.CENTER));
        List<String[]> rows = new ArrayList<>(data.rows().size());
        for (var row : data.rows()) {
            rows.add(new String[]{row.paymentNumber(), row.customerName(),
                    row.type(), row.method(),
                    "$ " + formatDecimal(row.amount()),
                    row.date().format(ReportMessages.dateTimeFormatter()),
                    row.status()});
        }
        var summary = data.summary();
        List<String> trailingLines = List.of(
                ReportMessages.text(
                        "finance.treasury.payments.reports.totalPayments")
                        + ": " + summary.totalPayments(),
                ReportMessages.text(
                        "finance.treasury.payments.reports.totalAmountCollected")
                        + ": $ " + formatDecimal(summary.totalAmount()),
                ReportMessages
                        .text("finance.treasury.payments.reports.payments")
                        + ": " + summary.paymentCount(),
                ReportMessages
                        .text("finance.treasury.payments.reports.advances")
                        + ": " + summary.advanceCount());
        return FastTablePdfRenderer.render(branding, PageSize.A4,
                ReportMessages.text(
                        "finance.treasury.payments.reports.paymentHistory"),
                buildFilters(data), columns, rows, trailingLines,
                ReportMessages.text("finance.treasury.payments.reports.page"),
                ReportMessages.text("finance.treasury.payments.reports.of"));
    }

    private String buildFilters(PaymentHistoryReportData data) {
        StringBuilder filters = new StringBuilder();
        if (data.dateFrom() != null) {
            filters.append(ReportMessages
                    .text("finance.treasury.payments.reports.from"))
                    .append(data.dateFrom()
                            .format(ReportMessages.dateFormatter()));
        }
        if (data.dateTo() != null) {
            if (!filters.isEmpty()) {
                filters.append(" | ");
            }
            filters.append(
                    ReportMessages.text("finance.treasury.payments.reports.to"))
                    .append(data.dateTo()
                            .format(ReportMessages.dateFormatter()));
        }
        if (data.customerId() != null) {
            if (!filters.isEmpty()) {
                filters.append(" | ");
            }
            filters.append(ReportMessages
                    .text("finance.treasury.payments.reports.customer"))
                    .append(data.customerId());
        }
        return filters.isEmpty()
                ? ReportMessages.text(
                        "finance.treasury.payments.reports.date")
                        + java.time.LocalDate.now()
                                .format(ReportMessages.dateFormatter())
                : filters.toString();
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }
}
