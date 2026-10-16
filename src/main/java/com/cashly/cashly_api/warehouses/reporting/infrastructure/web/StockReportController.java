package com.cashly.cashly_api.warehouses.reporting.infrastructure.web;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.StockPdfQuery;
import com.cashly.cashly_api.warehouses.reporting.application.generate_stock_excel.GenerateStockExcelUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.generate_stock_pdf.GenerateStockPdfUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports/inventory/stock")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
public class StockReportController {

    private final GenerateStockPdfUseCase generateStockPdfUseCase;
    private final GenerateStockExcelUseCase generateStockExcelUseCase;

    public StockReportController(
            GenerateStockPdfUseCase generateStockPdfUseCase,
            GenerateStockExcelUseCase generateStockExcelUseCase) {
        this.generateStockPdfUseCase = generateStockPdfUseCase;
        this.generateStockExcelUseCase = generateStockExcelUseCase;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getStockPdf(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean withStock,
            @RequestParam(required = false) Boolean lowStock) {
        StockPdfQuery query = new StockPdfQuery(warehouseId, categoryId, withStock, lowStock);
        byte[] pdf = generateStockPdfUseCase.execute(query);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"stock-almacenes.pdf\"")
                .body(pdf);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> getStockExcel(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean withStock,
            @RequestParam(required = false) Boolean lowStock) {
        StockPdfQuery query = new StockPdfQuery(warehouseId, categoryId, withStock, lowStock);
        byte[] excel = generateStockExcelUseCase.execute(query);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"stock-almacenes.xlsx\"")
                .body(excel);
    }
}
