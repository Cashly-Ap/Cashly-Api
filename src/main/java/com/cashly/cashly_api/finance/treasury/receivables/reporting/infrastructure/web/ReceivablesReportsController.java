package com.cashly.cashly_api.finance.treasury.receivables.reporting.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_accounts_receivable_report_pdf.use_case.GenerateAccountsReceivableReportPdfUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_collections_report_pdf.use_case.GenerateCollectionsReportPdfUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/finance")
public class ReceivablesReportsController {

    private final GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase;
    private final GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase;

    public ReceivablesReportsController(
            GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase,
            GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase) {
        this.generateAccountsReceivableReportPdfUseCase = generateAccountsReceivableReportPdfUseCase;
        this.generateCollectionsReportPdfUseCase = generateCollectionsReportPdfUseCase;
    }

    @GetMapping("/accounts-receivable/pdf")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_RECEIVABLES_REPORT_PDF')")
    public ResponseEntity<byte[]> getAccountsReceivablePdf(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "false") boolean overdueOnly) {
        byte[] pdf = generateAccountsReceivableReportPdfUseCase
                .execute(customerId, overdueOnly);
        String filename = "cuentas-por-cobrar-" + LocalDate.now().toString()
                + ".pdf";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/collections/pdf")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_RECEIVABLES_REPORT_PDF')")
    public ResponseEntity<byte[]> getCollectionsPdf(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate agingFrom) {
        byte[] pdf = generateCollectionsReportPdfUseCase.execute(customerId,
                agingFrom);
        String filename = "cobranzas-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
