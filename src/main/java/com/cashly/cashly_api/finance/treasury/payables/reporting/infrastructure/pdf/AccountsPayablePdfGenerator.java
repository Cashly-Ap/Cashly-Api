package com.cashly.cashly_api.finance.treasury.payables.reporting.infrastructure.pdf;

import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtListResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import com.cashly.cashly_api.finance.treasury.payables.reporting.application.ports.out.AccountsPayableReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class AccountsPayablePdfGenerator
        implements AccountsPayableReportPdfPort {

    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DateTimeFormatter DATE = DateTimeFormatter
            .ofPattern("dd/MM/yyyy");

    private final PdfBranding branding;
    private final PayablesQueryGateway gateway;

    public AccountsPayablePdfGenerator(PdfBranding branding,
            PayablesQueryGateway gateway) {
        this.branding = branding;
        this.gateway = gateway;
    }

    @Override
    public byte[] generate(UUID supplierId, boolean overdueOnly) {
        List<SupplierDebtListResponse> rows = loadRows(supplierId,
                overdueOnly);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 50, 40);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            document.add(branding.logo().setMarginBottom(12));
            document.add(new Paragraph("Cuentas por Pagar").setFont(bold)
                    .setFontSize(18).setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(6));
            document.add(new Paragraph(
                    "Fecha: " + LocalDate.now().format(DATE)).setFont(regular)
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(16));

            float[] widths = {4f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f};
            Table table = new Table(UnitValue.createPercentArray(widths))
                    .useAllAvailableWidth();
            for (String header : List.of("Proveedor", "RIF", "Límite crédito",
                    "Deuda", "Crédito disponible", "Vence")) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setFont(bold)
                                .setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG));
            }
            BigDecimal total = BigDecimal.ZERO;
            for (SupplierDebtListResponse row : rows) {
                total = total.add(row.totalDebt());
                table.addCell(new Cell().add(new Paragraph(row.supplierName())
                        .setFont(regular)));
                table.addCell(new Cell().add(new Paragraph(
                        row.supplierRif() != null ? row.supplierRif() : "")
                        .setFont(regular)));
                table.addCell(new Cell().add(new Paragraph(
                        money(row.creditLimit())).setFont(regular)));
                table.addCell(new Cell().add(new Paragraph(
                        money(row.totalDebt())).setFont(regular)));
                table.addCell(new Cell().add(new Paragraph(
                        money(row.availableCredit())).setFont(regular)));
                table.addCell(new Cell().add(new Paragraph(
                        row.oldestDueDate() != null
                                ? row.oldestDueDate().format(DATE)
                                : "-")
                        .setFont(regular)));
            }
            document.add(table);
            document.add(new Paragraph("Total por pagar: " + money(total))
                    .setFont(bold).setFontSize(12).setMarginTop(14));
            document.close();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Error generating accounts payable report PDF", exception);
        }
        return baos.toByteArray();
    }

    private List<SupplierDebtListResponse> loadRows(UUID supplierId,
            boolean overdueOnly) {
        if (supplierId != null) {
            SupplierDebtSummaryResponse detail = gateway
                    .findSupplierDebt(supplierId);
            SupplierDebtListResponse row = new SupplierDebtListResponse(
                    detail.supplierId(), detail.supplierName(),
                    detail.supplierRif(), detail.totalDebt(),
                    detail.pendingPaymentsAmount(), detail.pendingPaymentsCount(),
                    detail.purchasesWithDebt().stream()
                            .map(p -> p.dueDate()).filter(d -> d != null)
                            .min(LocalDate::compareTo).orElse(null),
                    detail.creditDays(), detail.creditLimit(),
                    detail.availableCredit(), detail.supplierBalance());
            return matches(row, overdueOnly) ? List.of(row) : List.of();
        }
        List<SupplierDebtListResponse> content = gateway
                .findSuppliersWithDebt(null, 0, 1000, null, null).content();
        List<SupplierDebtListResponse> filtered = new ArrayList<>();
        for (SupplierDebtListResponse row : content) {
            if (matches(row, overdueOnly)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private boolean matches(SupplierDebtListResponse row, boolean overdueOnly) {
        return !overdueOnly || (row.oldestDueDate() != null
                && row.oldestDueDate().isBefore(LocalDate.now()));
    }

    private String money(BigDecimal value) {
        return "$" + (value != null ? value : BigDecimal.ZERO)
                .setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
