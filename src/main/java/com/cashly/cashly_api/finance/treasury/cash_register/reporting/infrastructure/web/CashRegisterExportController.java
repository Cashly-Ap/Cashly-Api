package com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportFormat;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterReportFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.excel.use_case.GenerateCashRegisterExcelUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.range.use_case.GenerateCashRegisterRangeReportUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.session.use_case.GenerateCashRegisterReportUseCase;
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
@RequestMapping("/api/cash-register")
public class CashRegisterExportController {

    private static final String ATTACHMENT_HEADER = "attachment; filename=\"%s\"";

    private final GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase;
    private final GenerateCashRegisterReportUseCase generateCashRegisterReportUseCase;
    private final GenerateCashRegisterRangeReportUseCase generateCashRegisterRangeReportUseCase;

    public CashRegisterExportController(GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase,
                                        GenerateCashRegisterReportUseCase generateCashRegisterReportUseCase,
                                        GenerateCashRegisterRangeReportUseCase generateCashRegisterRangeReportUseCase) {
        this.generateCashRegisterExcelUseCase = generateCashRegisterExcelUseCase;
        this.generateCashRegisterReportUseCase = generateCashRegisterReportUseCase;
        this.generateCashRegisterRangeReportUseCase = generateCashRegisterRangeReportUseCase;
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<byte[]> exportRangeReport(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo,
            @RequestParam(defaultValue = "PDF") ReportFormat format,
            @RequestParam(defaultValue = "DETAILED") ReportType type,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId) {
        var filter = new CashRegisterReportFilter(
                dateFrom, dateTo, customerId, productId, categoryId);
        byte[] bytes = generateCashRegisterRangeReportUseCase.execute(filter, format, type);
        boolean pdf = format == ReportFormat.PDF;
        String extension = pdf ? "pdf" : "xlsx";
        String fileName = "caja-" + dateFrom + "_" + dateTo + "-"
                + type.name().toLowerCase() + "." + extension;
        MediaType contentType = pdf
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format(ATTACHMENT_HEADER, fileName))
                .contentType(contentType)
                .body(bytes);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<byte[]> exportExcel(@RequestParam LocalDate dateFrom,
                                              @RequestParam LocalDate dateTo,
                                              @RequestParam(defaultValue = "DETAILED") ReportType type) {
        byte[] excelBytes = generateCashRegisterExcelUseCase.execute(dateFrom, dateTo, type);

        String prefix = switch (type) {
            case SIMPLE -> "cierre-caja-resumen";
            case DETAILED -> "cierre-caja-detallado";
            case GROUPED -> "cierre-caja-por-cliente";
        };
        String fileName = String.format("%s-%s.xlsx", prefix, dateFrom + "_" + dateTo);
        String contentDisposition = String.format(ATTACHMENT_HEADER, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/{id}/report")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<byte[]> exportSession(
            @org.springframework.web.bind.annotation.PathVariable UUID id,
            @RequestParam(defaultValue = "PDF") ReportFormat format,
            @RequestParam(defaultValue = "DETAILED") ReportType type) {
        byte[] bytes = generateCashRegisterReportUseCase.execute(id, format, type);
        boolean pdf = format == ReportFormat.PDF;
        String extension = pdf ? "pdf" : "xlsx";
        String fileName = "caja-" + id + "-" + type.name().toLowerCase() + "." + extension;
        MediaType contentType = pdf
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format(ATTACHMENT_HEADER, fileName))
                .contentType(contentType)
                .body(bytes);
    }
}
