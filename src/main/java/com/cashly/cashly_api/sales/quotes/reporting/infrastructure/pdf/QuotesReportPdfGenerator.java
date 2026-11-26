package com.cashly.cashly_api.sales.quotes.reporting.infrastructure.pdf;

import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportRow;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuotesReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.TextAlignment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class QuotesReportPdfGenerator implements QuotesReportPdfPort {

    private final PdfBranding branding;

    public QuotesReportPdfGenerator() {
        this(new PdfBranding());
    }

    public QuotesReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<QuoteReportRow> quotes, String mode) {
        boolean detailed = "detailed".equalsIgnoreCase(mode);
        List<FastTablePdfRenderer.Column> columns = detailed
                ? List.of(
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.quoteNoLabel"), 3f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.customerLabel"), 3f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.status"), 2f,
                                TextAlignment.CENTER),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.total"), 2f,
                                TextAlignment.RIGHT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.dateLabel"), 2f,
                                TextAlignment.CENTER))
                : List.of(
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.quoteNoLabel"), 3f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.customerLabel"), 3f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.total"), 2f,
                                TextAlignment.RIGHT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.quotes.reports.dateLabel"), 2f,
                                TextAlignment.CENTER));
        List<String[]> rows = new ArrayList<>(quotes.size());
        for (QuoteReportRow quote : quotes) {
            String total = "$" + formatDecimal(quote.subtotal());
            String date = quote.createdAt().atZone(ZoneId.systemDefault())
                    .format(ReportMessages.dateTimeFormatter());
            if (detailed) {
                rows.add(new String[]{quote.quoteNumber(),
                        quote.customerName(), formatStatus(quote.status()),
                        total, date});
            } else {
                rows.add(new String[]{quote.quoteNumber(),
                        quote.customerName(), total, date});
            }
        }
        long draft = quotes.stream()
                .filter(q -> "DRAFT".equals(q.status())).count();
        long convertedToOrder = quotes.stream()
                .filter(q -> "CONVERTED_TO_ORDER".equals(q.status())).count();
        long convertedToInvoice = quotes.stream()
                .filter(q -> "CONVERTED_TO_INVOICE".equals(q.status())).count();
        long cancelled = quotes.stream()
                .filter(q -> "CANCELLED".equals(q.status())).count();
        List<String> trailingLines = new ArrayList<>();
        trailingLines.add(ReportMessages
                .text("sales.quotes.reports.totalQuotes") + quotes.size());
        trailingLines.add(String.format(ReportMessages.text(
                "sales.quotes.reports.draftDConvertedToOrderDConvertedToInvoiceDCancelledD"),
                draft, convertedToOrder, convertedToInvoice, cancelled));
        String modeLabel = detailed
                ? ReportMessages.text("sales.quotes.reports.detailed")
                : ReportMessages.text("sales.quotes.reports.simple");
        String subtitle = ReportMessages
                .text("sales.quotes.reports.generatedOn")
                + LocalDate.now().format(ReportMessages.dateFormatter())
                + ReportMessages.text("sales.quotes.reports.mode") + modeLabel
                + ReportMessages.text("sales.quotes.reports.totalLabel")
                + quotes.size()
                + ReportMessages.text("sales.quotes.reports.quotes");
        return FastTablePdfRenderer.render(branding, PageSize.A4.rotate(),
                ReportMessages.text("sales.quotes.reports.quotesReport"),
                subtitle, columns, rows, trailingLines,
                ReportMessages.text("inventory.warehouses.reports.page"),
                ReportMessages.text("inventory.warehouses.reports.of"));
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case "DRAFT" -> ReportMessages.text("sales.quotes.reports.draft");
            case "CONVERTED_TO_ORDER" ->
                ReportMessages.text("sales.quotes.reports.order");
            case "CONVERTED_TO_INVOICE" ->
                ReportMessages.text("sales.quotes.reports.invoice");
            case "CANCELLED" ->
                ReportMessages.text("sales.quotes.reports.cancelled");
            default -> status;
        };
    }
}
