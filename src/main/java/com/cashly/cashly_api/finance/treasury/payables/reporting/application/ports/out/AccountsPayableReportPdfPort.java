package com.cashly.cashly_api.finance.treasury.payables.reporting.application.ports.out;

import java.util.UUID;

public interface AccountsPayableReportPdfPort {
    byte[] generate(UUID supplierId, boolean overdueOnly);
}
