package com.cashly.cashly_api.invoices.application.create.from_quote;

import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;

import com.cashly.cashly_api.invoices.application.ports.in.QuoteInvoiceCreationApi;
import com.cashly.cashly_api.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.ports.out.CustomerCreditTermsGateway;
import com.cashly.cashly_api.invoices.application.ports.out.StockDeductionGateway;
import com.cashly.cashly_api.invoices.domain.Invoice;
import com.cashly.cashly_api.invoices.domain.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.InvoiceLineAllocation;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class QuoteInvoiceCreationService implements QuoteInvoiceCreationApi {
    private final InvoiceRepository invoices;
    private final ActorIdentityGateway actors;
    private final CustomerCreditTermsGateway creditTerms;
    private final InvoiceAuthorizationService authorization;
    private final StockDeductionGateway inventory;

    public QuoteInvoiceCreationService(InvoiceRepository invoices, ActorIdentityGateway actors,
                                      CustomerCreditTermsGateway creditTerms, InvoiceAuthorizationService authorization,
                                      StockDeductionGateway inventory) {
        this.invoices = invoices;
        this.actors = actors;
        this.creditTerms = creditTerms;
        this.authorization = authorization;
        this.inventory = inventory;
    }

    @Override
    @Transactional
    public CreatedInvoice create(Command command, UUID actorId) {
        PaymentType paymentType;
        try {
            paymentType = PaymentType.valueOf(command.paymentType().toUpperCase());
        } catch (IllegalArgumentException failure) {
            throw new IllegalArgumentException("Invalid payment type: " + command.paymentType() + ". Must be CASH or CREDIT");
        }
        var actor = actors.findActor(actorId).orElseThrow(() -> new IllegalArgumentException("User not found: " + actorId));
        LocalDate issueDate = command.issueDate() == null ? LocalDate.now() : command.issueDate();
        LocalDate dueDate = issueDate.plusDays(creditTerms.creditDays(command.customerId()));
        int year = LocalDate.now().getYear();
        var number = InvoiceNumber.of(year, invoices.getNextSequenceForYear(year));
        var lines = command.lines().stream().map(line -> InvoiceLine.create(line.productId(), line.productName(),
                line.productCode(), line.quantity(), line.unitPrice(), line.allocations().stream()
                        .map(item -> InvoiceLineAllocation.create(item.warehouseId(), item.quantity())).toList())).toList();
        var invoice = Invoice.reconstitute(InvoiceId.generate(), number, command.customerId(), command.customerName(),
                command.customerRif(), actorId, actor.name(), null, null, List.of(), List.of(), InvoiceStatus.DRAFT,
                paymentType, null, BigDecimal.ZERO, null, null, null, lines, LocalDateTime.now(), LocalDateTime.now(),
                issueDate, dueDate);
        authorization.authorize(invoice, actor, command.confirmationPassword(), command.useBalance(),
                command.balanceAmount(), command.paymentMethodId(), actorId);
        invoice.confirm();
        inventory.deduct(command.lines().stream().flatMap(line -> line.allocations().stream()
                .map(item -> new StockDeductionGateway.Allocation(item.warehouseId(), item.productId(),
                        item.quantity().intValue()))).toList());
        var saved = invoices.save(invoice);
        return new CreatedInvoice(saved.getId().value(), saved.getInvoiceNumber().value());
    }
}
