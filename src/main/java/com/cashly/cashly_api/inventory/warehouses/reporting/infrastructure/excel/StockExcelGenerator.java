package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.excel;

import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.StockReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.StockExcelPort;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StockExcelGenerator implements StockExcelPort {

    private static String[] headers() { return new String[]{ReportMessages.text("inventory.warehouses.reports.warehouse"), ReportMessages.text("inventory.warehouses.reports.product"), ReportMessages.text("inventory.warehouses.reports.sku"), ReportMessages.text("inventory.warehouses.reports.category"), ReportMessages.text("inventory.warehouses.reports.currentStock"), ReportMessages.text("inventory.warehouses.reports.status")}; }

    @Override
    public byte[] generateExcel(List<StockReportResponse> items) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Sheet sheet = workbook.createSheet(ReportMessages.text("inventory.warehouses.reports.warehouseStock"));

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle cellStyle = createCellStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(ReportMessages.text("inventory.warehouses.reports.warehouseStock"));
            titleCell.setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers().length - 1));

            String fecha = LocalDate.now().format(ReportMessages.dateFormatter());
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue(ReportMessages.text("inventory.warehouses.reports.generatedOn") + fecha);
            dateCell.setCellStyle(dateStyle);

            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers().length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers()[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < items.size(); i++) {
                Row row = sheet.createRow(4 + i);
                StockReportResponse item = items.get(i);

                createStringCell(row, 0, item.warehouseName(), cellStyle);
                createStringCell(row, 1, item.productName(), cellStyle);
                createStringCell(row, 2, item.sku(), cellStyle);
                createStringCell(row, 3, item.categoryName(), cellStyle);
                createNumericCell(row, 4, item.quantity(), cellStyle);
                createStringCell(row, 5, ReportMessages.code(item.status()), cellStyle);
            }

            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 6000);
            sheet.setColumnWidth(4, 4000);
            sheet.setColumnWidth(5, 4000);

            workbook.write(baos);
        } catch (IOException e) {
            throw new RuntimeException("Error generating stock Excel", e);
        } finally {
            workbook.dispose();
        }

        return baos.toByteArray();
    }

    private CellStyle createTitleStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCellStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createStringCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
