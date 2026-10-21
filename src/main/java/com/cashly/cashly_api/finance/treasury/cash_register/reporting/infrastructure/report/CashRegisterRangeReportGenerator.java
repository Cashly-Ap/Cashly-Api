package com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.report;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import org.springframework.beans.factory.annotation.Autowired;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterReportFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CustomerSummary;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportFormat;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterExcelPort;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterRangeReportPort;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegisterDetail;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class CashRegisterRangeReportGenerator implements CashRegisterRangeReportPort {

    private static final DateTimeFormatter SPANISH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CashRegisterExcelPort excelPort;
    private final PdfBranding branding;

    public CashRegisterRangeReportGenerator(CashRegisterExcelPort excelPort) {
        this(excelPort, new PdfBranding());
    }

    @Autowired
    public CashRegisterRangeReportGenerator(CashRegisterExcelPort excelPort, PdfBranding branding) {
        this.excelPort = excelPort;
        this.branding = branding;
    }

    @Override
    public byte[] generate(List<CashRegisterExportRow> rows, CashRegisterReportFilter filter,
                           ReportFormat format, ReportType type) {
        if (format == ReportFormat.EXCEL) {
            return generateExcel(rows, type);
        }
        return generatePdf(rows, filter, type);
    }

    private byte[] generateExcel(List<CashRegisterExportRow> rows, ReportType type) {
        return switch (type) {
            case SIMPLE -> {
                DailyTotals totals = totals(rows);
                yield excelPort.generateSimpleExcel(paymentDetails(rows), totals);
            }
            case DETAILED -> excelPort.generateExcel(rows);
            case GROUPED -> excelPort.generateGroupedExcel(customerSummaries(rows));
        };
    }

    private byte[] generatePdf(List<CashRegisterExportRow> rows, CashRegisterReportFilter filter,
                               ReportType type) {
        try (var output = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(output));
            Document document = new Document(pdf, type == ReportType.DETAILED ? PageSize.A4.rotate() : PageSize.A4);
            var bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.add(branding.logo().setMarginBottom(12));
            document.add(new Paragraph("Reporte de caja").setFont(bold).setFontSize(18));
            document.add(new Paragraph("Desde " + SPANISH_DATE_FORMAT.format(filter.dateFrom())
                    + " hasta " + SPANISH_DATE_FORMAT.format(filter.dateTo())));
            document.add(new Paragraph(reportTypeLabel(type)));
            if (type == ReportType.DETAILED) {
                addDetailedPdf(document, rows, bold);
            } else if (type == ReportType.GROUPED) {
                addGroupedPdf(document, customerSummaries(rows), bold);
            } else {
                addSimplePdf(document, totals(rows), bold);
            }
            document.close();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate cash register PDF report", exception);
        }
    }

    private void addSimplePdf(Document document, DailyTotals totals, com.itextpdf.kernel.font.PdfFont bold) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
        addHeader(table, new String[]{"Concepto", "Total"}, bold);
        addMoneyRow(table, "Facturación neta", totals.totalInvoiced());
        addMoneyRow(table, "Ingresos", totals.totalRevenue());
        addMoneyRow(table, "Egresos", totals.totalExpenses());
        addMoneyRow(table, "Balance", totals.totalRevenue().subtract(totals.totalExpenses()));
        document.add(table);
    }

    private void addGroupedPdf(Document document, List<CustomerSummary> summaries,
                               com.itextpdf.kernel.font.PdfFont bold) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{2.4f, 1, 1, 1, 1})).useAllAvailableWidth();
        addHeader(table, new String[]{"Cliente", "Facturación", "Pagado", "Devuelto", "Balance"}, bold);
        summaries.forEach(summary -> {
            table.addCell(summary.customerName());
            table.addCell(money(summary.totalInvoiced()));
            table.addCell(money(summary.totalPayments()));
            table.addCell(money(summary.totalReturns()));
            table.addCell(money(summary.balance()));
        });
        document.add(table);
    }

    private void addDetailedPdf(Document document, List<CashRegisterExportRow> rows,
                                com.itextpdf.kernel.font.PdfFont bold) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{12, 7, 15, 12, 25, 29}))
                .useAllAvailableWidth()
                .setFontSize(8);
        addHeader(table, new String[]{"Fecha", "Tipo", "Documento", "Operación", "Método", "Cliente"}, bold);
        rows.stream()
                .sorted(Comparator.comparing(CashRegisterExportRow::emissionDate)
                        .thenComparing(CashRegisterExportRow::documentType)
                        .thenComparingInt(CashRegisterExportRow::sortOrder))
                .forEach(row -> {
                    table.addCell(SPANISH_DATE_FORMAT.format(row.emissionDate()));
                    table.addCell(row.documentType());
                    table.addCell(row.documentNumber());
                    table.addCell(money(row.totalOperation()));
                    table.addCell(paymentSummary(row));
                    table.addCell(row.customerName() == null ? "-" : row.customerName());
                });
        document.add(table);
    }

    private void addHeader(Table table, String[] headers, com.itextpdf.kernel.font.PdfFont bold) {
        for (String header : headers) table.addHeaderCell(new Cell().add(new Paragraph(header).setFont(bold)));
    }

    private void addMoneyRow(Table table, String label, BigDecimal amount) {
        table.addCell(label);
        table.addCell(money(amount));
    }

    private DailyTotals totals(List<CashRegisterExportRow> rows) {
        BigDecimal payments = sum(rows, "PAG");
        BigDecimal advances = sum(rows, "ANT");
        BigDecimal returns = sum(rows, "DEV");
        BigDecimal refunds = sum(rows, "REI");
        BigDecimal creditNotes = sum(rows, "NC");
        BigDecimal debitNotes = sum(rows, "ND");
        BigDecimal invoices = sum(rows, "FAC");
        BigDecimal netInvoiced = invoices.add(debitNotes).subtract(creditNotes).subtract(returns);
        return new DailyTotals(
                payments, count(rows, "PAG"), advances, count(rows, "ANT"),
                returns, count(rows, "DEV"), creditNotes, debitNotes,
                invoices, count(rows, "FAC"), netInvoiced,
                payments.add(advances), refunds, paymentDetails(rows));
    }

    private List<CustomerSummary> customerSummaries(List<CashRegisterExportRow> rows) {
        var grouped = new LinkedHashMap<String, List<CashRegisterExportRow>>();
        rows.forEach(row -> grouped.computeIfAbsent(
                row.customerName() == null || row.customerName().isBlank() ? "Sin cliente" : row.customerName(),
                ignored -> new ArrayList<>()).add(row));
        return grouped.entrySet().stream().map(entry -> {
            List<CashRegisterExportRow> customerRows = entry.getValue();
            BigDecimal invoiced = sum(customerRows, "FAC").add(sum(customerRows, "ND"))
                    .subtract(sum(customerRows, "NC")).subtract(sum(customerRows, "DEV"));
            BigDecimal paid = sum(customerRows, "PAG").add(sum(customerRows, "ANT"));
            BigDecimal returned = sum(customerRows, "REI");
            return new CustomerSummary(entry.getKey(), invoiced, paid, returned, paid.subtract(returned));
        }).toList();
    }

    private List<CashRegisterDetail> paymentDetails(List<CashRegisterExportRow> rows) {
        List<CashRegisterDetail> details = new ArrayList<>();
        addPaymentDetail(details, rows, "Efectivo Bs.", CashRegisterExportRow::efectivoBs);
        addPaymentDetail(details, rows, "Dólar", CashRegisterExportRow::dolar);
        addPaymentDetail(details, rows, "Transferencia dólar", CashRegisterExportRow::transfDolar);
        addPaymentDetail(details, rows, "Zelle", CashRegisterExportRow::zelle);
        return details;
    }

    private void addPaymentDetail(List<CashRegisterDetail> details, List<CashRegisterExportRow> rows,
                                  String name,
                                  java.util.function.Function<CashRegisterExportRow, BigDecimal> extractor) {
        BigDecimal amount = rows.stream().map(extractor).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.signum() > 0) {
            int transactions = Math.toIntExact(rows.stream().map(extractor).filter(value -> value.signum() > 0).count());
            details.add(CashRegisterDetail.create(null, name, transactions, amount));
        }
    }

    private BigDecimal sum(List<CashRegisterExportRow> rows, String type) {
        return rows.stream().filter(row -> type.equals(row.documentType()))
                .map(CashRegisterExportRow::totalOperation).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int count(List<CashRegisterExportRow> rows, String type) {
        return Math.toIntExact(rows.stream().filter(row -> type.equals(row.documentType())).count());
    }

    private String paymentSummary(CashRegisterExportRow row) {
        List<String> parts = new ArrayList<>();
        if (row.efectivoBs().signum() > 0) parts.add("Efectivo " + money(row.efectivoBs()));
        if (row.dolar().signum() > 0) parts.add("Dólar " + money(row.dolar()));
        if (row.transfDolar().signum() > 0) parts.add("Transferencia " + money(row.transfDolar()));
        if (row.zelle().signum() > 0) parts.add("Zelle " + money(row.zelle()));
        return parts.isEmpty() ? "-" : String.join(", ", parts);
    }

    private String reportTypeLabel(ReportType type) {
        return switch (type) {
            case SIMPLE -> "Resumen simple";
            case DETAILED -> "Detalle de operaciones";
            case GROUPED -> "Agrupado por cliente";
        };
    }

    private String money(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
