package com.cashly.cashly_api.procurement.purchases.reporting.infrastructure.pdf;

import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportRow;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasesReportPdfPort;
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

public class PurchasesReportPdfGenerator implements PurchasesReportPdfPort {

    private final PdfBranding branding;

    public PurchasesReportPdfGenerator() {
        this(new PdfBranding());
    }

    public PurchasesReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<PurchaseReportRow> purchases, String mode) {
        boolean detailed = "detailed".equalsIgnoreCase(mode);
        List<FastTablePdfRenderer.Column> columns = detailed
                ? List.of(
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.purchaseNoLabel"),
                                3f, TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.supplierLabel"),
                                3f, TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.status"), 2f,
                                TextAlignment.CENTER),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.total"), 2f,
                                TextAlignment.RIGHT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.dateLabel"), 2f,
                                TextAlignment.CENTER))
                : List.of(
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.purchaseNoLabel"),
                                3f, TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.supplierLabel"),
                                3f, TextAlignment.LEFT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.total"), 2f,
                                TextAlignment.RIGHT),
                        new FastTablePdfRenderer.Column(ReportMessages.text(
                                "procurement.purchases.reports.dateLabel"), 2f,
                                TextAlignment.CENTER));
        List<String[]> rows = new ArrayList<>(purchases.size());
        for (PurchaseReportRow purchase : purchases) {
            String date = purchase.createdAt()
                    .atZone(ZoneId.systemDefault())
                    .format(ReportMessages.dateTimeFormatter());
            String total = "$" + formatDecimal(purchase.subtotal());
            if (detailed) {
                rows.add(new String[]{purchase.purchaseNumber(),
                        purchase.supplierName(),
                        formatStatus(purchase.status()), total, date});
            } else {
                rows.add(new String[]{purchase.purchaseNumber(),
                        purchase.supplierName(), total, date});
            }
        }
        long draft = purchases.stream()
                .filter(p -> "DRAFT".equals(p.status())).count();
        long confirmed = purchases.stream()
                .filter(p -> "CONFIRMED".equals(p.status())).count();
        long cancelled = purchases.stream()
                .filter(p -> "CANCELLED".equals(p.status())).count();
        List<String> trailingLines = new ArrayList<>();
        trailingLines.add(ReportMessages
                .text("procurement.purchases.reports.totalPurchases")
                + purchases.size());
        trailingLines.add(String.format(ReportMessages.text(
                "procurement.purchases.reports.draftDConfirmedDCancelledD"),
                draft, confirmed, cancelled));
        String modeLabel = detailed
                ? ReportMessages
                        .text("procurement.purchases.reports.detailed")
                : ReportMessages.text("procurement.purchases.reports.simple");
        String subtitle = ReportMessages
                .text("procurement.purchases.reports.generatedOn")
                + LocalDate.now().format(ReportMessages.dateFormatter())
                + ReportMessages.text("procurement.purchases.reports.mode")
                + modeLabel
                + ReportMessages
                        .text("procurement.purchases.reports.totalLabel")
                + purchases.size()
                + ReportMessages
                        .text("procurement.purchases.reports.purchases");
        return FastTablePdfRenderer.render(branding, PageSize.A4.rotate(),
                ReportMessages
                        .text("procurement.purchases.reports.purchasesReport"),
                subtitle, columns, rows, trailingLines,
                ReportMessages.text("inventory.warehouses.reports.page"),
                ReportMessages.text("inventory.warehouses.reports.of"));
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case "DRAFT" ->
                ReportMessages.text("procurement.purchases.reports.draft");
            case "CONFIRMED" ->
                ReportMessages.text("procurement.purchases.reports.confirmed");
            case "CANCELLED" ->
                ReportMessages.text("procurement.purchases.reports.cancelled");
            default -> status;
        };
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }
}
