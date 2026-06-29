package com.parsun.parsun_api.reports.purchases.infrastructure.web;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseListResponse;
import com.parsun.parsun_api.purchases.application.usecases.ListPurchasesUseCase;
import com.parsun.parsun_api.reports.purchases.application.usecases.GeneratePurchasePdfUseCase;
import com.parsun.parsun_api.reports.purchases.application.usecases.GeneratePurchasesReportPdfUseCase;
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
    private final ListPurchasesUseCase listPurchasesUseCase;

    public PurchasesReportController(GeneratePurchasePdfUseCase generatePurchasePdfUseCase,
                                      GeneratePurchasesReportPdfUseCase generatePurchasesReportPdfUseCase,
                                      ListPurchasesUseCase listPurchasesUseCase) {
        this.generatePurchasePdfUseCase = generatePurchasePdfUseCase;
        this.generatePurchasesReportPdfUseCase = generatePurchasesReportPdfUseCase;
        this.listPurchasesUseCase = listPurchasesUseCase;
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

        List<PurchaseListResponse> purchases = listPurchasesUseCase.execute(
                dateFrom, dateTo, supplierId, status, productId, categoryId, "createdAt", "desc");
        byte[] pdf = generatePurchasesReportPdfUseCase.execute(purchases, mode);

        String filename = "reporte-compras-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
