package com.cashly.cashly_api.cash_register.reporting.application.ports.out;

import com.cashly.cashly_api.cash_register.sessions.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportType;

public interface CashRegisterReportPort {

    byte[] generateExcel(CashRegisterResponse register, ReportType type);

    byte[] generatePdf(CashRegisterResponse register, ReportType type);
}
