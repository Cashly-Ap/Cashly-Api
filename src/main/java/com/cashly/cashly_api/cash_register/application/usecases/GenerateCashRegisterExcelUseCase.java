package com.cashly.cashly_api.cash_register.application.usecases;

import com.cashly.cashly_api.cash_register.application.dto.ReportType;

import java.time.LocalDate;

public interface GenerateCashRegisterExcelUseCase {
    byte[] execute(LocalDate dateFrom, LocalDate dateTo, ReportType type);
}
