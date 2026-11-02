package com.cashly.cashly_api.inventory.warehouses.reporting.infrastructure.web;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.MovementsPdfQuery;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_movements_excel.use_case.GenerateMovementsExcelUseCase;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_movements_pdf.use_case.GenerateMovementsPdfUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/inventory/movements")
public class MovementsReportController {

    private final GenerateMovementsPdfUseCase generateMovementsPdfUseCase;
    private final GenerateMovementsExcelUseCase generateMovementsExcelUseCase;

    public MovementsReportController(
            GenerateMovementsPdfUseCase generateMovementsPdfUseCase,
            GenerateMovementsExcelUseCase generateMovementsExcelUseCase) {
        this.generateMovementsPdfUseCase = generateMovementsPdfUseCase;
        this.generateMovementsExcelUseCase = generateMovementsExcelUseCase;
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES:EXPORT')")
    public ResponseEntity<byte[]> getMovementsPdf(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        MovementsPdfQuery query = new MovementsPdfQuery(warehouseId, productId,
                movementType, dateFrom, dateTo);
        byte[] pdf = generateMovementsPdfUseCase.execute(query);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"historial-movimientos.pdf\"")
                .body(pdf);
    }

    @GetMapping("/excel")
    @PreAuthorize("hasAuthority('INVENTORY_WAREHOUSES:EXPORT')")
    public ResponseEntity<byte[]> getMovementsExcel(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        MovementsPdfQuery query = new MovementsPdfQuery(warehouseId, productId,
                movementType, dateFrom, dateTo);
        byte[] excel = generateMovementsExcelUseCase.execute(query);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"historial-movimientos.xlsx\"")
                .body(excel);
    }
}
