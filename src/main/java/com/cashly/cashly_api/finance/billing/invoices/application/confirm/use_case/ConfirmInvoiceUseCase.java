package com.cashly.cashly_api.finance.billing.invoices.application.confirm.use_case;

import com.cashly.cashly_api.finance.billing.invoices.application.confirm.request.ConfirmInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConfirmInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final IssueInvoiceService issuance;

    public ConfirmInvoiceUseCase(InvoiceRepository invoiceRepository,
            IssueInvoiceService issuance) {
        this.invoiceRepository = invoiceRepository;
        this.issuance = issuance;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, ConfirmInvoiceRequest request,
            UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        Invoice saved = issuance.issue(invoice,
                new IssueInvoiceCommand(request.confirmationPassword(),
                        request.useBalance(), request.balanceAmount(),
                        request.paymentMethodId()),
                InvoiceOrigin.MANUAL_DRAFT, authenticatedUserId);
        return InvoiceMapper.toDetailResponse(saved);
    }
}
