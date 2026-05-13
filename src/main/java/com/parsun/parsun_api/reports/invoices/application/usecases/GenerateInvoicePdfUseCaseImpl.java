package com.parsun.parsun_api.reports.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoiceByIdUseCase;
import com.parsun.parsun_api.reports.invoices.application.port.InvoicePdfPort;

import java.util.UUID;

public class GenerateInvoicePdfUseCaseImpl implements GenerateInvoicePdfUseCase {

    private final GetInvoiceByIdUseCase getInvoiceByIdUseCase;
    private final InvoicePdfPort invoicePdfPort;

    public GenerateInvoicePdfUseCaseImpl(GetInvoiceByIdUseCase getInvoiceByIdUseCase,
                                          InvoicePdfPort invoicePdfPort) {
        this.getInvoiceByIdUseCase = getInvoiceByIdUseCase;
        this.invoicePdfPort = invoicePdfPort;
    }

    @Override
    public byte[] execute(UUID invoiceId) {
        InvoiceDetailResponse invoice = getInvoiceByIdUseCase.execute(invoiceId);
        return invoicePdfPort.generatePdf(invoice);
    }
}
