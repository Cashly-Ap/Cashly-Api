package com.parsun.parsun_api.reports.finance.application.usecases;

import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.GetPaymentByIdUseCase;
import com.parsun.parsun_api.reports.finance.application.port.PaymentPdfPort;

import java.util.UUID;

public class GeneratePaymentPdfUseCaseImpl implements GeneratePaymentPdfUseCase {

    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final PaymentPdfPort paymentPdfPort;

    public GeneratePaymentPdfUseCaseImpl(GetPaymentByIdUseCase getPaymentByIdUseCase,
                                          PaymentPdfPort paymentPdfPort) {
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.paymentPdfPort = paymentPdfPort;
    }

    @Override
    public byte[] execute(UUID paymentId) {
        PaymentResponse payment = getPaymentByIdUseCase.execute(paymentId);
        return paymentPdfPort.generatePaymentPdf(payment);
    }
}
