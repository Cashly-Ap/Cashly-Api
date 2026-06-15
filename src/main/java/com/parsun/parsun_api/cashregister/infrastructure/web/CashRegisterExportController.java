package com.parsun.parsun_api.cashregister.infrastructure.web;

import com.parsun.parsun_api.cashregister.application.usecases.GenerateCashRegisterExcelUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/cash-register")
public class CashRegisterExportController {

    private static final String FILE_NAME_PATTERN = "cierre-caja-detallado-%s.xlsx";
    private static final String ATTACHMENT_HEADER = "attachment; filename=\"%s\"";

    private final GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase;

    public CashRegisterExportController(GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase) {
        this.generateCashRegisterExcelUseCase = generateCashRegisterExcelUseCase;
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<byte[]> exportExcel(@RequestParam LocalDate dateFrom,
                                              @RequestParam LocalDate dateTo) {
        byte[] excelBytes = generateCashRegisterExcelUseCase.execute(dateFrom, dateTo);

        String fileName = String.format(FILE_NAME_PATTERN, dateFrom + "_" + dateTo);
        String contentDisposition = String.format(ATTACHMENT_HEADER, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
