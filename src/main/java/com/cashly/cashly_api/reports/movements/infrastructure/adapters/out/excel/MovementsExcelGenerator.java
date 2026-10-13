package com.cashly.cashly_api.reports.movements.infrastructure.adapters.out.excel;

import com.cashly.cashly_api.reports.movements.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.reports.movements.application.ports.out.MovementsExcelPort;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MovementsExcelGenerator implements MovementsExcelPort {

    private static final String[] HEADERS = {"Fecha", "Almacén", "Producto", "SKU", "Tipo", "Cantidad", "Stock Anterior", "Stock Nuevo", "Notas"};

    @Override
    public byte[] generateExcel(List<MovementReportResponse> items) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Sheet sheet = workbook.createSheet("Historial de Movimientos");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle cellStyle = createCellStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Historial de Movimientos");
            titleCell.setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generado el " + fecha);
            dateCell.setCellStyle(dateStyle);

            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (int i = 0; i < items.size(); i++) {
                Row row = sheet.createRow(4 + i);
                MovementReportResponse item = items.get(i);

                createStringCell(row, 0, item.createdAt() != null ? item.createdAt().format(dtf) : "-", cellStyle);
                createStringCell(row, 1, item.warehouseName(), cellStyle);
                createStringCell(row, 2, item.productName(), cellStyle);
                createStringCell(row, 3, item.sku(), cellStyle);
                createStringCell(row, 4, item.movementType(), cellStyle);
                createNumericCell(row, 5, item.quantity(), cellStyle);
                createNumericCell(row, 6, item.previousStock(), cellStyle);
                createNumericCell(row, 7, item.newStock(), cellStyle);
                createStringCell(row, 8, item.notes() != null ? item.notes() : "-", cellStyle);
            }

            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 8000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 4000);
            sheet.setColumnWidth(5, 3500);
            sheet.setColumnWidth(6, 4000);
            sheet.setColumnWidth(7, 4000);
            sheet.setColumnWidth(8, 8000);

            workbook.write(baos);
        } catch (IOException e) {
            throw new RuntimeException("Error generating movements Excel", e);
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
