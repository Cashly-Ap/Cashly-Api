package com.cashly.cashly_api.finance.treasury.payments.reporting.application.ports.out;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.dto.model.PaymentReportData;
import java.util.UUID;
public interface PaymentReportQueryGateway {
    PaymentReportData find(UUID id);
}
