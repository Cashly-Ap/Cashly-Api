package com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.excel;

import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CustomerSummary;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterExcelPort;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegisterDetail;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CashRegisterExcelGenerator implements CashRegisterExcelPort {


    private static final String MODULE_VENTAS = "VENTAS";
    private static final String MODULE_CXC = "CUENTAS POR COBRAR";
    private static final String MODULE_CAJA = "OPERACIONES DE CAJA";
    private static final String[] MODULE_ORDER = {MODULE_VENTAS, MODULE_CXC, MODULE_CAJA};

    private static final int COL_EMISSION_DATE = 0;
    private static final int COL_DOC_NUMBER = 1;
    private static final int COL_TOTAL_OPERATION = 2;
    private static final int COL_EFECTIVO = 3;
    private static final int COL_DOLAR = 4;
    private static final int COL_TRANSF_DOLAR = 5;
    private static final int COL_ZELLE = 6;
    private static final int COL_ANTICIPO = 7;
    private static final int COL_NOTA_CREDITO = 8;
    private static final int COL_NOTA_DEBITO = 9;
    private static final int COL_CUSTOMER = 10;
    private static final int COLUMN_COUNT = 11;







    @Override
    public byte[] generateSimpleExcel(List<CashRegisterDetail> paymentSummary, DailyTotals totals) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(ReportMessages.text("finance.treasury.cash_register.reports.cashRegisterSummary"));

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle subtotalStyle = createSubtotalStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            configureSimpleColumnWidths(sheet);

            int rowNum = 0;
            rowNum = writeTitle(sheet, titleStyle, rowNum);
            rowNum = writeEmptyRow(sheet, rowNum);

            rowNum = writeSectionTitle(sheet, subtotalStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.summaryByPaymentMethod"));
            rowNum = writePaymentMethodHeader(sheet, headerStyle, rowNum);

            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalTx = 0;

            for (CashRegisterDetail detail : paymentSummary) {
                Row dataRow = sheet.createRow(rowNum);
                Cell nameCell = dataRow.createCell(0);
                nameCell.setCellValue(detail.getPaymentMethodName());
                nameCell.setCellStyle(dataStyle);
                Cell txCell = dataRow.createCell(1);
                txCell.setCellValue(detail.getTransactionCount());
                txCell.setCellStyle(dataStyle);
                Cell amountCell = dataRow.createCell(2);
                amountCell.setCellValue(detail.getExpectedAmount().doubleValue());
                amountCell.setCellStyle(currencyStyle);
                dataRow.setHeightInPoints(18);
                rowNum++;

                totalAmount = totalAmount.add(detail.getExpectedAmount());
                totalTx += detail.getTransactionCount();
            }

            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue(ReportMessages.text("finance.treasury.cash_register.reports.total"));
            totalLabelCell.setCellStyle(subtotalStyle);
            Cell totalTxCell = totalRow.createCell(1);
            totalTxCell.setCellValue(totalTx);
            totalTxCell.setCellStyle(subtotalStyle);
            Cell totalAmountCell = totalRow.createCell(2);
            totalAmountCell.setCellValue(totalAmount.doubleValue());
            totalAmountCell.setCellStyle(createCurrencySubtotalStyle(workbook));
            totalRow.setHeightInPoints(20);
            rowNum += 2;

            rowNum = writeSectionTitle(sheet, subtotalStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.grandTotals"));
            rowNum = writeTotalsHeader(sheet, headerStyle, rowNum);

            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.totalInvoiced"), totals.totalInvoiced());
            rowNum++;
            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.totalReceived"), totals.totalRevenue());
            rowNum++;
            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.totalSpent"), totals.totalExpenses());
            rowNum++;
            BigDecimal balance = totals.totalRevenue().subtract(totals.totalExpenses());
            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.balance"), balance);

            return toBytes(workbook);
        } catch (IOException e) {
            throw new RuntimeException(ReportMessages.text("finance.treasury.cash_register.reports.unableToGenerateTheExcelFile"), e);
        }
    }

    @Override
    public byte[] generateGroupedExcel(List<CustomerSummary> customerSummaries) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(ReportMessages.text("finance.treasury.cash_register.reports.closureByCustomer"));

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle subtotalStyle = createSubtotalStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle currencySubtotalStyle = createCurrencySubtotalStyle(workbook);

            configureGroupedColumnWidths(sheet);

            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(ReportMessages.text("finance.treasury.cash_register.reports.cashRegisterClosureByCustomer"));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 4));
            titleRow.setHeightInPoints(30);
            rowNum += 2;

            String[] headers = {ReportMessages.text("finance.treasury.cash_register.reports.customer"), ReportMessages.text("finance.treasury.cash_register.reports.totalInvoiced"), ReportMessages.text("finance.treasury.cash_register.reports.totalPaid"), ReportMessages.text("finance.treasury.cash_register.reports.totalRefunded"), ReportMessages.text("finance.treasury.cash_register.reports.balance")};
            Row headerRow = sheet.createRow(rowNum);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            headerRow.setHeightInPoints(20);
            rowNum++;

            BigDecimal grandInvoiced = BigDecimal.ZERO;
            BigDecimal grandPaid = BigDecimal.ZERO;
            BigDecimal grandReturned = BigDecimal.ZERO;
            BigDecimal grandBalance = BigDecimal.ZERO;

            for (CustomerSummary summary : customerSummaries) {
                Row dataRow = sheet.createRow(rowNum);
                Cell nameCell = dataRow.createCell(0);
                nameCell.setCellValue(summary.customerName());
                nameCell.setCellStyle(dataStyle);
                writeCurrencyCell(dataRow, 1, summary.totalInvoiced(), currencyStyle);
                writeCurrencyCell(dataRow, 2, summary.totalPayments(), currencyStyle);
                writeCurrencyCell(dataRow, 3, summary.totalReturns(), currencyStyle);
                writeCurrencyCell(dataRow, 4, summary.balance(), currencyStyle);
                dataRow.setHeightInPoints(18);
                rowNum++;

                grandInvoiced = grandInvoiced.add(summary.totalInvoiced());
                grandPaid = grandPaid.add(summary.totalPayments());
                grandReturned = grandReturned.add(summary.totalReturns());
                grandBalance = grandBalance.add(summary.balance());
            }

            Row subtotalRow = sheet.createRow(rowNum);
            Cell subtotalLabelCell = subtotalRow.createCell(0);
            subtotalLabelCell.setCellValue(ReportMessages.text("finance.treasury.cash_register.reports.grandTotal"));
            subtotalLabelCell.setCellStyle(subtotalStyle);
            writeCurrencyCell(subtotalRow, 1, grandInvoiced, currencySubtotalStyle);
            writeCurrencyCell(subtotalRow, 2, grandPaid, currencySubtotalStyle);
            writeCurrencyCell(subtotalRow, 3, grandReturned, currencySubtotalStyle);
            writeCurrencyCell(subtotalRow, 4, grandBalance, currencySubtotalStyle);
            subtotalRow.setHeightInPoints(20);

            return toBytes(workbook);
        } catch (IOException e) {
            throw new RuntimeException(ReportMessages.text("finance.treasury.cash_register.reports.unableToGenerateTheExcelFile"), e);
        }
    }

    @Override
    public byte[] generateExcel(List<CashRegisterExportRow> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(ReportMessages.text("finance.treasury.cash_register.reports.cashRegisterClosureDetails"));

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dataDateStyle = createDataDateStyle(workbook);
            CellStyle subtotalStyle = createSubtotalStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle currencySubtotalStyle = createCurrencySubtotalStyle(workbook);

            configureColumnWidths(sheet);

            int rowNum = 0;

            rowNum = writeTitle(sheet, titleStyle, rowNum);
            rowNum++;

            if (rows.isEmpty()) {
                writeNoDataRow(sheet, dataStyle, rowNum);
                return toBytes(workbook);
            }

            rowNum = writeHeader(sheet, headerStyle, rowNum);

            Map<String, List<CashRegisterExportRow>> grouped = rows.stream()
                    .collect(Collectors.groupingBy(CashRegisterExportRow::module));

            for (String module : MODULE_ORDER) {
                List<CashRegisterExportRow> moduleRows = grouped.get(module);
                if (moduleRows == null || moduleRows.isEmpty()) {
                    continue;
                }

                moduleRows.sort(Comparator.comparingInt(CashRegisterExportRow::sortOrder));

                for (CashRegisterExportRow row : moduleRows) {
                    writeDataRow(sheet, dataStyle, dataDateStyle, currencyStyle, rowNum, row);
                    rowNum++;
                }

                BigDecimal moduleTotal = moduleRows.stream()
                        .map(CashRegisterExportRow::totalOperation)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal moduleEfectivo = moduleRows.stream()
                        .map(CashRegisterExportRow::efectivoBs)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal moduleDolar = moduleRows.stream()
                        .map(CashRegisterExportRow::dolar)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal moduleTransf = moduleRows.stream()
                        .map(CashRegisterExportRow::transfDolar)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal moduleZelle = moduleRows.stream()
                        .map(CashRegisterExportRow::zelle)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal moduleAnticipo = moduleRows.stream()
                        .map(CashRegisterExportRow::anticipoAplicado)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal moduleNC = moduleRows.stream()
                        .map(CashRegisterExportRow::notaCreditoAplicado)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal moduleND = moduleRows.stream()
                        .map(CashRegisterExportRow::notaDebito)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                writeSubtotalRow(sheet, subtotalStyle, currencySubtotalStyle, rowNum,
                        ReportMessages.text("finance.treasury.cash_register.reports.subtotal") + ReportMessages.code(module), moduleTotal,
                        moduleEfectivo, moduleDolar, moduleTransf, moduleZelle,
                        moduleAnticipo, moduleNC, moduleND);
                rowNum++;
                rowNum++;
            }

            BigDecimal grossInvoiced = rows.stream()
                    .filter(r -> "FAC".equals(r.documentType()))
                    .map(CashRegisterExportRow::totalOperation)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal debitNotes = rows.stream()
                    .filter(r -> "ND".equals(r.documentType()))
                    .map(CashRegisterExportRow::totalOperation)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal commercialReductions = rows.stream()
                    .filter(r -> "DEV".equals(r.documentType()) || "NC".equals(r.documentType()))
                    .map(CashRegisterExportRow::totalOperation)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal netInvoiced = grossInvoiced.add(debitNotes).subtract(commercialReductions);
            BigDecimal totalIncome = rows.stream()
                    .filter(r -> "PAG".equals(r.documentType()) || "ANT".equals(r.documentType()))
                    .map(CashRegisterExportRow::totalOperation)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalExpense = rows.stream()
                    .filter(r -> "REI".equals(r.documentType()))
                    .map(CashRegisterExportRow::totalOperation)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balance = totalIncome.subtract(totalExpense);

            rowNum = writeSectionTitle(sheet, subtotalStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.generalSummary"));
            rowNum = writeTotalsHeader(sheet, headerStyle, rowNum);
            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.netInvoicing"), netInvoiced);
            rowNum++;
            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.inflows"), totalIncome);
            rowNum++;
            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.outflows"), totalExpense);
            rowNum++;
            writeTotalsRow(sheet, dataStyle, currencyStyle, rowNum, ReportMessages.text("finance.treasury.cash_register.reports.balance"), balance);

            return toBytes(workbook);

        } catch (IOException e) {
            throw new RuntimeException(ReportMessages.text("finance.treasury.cash_register.reports.unableToGenerateTheExcelFile"), e);
        }
    }

    private int writeTitle(Sheet sheet, CellStyle titleStyle, int rowNum) {
        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(COL_EMISSION_DATE);
        titleCell.setCellValue(ReportMessages.text("finance.treasury.cash_register.reports.detailedCashRegisterClosureByCurrency"));
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, COL_EMISSION_DATE, COL_CUSTOMER));
        titleRow.setHeightInPoints(30);
        return rowNum + 1;
    }

    private int writeHeader(Sheet sheet, CellStyle headerStyle, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);
        String[] headers = {
                ReportMessages.text("finance.treasury.cash_register.reports.issueDate"), ReportMessages.text("finance.treasury.cash_register.reports.document"), ReportMessages.text("finance.treasury.cash_register.reports.transactionTotal"),
                ReportMessages.text("finance.treasury.cash_register.reports.cashInBs"), ReportMessages.text("finance.treasury.cash_register.reports.usDollar"), ReportMessages.text("finance.treasury.cash_register.reports.usdTransfer"), ReportMessages.text("finance.treasury.cash_register.reports.zelle"),
                ReportMessages.text("finance.treasury.cash_register.reports.advanceApplied"), ReportMessages.text("finance.treasury.cash_register.reports.creditNoteApplied"), ReportMessages.text("finance.treasury.cash_register.reports.debitNoteApplied"), ReportMessages.text("finance.treasury.cash_register.reports.customer")
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        headerRow.setHeightInPoints(20);
        return rowNum + 1;
    }

    private void writeDataRow(Sheet sheet, CellStyle dataStyle, CellStyle dateStyle,
                              CellStyle currencyStyle, int rowNum, CashRegisterExportRow row) {
        Row dataRow = sheet.createRow(rowNum);

        Cell dateCell = dataRow.createCell(COL_EMISSION_DATE);
        dateCell.setCellValue(row.emissionDate() != null
                ? row.emissionDate().format(ReportMessages.dateFormatter()) : "");
        dateCell.setCellStyle(dateStyle);

        Cell docNumCell = dataRow.createCell(COL_DOC_NUMBER);
        docNumCell.setCellValue(row.documentNumber());
        docNumCell.setCellStyle(dataStyle);

        Cell totalCell = dataRow.createCell(COL_TOTAL_OPERATION);
        totalCell.setCellValue(row.totalOperation().doubleValue());
        totalCell.setCellStyle(currencyStyle);

        Cell efectivoCell = dataRow.createCell(COL_EFECTIVO);
        efectivoCell.setCellValue(row.efectivoBs().doubleValue());
        efectivoCell.setCellStyle(currencyStyle);

        Cell dolarCell = dataRow.createCell(COL_DOLAR);
        dolarCell.setCellValue(row.dolar().doubleValue());
        dolarCell.setCellStyle(currencyStyle);

        Cell transfCell = dataRow.createCell(COL_TRANSF_DOLAR);
        transfCell.setCellValue(row.transfDolar().doubleValue());
        transfCell.setCellStyle(currencyStyle);

        Cell zelleCell = dataRow.createCell(COL_ZELLE);
        zelleCell.setCellValue(row.zelle().doubleValue());
        zelleCell.setCellStyle(currencyStyle);

        Cell anticipoCell = dataRow.createCell(COL_ANTICIPO);
        anticipoCell.setCellValue(row.anticipoAplicado().doubleValue());
        anticipoCell.setCellStyle(currencyStyle);

        Cell ncCell = dataRow.createCell(COL_NOTA_CREDITO);
        ncCell.setCellValue(row.notaCreditoAplicado().doubleValue());
        ncCell.setCellStyle(currencyStyle);

        Cell ndCell = dataRow.createCell(COL_NOTA_DEBITO);
        ndCell.setCellValue(row.notaDebito().doubleValue());
        ndCell.setCellStyle(currencyStyle);

        Cell customerCell = dataRow.createCell(COL_CUSTOMER);
        customerCell.setCellValue(row.customerName() != null ? row.customerName() : "");
        customerCell.setCellStyle(dataStyle);

        dataRow.setHeightInPoints(18);
    }

    private void writeSubtotalRow(Sheet sheet, CellStyle subtotalStyle, CellStyle currencyStyle,
                                  int rowNum, String label, BigDecimal total,
                                  BigDecimal efectivo, BigDecimal dolar, BigDecimal transf,
                                  BigDecimal zelle, BigDecimal anticipo, BigDecimal nc,
                                  BigDecimal nd) {
        Row subtotalRow = sheet.createRow(rowNum);

        Cell labelCell = subtotalRow.createCell(COL_EMISSION_DATE);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(subtotalStyle);

        for (int i = COL_DOC_NUMBER; i <= COL_CUSTOMER; i++) {
            Cell emptyCell = subtotalRow.createCell(i);
            emptyCell.setCellStyle(subtotalStyle);
        }

        Cell totalCell = subtotalRow.createCell(COL_TOTAL_OPERATION);
        totalCell.setCellValue(total.doubleValue());
        totalCell.setCellStyle(currencyStyle);

        Cell efectivoCell = subtotalRow.createCell(COL_EFECTIVO);
        efectivoCell.setCellValue(efectivo.doubleValue());
        efectivoCell.setCellStyle(currencyStyle);

        Cell dolarCell = subtotalRow.createCell(COL_DOLAR);
        dolarCell.setCellValue(dolar.doubleValue());
        dolarCell.setCellStyle(currencyStyle);

        Cell transfCell = subtotalRow.createCell(COL_TRANSF_DOLAR);
        transfCell.setCellValue(transf.doubleValue());
        transfCell.setCellStyle(currencyStyle);

        Cell zelleCell = subtotalRow.createCell(COL_ZELLE);
        zelleCell.setCellValue(zelle.doubleValue());
        zelleCell.setCellStyle(currencyStyle);

        Cell anticipoCell = subtotalRow.createCell(COL_ANTICIPO);
        anticipoCell.setCellValue(anticipo.doubleValue());
        anticipoCell.setCellStyle(currencyStyle);

        Cell ncCell = subtotalRow.createCell(COL_NOTA_CREDITO);
        ncCell.setCellValue(nc.doubleValue());
        ncCell.setCellStyle(currencyStyle);

        Cell ndCell = subtotalRow.createCell(COL_NOTA_DEBITO);
        ndCell.setCellValue(nd.doubleValue());
        ndCell.setCellStyle(currencyStyle);

        subtotalRow.setHeightInPoints(20);
    }

    private void writeNoDataRow(Sheet sheet, CellStyle dataStyle, int rowNum) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(COL_EMISSION_DATE);
        cell.setCellValue(ReportMessages.text("finance.treasury.cash_register.reports.noTransactionsFoundForTheSpecifiedDate"));
        cell.setCellStyle(dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, COL_EMISSION_DATE, COL_CUSTOMER));
    }

    private void configureColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(COL_EMISSION_DATE, 4500);
        sheet.setColumnWidth(COL_DOC_NUMBER, 5000);
        sheet.setColumnWidth(COL_TOTAL_OPERATION, 4500);
        sheet.setColumnWidth(COL_EFECTIVO, 4000);
        sheet.setColumnWidth(COL_DOLAR, 3500);
        sheet.setColumnWidth(COL_TRANSF_DOLAR, 4000);
        sheet.setColumnWidth(COL_ZELLE, 3500);
        sheet.setColumnWidth(COL_ANTICIPO, 4500);
        sheet.setColumnWidth(COL_NOTA_CREDITO, 4000);
        sheet.setColumnWidth(COL_NOTA_DEBITO, 4000);
        sheet.setColumnWidth(COL_CUSTOMER, 6000);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createCurrencySubtotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private void writeCurrencyCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private void writeTotalsRow(Sheet sheet, CellStyle dataStyle, CellStyle currencyStyle,
                                int rowNum, String label, BigDecimal value) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(dataStyle);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(currencyStyle);
        row.setHeightInPoints(18);
    }

    private int writeEmptyRow(Sheet sheet, int rowNum) {
        sheet.createRow(rowNum);
        return rowNum + 1;
    }

    private int writeSectionTitle(Sheet sheet, CellStyle style, int rowNum, String title) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 4));
        row.setHeightInPoints(22);
        return rowNum + 1;
    }

    private int writePaymentMethodHeader(Sheet sheet, CellStyle style, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);
        String[] headers = {ReportMessages.text("finance.treasury.cash_register.reports.paymentMethod"), ReportMessages.text("finance.treasury.cash_register.reports.transactions"), ReportMessages.text("finance.treasury.cash_register.reports.amount")};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        headerRow.setHeightInPoints(20);
        return rowNum + 1;
    }

    private int writeTotalsHeader(Sheet sheet, CellStyle style, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);
        String[] headers = {ReportMessages.text("finance.treasury.cash_register.reports.description"), ReportMessages.text("finance.treasury.cash_register.reports.amount")};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        headerRow.setHeightInPoints(20);
        return rowNum + 1;
    }

    private void configureSimpleColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4500);
        sheet.setColumnWidth(2, 4500);
    }

    private void configureGroupedColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4500);
        sheet.setColumnWidth(2, 4500);
        sheet.setColumnWidth(3, 4500);
        sheet.setColumnWidth(4, 4500);
    }

    private byte[] toBytes(XSSFWorkbook workbook) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(ReportMessages.text("finance.treasury.cash_register.reports.unableToGenerateTheExcelFile"), e);
        }
    }
}
