package com.cashly.cashly_api.purchases.reporting.application.ports.out;

import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportData;
import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportRow;
import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportFilter;
import java.util.List;
import java.util.UUID;

public interface PurchaseReportQueryGateway {
    PurchaseReportData find(UUID id);
    List<PurchaseReportRow> findAll(PurchaseReportFilter filter);
}
