package com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchases_report_pdf.use_case;

import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportFilter;

public interface GeneratePurchasesReportPdfUseCase {
    byte[] execute(PurchaseReportFilter filter, String mode);
}
