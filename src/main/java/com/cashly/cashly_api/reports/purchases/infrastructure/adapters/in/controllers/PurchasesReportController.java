package com.cashly.cashly_api.reports.purchases.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportFilter;

import com.cashly.cashly_api.reports.purchases.application.usecases.GeneratePurchasePdfUseCase;
import com.cashly.cashly_api.reports.purchases.application.usecases.GeneratePurchasesReportPdfUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/purchases")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
public class PurchasesReportController {

    private final GeneratePurchasePdfUseCase generatePurchasePdfUseCase;
    private final GeneratePurchasesReportPdfUseCase generatePurchasesReportPdfUseCase;


    public PurchasesReportController(GeneratePurchasePdfUseCase generatePurchasePdfUseCase,
                                      GeneratePurchasesReportPdfUseCase generatePurchasesReportPdfUseCase) {
        this.generatePurchasePdfUseCase = generatePurchasePdfUseCase;
        this.generatePurchasesReportPdfUseCase = generatePurchasesReportPdfUseCase;

    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPurchasePdf(@PathVariable UUID id) {
        byte[] pdf = generatePurchasePdfUseCase.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compra-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getPurchasesReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "SIMPLE") String mode) {

        PurchaseReportFilter filter = new PurchaseReportFilter(dateFrom, dateTo, supplierId, status, productId, categoryId);
        byte[] pdf = generatePurchasesReportPdfUseCase.execute(filter, mode);

        String filename = "reporte-compras-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
