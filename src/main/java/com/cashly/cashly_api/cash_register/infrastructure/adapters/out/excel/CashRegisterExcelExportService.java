package com.cashly.cashly_api.cash_register.infrastructure.adapters.out.excel;

import com.cashly.cashly_api.cash_register.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CashRegisterExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String MODULE_VENTAS = "VENTAS";
    private static final String MODULE_CXC = "CUENTAS POR COBRAR";
    private static final String MODULE_CAJA = "OPERACIONES DE CAJA";
    private static final String[] MODULE_ORDER = {MODULE_VENTAS, MODULE_CXC, MODULE_CAJA};

    private static final int COL_EMISSION_DATE = 0;
    private static final int COL_DOC_TYPE = 1;
    private static final int COL_DOC_NUMBER = 2;
    private static final int COL_TOTAL_OPERATION = 3;
    private static final int COL_EFECTIVO = 4;
    private static final int COL_DOLAR = 5;
    private static final int COL_TRANSF_DOLAR = 6;
    private static final int COL_ZELLE = 7;
    private static final int COL_ANTICIPO = 8;
    private static final int COL_NOTA_CREDITO = 9;
    private static final int COL_CUSTOMER = 10;
    private static final int COLUMN_COUNT = 11;

    private static final String SHEET_NAME = "Detalle Cierre de Caja";
    private static final String TITLE_TEXT = "CIERRE DE CAJA DETALLADO CON DIVISAS";

    private static final String MSG_NO_DATA = "No se encontraron transacciones para la fecha especificada";
    private static final String ERROR_EXCEL = "Error al generar el archivo Excel";

    private final CashRegisterQueryGateway repository;

    public CashRegisterExcelExportService(CashRegisterQueryGateway repository) {
        this.repository = repository;
    }

    public byte[] exportToExcel(LocalDate dateFrom, LocalDate dateTo) {
        List<CashRegisterExportRow> rows = repository.findExportRows(dateFrom, dateTo);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

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

            BigDecimal grandTotal = BigDecimal.ZERO;
            BigDecimal grandEfectivo = BigDecimal.ZERO;
            BigDecimal grandDolar = BigDecimal.ZERO;
            BigDecimal grandTransf = BigDecimal.ZERO;
            BigDecimal grandZelle = BigDecimal.ZERO;
            BigDecimal grandAnticipo = BigDecimal.ZERO;
            BigDecimal grandNC = BigDecimal.ZERO;

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

                writeSubtotalRow(sheet, subtotalStyle, currencySubtotalStyle, rowNum,
                        "SUBTOTAL " + module, moduleTotal,
                        moduleEfectivo, moduleDolar, moduleTransf, moduleZelle,
                        moduleAnticipo, moduleNC);
                rowNum++;

                grandTotal = grandTotal.add(moduleTotal);
                grandEfectivo = grandEfectivo.add(moduleEfectivo);
                grandDolar = grandDolar.add(moduleDolar);
                grandTransf = grandTransf.add(moduleTransf);
                grandZelle = grandZelle.add(moduleZelle);
                grandAnticipo = grandAnticipo.add(moduleAnticipo);
                grandNC = grandNC.add(moduleNC);

                rowNum++;
            }

            writeSubtotalRow(sheet, subtotalStyle, currencySubtotalStyle, rowNum,
                    "TOTAL GENERAL", grandTotal,
                    grandEfectivo, grandDolar, grandTransf, grandZelle,
                    grandAnticipo, grandNC);

            return toBytes(workbook);

        } catch (IOException e) {
            throw new RuntimeException(ERROR_EXCEL, e);
        }
    }

    private int writeTitle(Sheet sheet, CellStyle titleStyle, int rowNum) {
        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(COL_EMISSION_DATE);
        titleCell.setCellValue(TITLE_TEXT);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, COL_EMISSION_DATE, COL_CUSTOMER));
        titleRow.setHeightInPoints(30);
        return rowNum + 1;
    }

    private int writeHeader(Sheet sheet, CellStyle headerStyle, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);
        String[] headers = {
                "Fecha Emisión", "Tipo Doc.", "Documento", "Total Operación",
                "Efectivo Bs.", "Dólar", "Transf.Dólar", "Zelle",
                "Anticipo Aplicado", "N/C Aplicado", "Cliente"
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
                ? row.emissionDate().format(DATE_FORMATTER) : "");
        dateCell.setCellStyle(dateStyle);

        Cell docTypeCell = dataRow.createCell(COL_DOC_TYPE);
        docTypeCell.setCellValue(row.documentType());
        docTypeCell.setCellStyle(dataStyle);

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

        Cell customerCell = dataRow.createCell(COL_CUSTOMER);
        customerCell.setCellValue(row.customerName() != null ? row.customerName() : "");
        customerCell.setCellStyle(dataStyle);

        dataRow.setHeightInPoints(18);
    }

    private void writeSubtotalRow(Sheet sheet, CellStyle subtotalStyle, CellStyle currencyStyle,
                                  int rowNum, String label, BigDecimal total,
                                  BigDecimal efectivo, BigDecimal dolar, BigDecimal transf,
                                  BigDecimal zelle, BigDecimal anticipo, BigDecimal nc) {
        Row subtotalRow = sheet.createRow(rowNum);

        Cell labelCell = subtotalRow.createCell(COL_EMISSION_DATE);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(subtotalStyle);

        for (int i = COL_DOC_TYPE; i <= COL_CUSTOMER; i++) {
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

        subtotalRow.setHeightInPoints(20);
    }

    private void writeNoDataRow(Sheet sheet, CellStyle dataStyle, int rowNum) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(COL_EMISSION_DATE);
        cell.setCellValue(MSG_NO_DATA);
        cell.setCellStyle(dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, COL_EMISSION_DATE, COL_CUSTOMER));
    }

    private void configureColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(COL_EMISSION_DATE, 3500);
        sheet.setColumnWidth(COL_DOC_TYPE, 3000);
        sheet.setColumnWidth(COL_DOC_NUMBER, 5000);
        sheet.setColumnWidth(COL_TOTAL_OPERATION, 4500);
        sheet.setColumnWidth(COL_EFECTIVO, 4000);
        sheet.setColumnWidth(COL_DOLAR, 3500);
        sheet.setColumnWidth(COL_TRANSF_DOLAR, 4000);
        sheet.setColumnWidth(COL_ZELLE, 3500);
        sheet.setColumnWidth(COL_ANTICIPO, 4500);
        sheet.setColumnWidth(COL_NOTA_CREDITO, 4000);
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

    private byte[] toBytes(XSSFWorkbook workbook) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(ERROR_EXCEL, e);
        }
    }
}
