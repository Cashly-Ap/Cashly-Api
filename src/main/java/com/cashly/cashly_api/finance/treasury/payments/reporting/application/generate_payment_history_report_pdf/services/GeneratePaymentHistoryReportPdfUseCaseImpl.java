package com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_history_report_pdf.services;

import com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_history_report_pdf.use_case.GeneratePaymentHistoryReportPdfUseCase;

import com.cashly.cashly_api.finance.treasury.payments.reporting.application.dto.model.PaymentHistoryReportData;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.ports.out.PaymentHistoryReportPdfPort;

import java.time.LocalDate;
import java.util.UUID;

public class GeneratePaymentHistoryReportPdfUseCaseImpl
        implements
            GeneratePaymentHistoryReportPdfUseCase {

    private final PaymentHistoryReportPdfPort pdfPort;

    public GeneratePaymentHistoryReportPdfUseCaseImpl(
            PaymentHistoryReportPdfPort pdfPort) {
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(LocalDate dateFrom, LocalDate dateTo, UUID customerId,
            String type, String method) {
        PaymentHistoryReportData data = buildReportData(dateFrom, dateTo,
                customerId, type, method);
        return pdfPort.generateReport(data);
    }

    private PaymentHistoryReportData buildReportData(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String type, String method) {
        return new PaymentHistoryReportData(dateFrom, dateTo, customerId, type,
                method, java.util.List.of(),
                new PaymentHistoryReportData.Summary(0,
                        java.math.BigDecimal.ZERO, 0, 0));
    }
}
