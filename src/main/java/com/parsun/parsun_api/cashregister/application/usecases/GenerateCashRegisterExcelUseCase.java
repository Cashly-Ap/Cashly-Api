package com.parsun.parsun_api.cashregister.application.usecases;

import java.time.LocalDate;

public interface GenerateCashRegisterExcelUseCase {
    byte[] execute(LocalDate dateFrom, LocalDate dateTo);
}
