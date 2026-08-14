package com.parsun.parsun_api.cashregister.application.port;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.application.dto.ReportType;

public interface CashRegisterReportPort {

    byte[] generateExcel(CashRegisterResponse register, ReportType type);

    byte[] generatePdf(CashRegisterResponse register, ReportType type);
}
