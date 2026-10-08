package com.cashly.cashly_api.reports.finance.application.usecases;

import com.cashly.cashly_api.reports.finance.application.dto.PaymentReportData;
import com.cashly.cashly_api.reports.finance.application.contracts.PaymentReportQueryGateway;
import com.cashly.cashly_api.reports.finance.application.port.PaymentPdfPort;

import java.util.UUID;

public class GeneratePaymentPdfUseCaseImpl implements GeneratePaymentPdfUseCase {

    private final PaymentReportQueryGateway paymentQuery;
    private final PaymentPdfPort paymentPdfPort;

    public GeneratePaymentPdfUseCaseImpl(PaymentReportQueryGateway paymentQuery,
                                          PaymentPdfPort paymentPdfPort) {
        this.paymentQuery = paymentQuery;
        this.paymentPdfPort = paymentPdfPort;
    }

    @Override
    public byte[] execute(UUID paymentId) {
        PaymentReportData payment = paymentQuery.find(paymentId);
        return paymentPdfPort.generatePaymentPdf(payment);
    }
}
