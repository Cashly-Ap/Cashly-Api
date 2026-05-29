package com.parsun.parsun_api.reports.invoices.infrastructure.web;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoicesUseCase;
import com.parsun.parsun_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCase;
import com.parsun.parsun_api.reports.invoices.application.usecases.GenerateInvoicesReportPdfUseCase;
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
@RequestMapping("/api/reports/invoices")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
public class InvoicesReportController {

    private final GenerateInvoicePdfUseCase generateInvoicePdfUseCase;
    private final GenerateInvoicesReportPdfUseCase generateInvoicesReportPdfUseCase;
    private final GetInvoicesUseCase getInvoicesUseCase;

    public InvoicesReportController(GenerateInvoicePdfUseCase generateInvoicePdfUseCase,
                                     GenerateInvoicesReportPdfUseCase generateInvoicesReportPdfUseCase,
                                     GetInvoicesUseCase getInvoicesUseCase) {
        this.generateInvoicePdfUseCase = generateInvoicePdfUseCase;
        this.generateInvoicesReportPdfUseCase = generateInvoicesReportPdfUseCase;
        this.getInvoicesUseCase = getInvoicesUseCase;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable UUID id) {
        byte[] pdf = generateInvoicePdfUseCase.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getInvoicesReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "SIMPLE") String mode) {

        List<InvoiceListResponse> invoices = getInvoicesUseCase.execute(
                dateFrom, dateTo, customerId, status, null, productId, categoryId, "createdAt", "desc");
        byte[] pdf = generateInvoicesReportPdfUseCase.execute(invoices, mode);

        String filename = "reporte-facturas-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
