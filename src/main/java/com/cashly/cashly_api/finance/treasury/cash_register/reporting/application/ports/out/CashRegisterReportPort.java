package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;

public interface CashRegisterReportPort {

    byte[] generateExcel(CashRegisterResponse register, ReportType type);

    byte[] generatePdf(CashRegisterResponse register, ReportType type);
}
