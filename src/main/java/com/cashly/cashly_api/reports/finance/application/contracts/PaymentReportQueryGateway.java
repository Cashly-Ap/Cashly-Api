package com.cashly.cashly_api.reports.finance.application.contracts;
import com.cashly.cashly_api.reports.finance.application.dto.PaymentReportData;
import java.util.UUID;
public interface PaymentReportQueryGateway { PaymentReportData find(UUID id); }
