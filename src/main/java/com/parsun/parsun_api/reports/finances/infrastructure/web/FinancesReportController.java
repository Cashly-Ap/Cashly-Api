package com.parsun.parsun_api.reports.finances.infrastructure.web;

import com.parsun.parsun_api.reports.finances.application.usecases.GenerateAccountsReceivableReportPdfUseCase;
import com.parsun.parsun_api.reports.finances.application.usecases.GenerateCollectionsReportPdfUseCase;
import com.parsun.parsun_api.reports.finances.application.usecases.GeneratePaymentHistoryReportPdfUseCase;
import com.parsun.parsun_api.reports.finances.application.usecases.GeneratePaymentPdfUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/finances")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
public class FinancesReportController {

    private final GeneratePaymentPdfUseCase generatePaymentPdfUseCase;
    private final GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase;
    private final GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase;
    private final GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase;

    public FinancesReportController(GeneratePaymentPdfUseCase generatePaymentPdfUseCase,
                                     GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase,
                                     GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase,
                                     GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase) {
        this.generatePaymentPdfUseCase = generatePaymentPdfUseCase;
        this.generateAccountsReceivableReportPdfUseCase = generateAccountsReceivableReportPdfUseCase;
        this.generatePaymentHistoryReportPdfUseCase = generatePaymentHistoryReportPdfUseCase;
        this.generateCollectionsReportPdfUseCase = generateCollectionsReportPdfUseCase;
    }

    @GetMapping("/payments/{id}/pdf")
    public ResponseEntity<byte[]> getPaymentPdf(@PathVariable UUID id) {
        byte[] pdf = generatePaymentPdfUseCase.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pago-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/accounts-receivable/pdf")
    public ResponseEntity<byte[]> getAccountsReceivablePdf(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "false") boolean overdueOnly) {
        byte[] pdf = generateAccountsReceivableReportPdfUseCase.execute(customerId, overdueOnly);
        String filename = "cuentas-por-cobrar-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/payment-history/pdf")
    public ResponseEntity<byte[]> getPaymentHistoryPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String method) {
        byte[] pdf = generatePaymentHistoryReportPdfUseCase.execute(dateFrom, dateTo, customerId, type, method);
        String filename = "historial-pagos-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/collections/pdf")
    public ResponseEntity<byte[]> getCollectionsPdf(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate agingFrom) {
        byte[] pdf = generateCollectionsReportPdfUseCase.execute(customerId, agingFrom);
        String filename = "cobranzas-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
