package com.cashly.cashly_api.finance.treasury.payments.reporting.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_history_report_pdf.use_case.GeneratePaymentHistoryReportPdfUseCase;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_pdf.use_case.GeneratePaymentPdfUseCase;
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
public class PaymentReportsController {

    private final GeneratePaymentPdfUseCase generatePaymentPdfUseCase;
    private final GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase;

    public PaymentReportsController(GeneratePaymentPdfUseCase generatePaymentPdfUseCase,
            GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase) {
        this.generatePaymentPdfUseCase = generatePaymentPdfUseCase;
        this.generatePaymentHistoryReportPdfUseCase = generatePaymentHistoryReportPdfUseCase;
    }

    @GetMapping("/payments/{id}/pdf")
    @PreAuthorize("hasAuthority('FINANCES:EXPORT') and hasAuthority('DOCUMENT_ADMIN:EXPORT')")
    public ResponseEntity<byte[]> getPaymentPdf(@PathVariable UUID id) {
        byte[] pdf = generatePaymentPdfUseCase.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pago-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/payment-history/pdf")
    @PreAuthorize("hasAuthority('FINANCES:EXPORT') and hasAuthority('DOCUMENT_ADMIN:EXPORT')")
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
}
