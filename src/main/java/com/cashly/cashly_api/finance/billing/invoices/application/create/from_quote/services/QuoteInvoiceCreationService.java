package com.cashly.cashly_api.finance.billing.invoices.application.create.from_quote.services;

import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.QuoteInvoiceCreationApi;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditTermsGateway;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
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
    private final IssueInvoiceService issuance;

    public QuoteInvoiceCreationService(InvoiceRepository invoices, ActorIdentityGateway actors,
                                      CustomerCreditTermsGateway creditTerms, IssueInvoiceService issuance) {
        this.invoices = invoices;
        this.actors = actors;
        this.creditTerms = creditTerms;
        this.issuance = issuance;
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
        var saved = issuance.issue(invoice, new IssueInvoiceCommand(command.confirmationPassword(), command.useBalance(),
                command.balanceAmount(), command.paymentMethodId()), InvoiceOrigin.QUOTE, actorId);
        return new CreatedInvoice(saved.getId().value(), saved.getInvoiceNumber().value());
    }
}
