package com.cashly.cashly_api.cash_register.reporting.application.excel;

import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportType;

import java.time.LocalDate;

public interface GenerateCashRegisterExcelUseCase {
    byte[] execute(LocalDate dateFrom, LocalDate dateTo, ReportType type);
}
