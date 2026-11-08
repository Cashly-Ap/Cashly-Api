package com.cashly.cashly_api.finance.billing.invoices.application.confirm.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

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
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

@Service
public class ConfirmInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final IssueInvoiceService issuance;

    private final ActivityNotificationRecorder activityNotifications;

    public ConfirmInvoiceUseCase(InvoiceRepository invoiceRepository,
            IssueInvoiceService issuance,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.invoiceRepository = invoiceRepository;
        this.issuance = issuance;
    }

    @Transactional
    @Audited(module = "finance", entityType = "INVOICE", action = "CONFIRM")
    public InvoiceDetailResponse execute(UUID id, ConfirmInvoiceRequest request,
            UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        Invoice saved = issuance.issue(invoice,
                new IssueInvoiceCommand(request.confirmationPassword(),
                        request.useBalance(), request.balanceAmount(),
                        request.paymentMethodId()),
                InvoiceOrigin.MANUAL_DRAFT, authenticatedUserId);
        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_INVOICES",
                "CONFIRM", "INVOICE", saved.getId().value(),
                saved.getInvoiceNumber().value(), authenticatedUserId));
        return InvoiceMapper.toDetailResponse(saved);
    }
}
