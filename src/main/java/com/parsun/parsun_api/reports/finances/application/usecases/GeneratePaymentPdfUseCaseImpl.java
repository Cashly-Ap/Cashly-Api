package com.parsun.parsun_api.reports.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PaymentDetailResponse;
import com.parsun.parsun_api.finances.application.usecases.GetPaymentByIdUseCase;
import com.parsun.parsun_api.reports.finances.application.port.PaymentPdfPort;

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
        PaymentDetailResponse payment = getPaymentByIdUseCase.execute(paymentId);
        return paymentPdfPort.generatePaymentPdf(payment);
    }
}
