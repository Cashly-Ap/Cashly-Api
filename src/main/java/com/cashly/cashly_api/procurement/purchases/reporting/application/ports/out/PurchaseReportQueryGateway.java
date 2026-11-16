package com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out;

import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportData;
import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportRow;
import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportFilter;
import java.util.List;
import java.util.UUID;

public interface PurchaseReportQueryGateway {
    PurchaseReportData find(UUID id);
    List<PurchaseReportRow> findAll(PurchaseReportFilter filter);
}
