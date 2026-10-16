package com.cashly.cashly_api.invoices.application.confirm;

import com.cashly.cashly_api.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.invoices.application.ports.out.InvoiceNotificationGateway;
import com.cashly.cashly_api.invoices.application.confirm.ConfirmInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.invoices.domain.Invoice;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConfirmInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InventoryGateway inventoryGateway;
    private final ActorIdentityGateway actorIdentityGateway;
    private final InvoiceAuthorizationService authorizationService;
    private final InvoiceNotificationGateway notificationService;
    private final CashRegisterGateway cashRegisterGateway;

    public ConfirmInvoiceUseCase(InvoiceRepository invoiceRepository,
                                  InventoryGateway inventoryGateway,
                                  InvoiceAuthorizationService authorizationService,
                                  ActorIdentityGateway actorIdentityGateway,
                                  InvoiceNotificationGateway notificationService,
                                  CashRegisterGateway cashRegisterGateway) {
        this.invoiceRepository = invoiceRepository;
        this.inventoryGateway = inventoryGateway;
        this.authorizationService = authorizationService;
        this.actorIdentityGateway = actorIdentityGateway;
        this.notificationService = notificationService;
        this.cashRegisterGateway = cashRegisterGateway;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, ConfirmInvoiceRequest request, UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        var actor = actorIdentityGateway.requireActor(authenticatedUserId);

        authorizationService.authorize(
                invoice, actor,
                request.confirmationPassword(),
                request.useBalance(),
                request.balanceAmount(),
                request.paymentMethodId(),
                authenticatedUserId
        );

        invoice.confirm();
        deductStockAllocations(invoice);

        Invoice saved = invoiceRepository.save(invoice);
        cashRegisterGateway.recordInvoice(new CashRegisterGateway.InvoiceEntry(saved.calculateTotal(), saved.getId().value(),
                saved.getInvoiceNumber().value(), authenticatedUserId, actor.name()));
        notificationService.notifyCustomerInvoiceConfirmed(
                saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value());
        return InvoiceMapper.toDetailResponse(saved);
    }

    private void deductStockAllocations(Invoice invoice) {
        inventoryGateway.confirmInvoice(InventoryGateway.InvoiceInventoryCommand.from(invoice));
    }
}
