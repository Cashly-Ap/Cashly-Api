package com.cashly.cashly_api.invoices.application.issue.services;

import com.cashly.cashly_api.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.invoices.application.issue.request.IssueInvoiceCommand;

import com.cashly.cashly_api.invoices.application.ports.out.*;
import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class IssueInvoiceService {
    private final InvoiceRepository invoices;
    private final ActorIdentityGateway actors;
    private final InvoiceIssueValidator validator;
    private final InvoicePaymentService payments;
    private final InventoryGateway reservations;
    private final StockDeductionGateway stock;
    private final InvoiceNotificationGateway notifications;
    private final CashRegisterGateway cashRegister;

    public IssueInvoiceService(InvoiceRepository invoices, ActorIdentityGateway actors,
            InvoiceIssueValidator validator, InvoicePaymentService payments, InventoryGateway reservations,
            StockDeductionGateway stock, InvoiceNotificationGateway notifications, CashRegisterGateway cashRegister) {
        this.invoices = invoices;
        this.actors = actors;
        this.validator = validator;
        this.payments = payments;
        this.reservations = reservations;
        this.stock = stock;
        this.notifications = notifications;
        this.cashRegister = cashRegister;
    }

    @Transactional
    public Invoice issue(Invoice invoice, IssueInvoiceCommand command, InvoiceOrigin origin, UUID actorId) {
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be confirmed");
        }
        var actor = actors.requireActor(actorId);
        var plan = validator.validate(invoice, actor, command.confirmationPassword(), command.useBalance(),
                command.balanceAmount(), command.paymentMethodId(), actorId);
        if (invoice.getPaymentType() == PaymentType.CASH && origin != InvoiceOrigin.MANUAL_DRAFT) {
            invoices.save(invoice);
        }
        payments.apply(invoice, plan, command, actor);
        invoice.confirm();
        switch (origin) {
            case QUOTE -> stock.deduct(invoice.getLines().stream().flatMap(line -> line.getAllocations().stream()
                    .map(allocation -> new StockDeductionGateway.Allocation(allocation.getWarehouseId(),
                            line.getProductId(), allocation.getAllocated().intValue()))).toList());
            case MANUAL_DRAFT -> reservations.confirmInvoice(InventoryGateway.InvoiceInventoryCommand.from(invoice));
            case CONFIRMED_ORDER -> {}
        }
        Invoice saved = invoices.save(invoice);
        if (origin == InvoiceOrigin.MANUAL_DRAFT) {
            cashRegister.recordInvoice(new CashRegisterGateway.InvoiceEntry(saved.calculateTotal(), saved.getId().value(),
                    saved.getInvoiceNumber().value(), actorId, actor.name()));
        }
        if (origin == InvoiceOrigin.CONFIRMED_ORDER) {
            notifications.notifyCustomerInvoiceCreated(saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value());
        }
        if (origin != InvoiceOrigin.QUOTE) {
            notifications.notifyCustomerInvoiceConfirmed(saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value());
        }
        return saved;
    }
}
