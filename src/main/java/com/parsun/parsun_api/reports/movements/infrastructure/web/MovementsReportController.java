package com.parsun.parsun_api.reports.movements.infrastructure.web;

import com.parsun.parsun_api.reports.movements.application.query.MovementsPdfQuery;
import com.parsun.parsun_api.reports.movements.application.usecases.GenerateMovementsPdfUseCase;
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
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
public class MovementsReportController {

    private final GenerateMovementsPdfUseCase generateMovementsPdfUseCase;

    public MovementsReportController(GenerateMovementsPdfUseCase generateMovementsPdfUseCase) {
        this.generateMovementsPdfUseCase = generateMovementsPdfUseCase;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getMovementsPdf(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        MovementsPdfQuery query = new MovementsPdfQuery(warehouseId, productId, movementType, dateFrom, dateTo);
        byte[] pdf = generateMovementsPdfUseCase.execute(query);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historial-movimientos.pdf\"")
                .body(pdf);
    }
}
