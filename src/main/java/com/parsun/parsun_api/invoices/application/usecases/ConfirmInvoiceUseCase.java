package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConfirmInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;

    public ConfirmInvoiceUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        invoice.confirm();

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceMapper.toDetailResponse(saved);
    }
}
