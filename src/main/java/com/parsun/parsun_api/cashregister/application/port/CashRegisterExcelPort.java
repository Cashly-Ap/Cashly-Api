package com.parsun.parsun_api.cashregister.application.port;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;

import java.util.List;

public interface CashRegisterExcelPort {
    byte[] generateExcel(List<CashRegisterExportRow> items);
}
