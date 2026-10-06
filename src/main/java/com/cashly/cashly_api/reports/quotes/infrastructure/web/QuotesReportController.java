package com.cashly.cashly_api.reports.quotes.infrastructure.web;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.quotes.application.usecases.GetQuotesUseCase;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotePdfUseCase;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotesReportPdfUseCase;
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
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
public class QuotesReportController {

    private final GenerateQuotePdfUseCase generateQuotePdfUseCase;
    private final GenerateQuotesReportPdfUseCase generateQuotesReportPdfUseCase;
    private final GetQuotesUseCase getQuotesUseCase;

    public QuotesReportController(GenerateQuotePdfUseCase generateQuotePdfUseCase,
                                   GenerateQuotesReportPdfUseCase generateQuotesReportPdfUseCase,
                                   GetQuotesUseCase getQuotesUseCase) {
        this.generateQuotePdfUseCase = generateQuotePdfUseCase;
        this.generateQuotesReportPdfUseCase = generateQuotesReportPdfUseCase;
        this.getQuotesUseCase = getQuotesUseCase;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getQuotePdf(@PathVariable UUID id) {
        byte[] pdf = generateQuotePdfUseCase.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"presupuesto-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getQuotesReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "SIMPLE") String mode) {

        List<QuoteListResponse> quotes = getQuotesUseCase.execute(
                dateFrom, dateTo, customerId, status, productId, categoryId, "createdAt", "desc");
        byte[] pdf = generateQuotesReportPdfUseCase.execute(quotes, mode);

        String filename = "reporte-presupuestos-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
