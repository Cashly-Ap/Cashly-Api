package com.cashly.cashly_api.invoices.application.usecases;

import com.cashly.cashly_api.invoices.application.contracts.ActorIdentityGateway;
import com.cashly.cashly_api.invoices.application.contracts.CustomerCreditTermsGateway;
import com.cashly.cashly_api.invoices.application.contracts.InventoryGateway;
import com.cashly.cashly_api.invoices.application.contracts.InvoiceCatalogGateway;
import com.cashly.cashly_api.invoices.application.contracts.InvoiceCustomerGateway;
import com.cashly.cashly_api.invoices.application.contracts.InvoiceNotificationGateway;
import com.cashly.cashly_api.invoices.application.contracts.InvoiceOrderGateway;
import com.cashly.cashly_api.invoices.application.contracts.InvoiceQuoteGateway;
import com.cashly.cashly_api.invoices.application.dto.request.CreateInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLineAllocation;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CreateInvoiceUseCase {
    private final InvoiceRepository invoices;
    private final InvoiceOrderGateway orders;
    private final InvoiceCustomerGateway customers;
    private final CustomerCreditTermsGateway customerCreditTerms;
    private final ActorIdentityGateway actors;
    private final InvoiceCatalogGateway catalog;
    private final InventoryGateway inventory;
    private final InvoiceAuthorizationService authorization;
    private final InvoiceNotificationGateway notifications;
    private final InvoiceQuoteGateway quotes;

    public CreateInvoiceUseCase(InvoiceRepository invoices, InvoiceOrderGateway orders,
            InvoiceCustomerGateway customers, CustomerCreditTermsGateway customerCreditTerms,
            ActorIdentityGateway actors, InvoiceCatalogGateway catalog, InventoryGateway inventory,
            InvoiceAuthorizationService authorization, InvoiceNotificationGateway notifications,
            InvoiceQuoteGateway quotes) {
        this.invoices = invoices;
        this.orders = orders;
        this.customers = customers;
        this.customerCreditTerms = customerCreditTerms;
        this.actors = actors;
        this.catalog = catalog;
        this.inventory = inventory;
        this.authorization = authorization;
        this.notifications = notifications;
        this.quotes = quotes;
    }

    @Transactional
    public InvoiceDetailResponse execute(CreateInvoiceRequest request, UUID authenticatedUserId) {
        UUID customerId;
        String customerName;
        String customerRif;
        UUID createdBy;
        String createdByName;
        UUID primaryOrderId = null;
        String primaryOrderNumber = null;
        List<UUID> orderIds = request.orderIds() == null ? List.of() : request.orderIds();
        List<UUID> quoteIds = request.quoteIds() == null ? List.of() : request.quoteIds();
        List<InvoiceLine> lines;

        if (request.orderId() != null) {
            InvoiceOrderGateway.Order order = orders.findOrder(request.orderId());
            ActorIdentityGateway.Actor actor = actors.requireActor(authenticatedUserId);
            customerId = order.customerId();
            customerName = order.customerName();
            customerRif = order.customerRif();
            createdBy = authenticatedUserId;
            createdByName = actor.name();
            primaryOrderId = order.id();
            primaryOrderNumber = order.number();
            lines = order.lines().stream().map(line -> InvoiceLine.create(
                    line.productId(), line.productName(), line.productCode(), line.quantity(), line.unitPrice(),
                    line.allocations().stream().map(allocation -> InvoiceLineAllocation.create(
                            allocation.warehouseId(), allocation.quantity())).toList())).toList();
        } else {
            InvoiceCustomerGateway.Customer customer = customers.findCustomer(request.customerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));
            ActorIdentityGateway.Actor actor = actors.requireActor(authenticatedUserId);
            customerId = customer.id();
            customerName = customer.name();
            customerRif = customer.rif();
            createdBy = authenticatedUserId;
            createdByName = actor.name();
            lines = request.lines().stream().map(line -> {
                InvoiceCatalogGateway.Product product = catalog.findProduct(line.productId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.productId()));
                List<InvoiceLineAllocation> allocations = applyStockAllocations(line, product.id());
                return InvoiceLine.create(product.id(), product.name(), product.code(), line.quantity(),
                        line.unitPrice(), allocations);
            }).toList();
        }

        validateSources(orderIds, quoteIds, customerId);
        InvoiceCustomerGateway.Customer customer = customers.findCustomer(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        PaymentType paymentType = PaymentType.valueOf(request.paymentType());
        LocalDate issueDate = request.issueDate() != null ? request.issueDate() : LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(customerCreditTerms.creditDays(customerId));
        int year = LocalDate.now().getYear();
        InvoiceNumber invoiceNumber = InvoiceNumber.of(year, invoices.getNextSequenceForYear(year));

        Invoice invoice = Invoice.create(invoiceNumber, customerId, customerName, customerRif, createdBy,
                createdByName, primaryOrderId, primaryOrderNumber, orderIds, quoteIds, paymentType,
                request.notes(), lines, issueDate, dueDate);

        if (primaryOrderId != null) {
            ActorIdentityGateway.Actor actor = actors.requireActor(authenticatedUserId);
            authorization.authorize(invoice, actor, request.confirmationPassword(), request.useBalance(),
                    request.balanceAmount(), request.paymentMethodId(), authenticatedUserId);
            invoice.confirm();
        }

        Invoice saved = invoices.save(invoice);
        notifications.notifyCustomerInvoiceCreated(saved.getCustomerId(), saved.getId().value(),
                saved.getInvoiceNumber().value());
        if (primaryOrderId != null) {
            notifications.notifyCustomerInvoiceConfirmed(saved.getCustomerId(), saved.getId().value(),
                    saved.getInvoiceNumber().value());
        }
        return InvoiceMapper.toDetailResponse(saved);
    }

    private void validateSources(List<UUID> orderIds, List<UUID> quoteIds, UUID customerId) {
        for (UUID orderId : orderIds) {
            InvoiceOrderGateway.Order order = orders.findOrder(orderId);
            if (!customerId.equals(order.customerId())) {
                throw new IllegalArgumentException("Order does not belong to the invoice customer: " + orderId);
            }
            if (!order.status().equals("DRAFT") && !order.status().equals("CONFIRMED")) {
                throw new IllegalArgumentException("Only draft or confirmed orders can be linked to an invoice: " + orderId);
            }
        }
        for (UUID quoteId : quoteIds) {
            InvoiceQuoteGateway.Quote quote = quotes.findQuote(quoteId)
                    .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + quoteId));
            if (!customerId.equals(quote.customerId())) {
                throw new IllegalArgumentException("Quote does not belong to the invoice customer: " + quoteId);
            }
            if (!quote.status().equals("DRAFT")) {
                throw new IllegalArgumentException("Only draft quotes can be linked to an invoice: " + quoteId);
            }
        }
    }

    private List<InvoiceLineAllocation> applyStockAllocations(
            CreateInvoiceRequest.InvoiceLineRequest lineRequest, UUID productId) {
        inventory.reserveAllocations(lineRequest.warehouseAllocations().stream()
                .map(allocation -> new InventoryGateway.Allocation(allocation.warehouseId(), productId,
                        allocation.allocated())).toList());
        return lineRequest.warehouseAllocations().stream()
                .map(allocation -> InvoiceLineAllocation.create(allocation.warehouseId(),
                        BigDecimal.valueOf(allocation.allocated()))).toList();
    }
}
