package com.cashly.cashly_api.finance.billing.invoices.reporting.infrastructure.web;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportFilter;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoice_pdf.use_case.GenerateInvoicePdfUseCase;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoices_report_pdf.use_case.GenerateInvoicesReportPdfUseCase;
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
public class InvoicesReportController {

    private final GenerateInvoicePdfUseCase generateInvoicePdfUseCase;
    private final GenerateInvoicesReportPdfUseCase generateInvoicesReportPdfUseCase;


    public InvoicesReportController(GenerateInvoicePdfUseCase generateInvoicePdfUseCase,
                                     GenerateInvoicesReportPdfUseCase generateInvoicesReportPdfUseCase) {
        this.generateInvoicePdfUseCase = generateInvoicePdfUseCase;
        this.generateInvoicesReportPdfUseCase = generateInvoicesReportPdfUseCase;

    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('FINANCES:EXPORT') and hasAuthority('DOCUMENT_ADMIN:EXPORT')")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable UUID id) {
        byte[] pdf = generateInvoicePdfUseCase.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nota-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAuthority('FINANCES:EXPORT') and hasAuthority('DOCUMENT_ADMIN:EXPORT')")
    public ResponseEntity<byte[]> getInvoicesReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "SIMPLE") String mode) {

        InvoiceReportFilter filter = new InvoiceReportFilter(dateFrom, dateTo, customerId, status, productId, categoryId);
        byte[] pdf = generateInvoicesReportPdfUseCase.execute(filter, mode);

        String filename = "reporte-facturas-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
