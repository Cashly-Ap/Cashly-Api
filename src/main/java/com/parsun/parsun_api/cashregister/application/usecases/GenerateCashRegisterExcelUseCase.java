package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.ReportType;

import java.time.LocalDate;

public interface GenerateCashRegisterExcelUseCase {
    byte[] execute(LocalDate dateFrom, LocalDate dateTo, ReportType type);
}
