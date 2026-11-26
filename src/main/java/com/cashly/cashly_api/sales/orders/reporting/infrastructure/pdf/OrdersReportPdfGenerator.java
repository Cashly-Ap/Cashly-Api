package com.cashly.cashly_api.sales.orders.reporting.infrastructure.pdf;

import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportRow;
import com.cashly.cashly_api.sales.orders.reporting.application.ports.out.OrdersReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.TextAlignment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdersReportPdfGenerator implements OrdersReportPdfPort {

    private final PdfBranding branding;

    public OrdersReportPdfGenerator() {
        this(new PdfBranding());
    }

    public OrdersReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<OrderReportRow> orders, String mode) {
        boolean detailed = "detailed".equalsIgnoreCase(mode);
        List<FastTablePdfRenderer.Column> columns = detailed
                ? List.of(
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.orderNo"), 2f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.customer"), 3f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.lines"), 1.5f,
                                TextAlignment.CENTER),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.invoice"), 1.5f,
                                TextAlignment.CENTER),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.status"), 2f,
                                TextAlignment.CENTER),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.totalLabel"), 2f,
                                TextAlignment.RIGHT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.date"), 1.5f,
                                TextAlignment.CENTER))
                : List.of(
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.orderNo"), 2f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.customer"), 3f,
                                TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.totalLabel"), 2f,
                                TextAlignment.RIGHT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "sales.orders.reports.date"), 2f,
                                TextAlignment.CENTER));
        List<String[]> rows = new ArrayList<>(orders.size());
        for (OrderReportRow order : orders) {
            String total = "$" + formatDecimal(order.subtotal());
            String date = order.createdAt()
                    .format(ReportMessages.dateTimeFormatter());
            if (detailed) {
                rows.add(new String[]{order.orderNumber(),
                        order.customerName(),
                        String.valueOf(order.lineCount()),
                        order.invoiceId() != null
                                ? ReportMessages.text(
                                        "sales.orders.reports.yes")
                                : "-",
                        formatStatus(order.status()), total, date});
            } else {
                rows.add(new String[]{order.orderNumber(),
                        order.customerName(), total, date});
            }
        }
        long confirmed = orders.stream()
                .filter(o -> "CONFIRMED".equals(o.status())).count();
        long invoiced = orders.stream()
                .filter(o -> "INVOICED".equals(o.status())).count();
        long cancelled = orders.stream()
                .filter(o -> "CANCELLED".equals(o.status())).count();
        long draft = orders.stream()
                .filter(o -> "DRAFT".equals(o.status())).count();
        List<String> trailingLines = new ArrayList<>();
        trailingLines.add(ReportMessages
                .text("sales.orders.reports.totalOrders") + orders.size());
        trailingLines.add(String.format(ReportMessages.text(
                "sales.orders.reports.confirmedDInvoicedDCancelledDDraftD"),
                confirmed, invoiced, cancelled, draft));
        String modeLabel = detailed
                ? ReportMessages.text("sales.orders.reports.detailed")
                : ReportMessages.text("sales.orders.reports.simple");
        String subtitle = ReportMessages
                .text("sales.orders.reports.generatedOn")
                + LocalDate.now().format(ReportMessages.dateFormatter())
                + ReportMessages.text("sales.orders.reports.mode") + modeLabel
                + ReportMessages.text("sales.orders.reports.total")
                + orders.size()
                + ReportMessages.text("sales.orders.reports.orders");
        return FastTablePdfRenderer.render(branding, PageSize.A4.rotate(),
                ReportMessages.text("sales.orders.reports.ordersReport"),
                subtitle, columns, rows, trailingLines,
                ReportMessages.text("inventory.warehouses.reports.page"),
                ReportMessages.text("inventory.warehouses.reports.of"));
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }

    private static String formatStatus(String status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case "DRAFT" -> ReportMessages.text("sales.orders.reports.draft");
            case "CONFIRMED" ->
                ReportMessages.text("sales.orders.reports.confirmed");
            case "INVOICED" ->
                ReportMessages.text("sales.orders.reports.invoiced");
            case "IN_PREPARATION" ->
                ReportMessages.text("sales.orders.reports.inPreparation");
            case "READY_FOR_DISPATCH" ->
                ReportMessages.text("sales.orders.reports.readyForDispatch");
            case "DISPATCHED" ->
                ReportMessages.text("sales.orders.reports.dispatched");
            case "DELIVERED" ->
                ReportMessages.text("sales.orders.reports.delivered");
            case "CANCELLED" ->
                ReportMessages.text("sales.orders.reports.cancelled");
            default -> status;
        };
    }
}
