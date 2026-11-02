package com.cashly.cashly_api.sales.quotes.reporting.infrastructure.web;

import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportFilter;

import com.cashly.cashly_api.sales.quotes.reporting.application.generate_quote_pdf.use_case.GenerateQuotePdfUseCase;
import com.cashly.cashly_api.sales.quotes.reporting.application.generate_quotes_report_pdf.use_case.GenerateQuotesReportPdfUseCase;
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
@RequestMapping("/api/reports/quotes")
public class QuotesReportController {

    private final GenerateQuotePdfUseCase generateQuotePdfUseCase;
    private final GenerateQuotesReportPdfUseCase generateQuotesReportPdfUseCase;

    public QuotesReportController(
            GenerateQuotePdfUseCase generateQuotePdfUseCase,
            GenerateQuotesReportPdfUseCase generateQuotesReportPdfUseCase) {
        this.generateQuotePdfUseCase = generateQuotePdfUseCase;
        this.generateQuotesReportPdfUseCase = generateQuotesReportPdfUseCase;

    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('SALES_QUOTES:EXPORT')")
    public ResponseEntity<byte[]> getQuotePdf(@PathVariable UUID id) {
        byte[] pdf = generateQuotePdfUseCase.execute(id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"presupuesto-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAuthority('SALES_QUOTES:EXPORT')")
    public ResponseEntity<byte[]> getQuotesReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "SIMPLE") String mode) {

        QuoteReportFilter filter = new QuoteReportFilter(dateFrom, dateTo,
                customerId, status, productId, categoryId);
        byte[] pdf = generateQuotesReportPdfUseCase.execute(filter, mode);

        String filename = "reporte-presupuestos-" + LocalDate.now().toString()
                + ".pdf";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
