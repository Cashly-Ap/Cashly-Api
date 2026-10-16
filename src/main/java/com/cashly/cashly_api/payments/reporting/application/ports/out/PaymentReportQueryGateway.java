package com.cashly.cashly_api.payments.reporting.application.ports.out;
import com.cashly.cashly_api.payments.reporting.application.dto.model.PaymentReportData;
import java.util.UUID;
public interface PaymentReportQueryGateway { PaymentReportData find(UUID id); }
