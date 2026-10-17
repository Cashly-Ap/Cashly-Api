package com.cashly.cashly_api.finance.billing.invoices.application.create.use_case;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditTermsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceCatalogGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceCustomerGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNotificationGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceOrderGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceQuoteGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.create.request.CreateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
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
    private final IssueInvoiceService issuance;
    private final InvoiceNotificationGateway notifications;
    private final InvoiceQuoteGateway quotes;

    public CreateInvoiceUseCase(InvoiceRepository invoices, InvoiceOrderGateway orders,
            InvoiceCustomerGateway customers, CustomerCreditTermsGateway customerCreditTerms,
            ActorIdentityGateway actors, InvoiceCatalogGateway catalog, InventoryGateway inventory,
            IssueInvoiceService issuance, InvoiceNotificationGateway notifications,
            InvoiceQuoteGateway quotes) {
        this.invoices = invoices;
        this.orders = orders;
        this.customers = customers;
        this.customerCreditTerms = customerCreditTerms;
        this.actors = actors;
        this.catalog = catalog;
        this.inventory = inventory;
        this.issuance = issuance;
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
            if (!"CONFIRMED".equals(order.status())) {
                throw new IllegalStateException("Only CONFIRMED orders can be invoiced");
            }
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
        customers.findCustomer(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        PaymentType paymentType = PaymentType.valueOf(request.paymentType());
        LocalDate issueDate = request.issueDate() != null ? request.issueDate() : LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(customerCreditTerms.creditDays(customerId));
        int year = LocalDate.now().getYear();
        InvoiceNumber invoiceNumber = InvoiceNumber.of(year, invoices.getNextSequenceForYear(year));

        Invoice invoice = Invoice.create(invoiceNumber, customerId, customerName, customerRif, createdBy,
                createdByName, primaryOrderId, primaryOrderNumber, orderIds, quoteIds, paymentType,
                request.notes(), lines, issueDate, dueDate);

        Invoice saved;
        if (primaryOrderId != null) {
            saved = issuance.issue(invoice, new IssueInvoiceCommand(request.confirmationPassword(), request.useBalance(),
                    request.balanceAmount(), request.paymentMethodId()), InvoiceOrigin.CONFIRMED_ORDER, authenticatedUserId);
        } else {
            saved = invoices.save(invoice);
            notifications.notifyCustomerInvoiceCreated(saved.getCustomerId(), saved.getId().value(),
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
