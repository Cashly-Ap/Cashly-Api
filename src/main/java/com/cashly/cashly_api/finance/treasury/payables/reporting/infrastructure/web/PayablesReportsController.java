package com.cashly.cashly_api.finance.treasury.payables.reporting.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payables.reporting.application.generate_accounts_payable_pdf.use_case.GenerateAccountsPayablePdfUseCase;
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
@RequestMapping("/api/reports/finance")
public class PayablesReportsController {

    private final GenerateAccountsPayablePdfUseCase useCase;

    public PayablesReportsController(
            GenerateAccountsPayablePdfUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/accounts-payable/pdf")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_REPORT_PDF')")
    public ResponseEntity<byte[]> accountsPayablePdf(
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(defaultValue = "false") boolean overdueOnly) {
        byte[] pdf = useCase.execute(supplierId, overdueOnly);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cuentas-por-pagar-"
                                + LocalDate.now() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
