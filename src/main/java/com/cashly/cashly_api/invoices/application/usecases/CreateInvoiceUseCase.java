package com.cashly.cashly_api.invoices.application.usecases;

import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.invoices.application.dto.request.CreateInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLineAllocation;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteStatus;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CreateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final InvoiceAuthorizationService authorizationService;
    private final NotificationService notificationService;
    private final QuoteRepository quoteRepository;

    @Autowired
    public CreateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                 OrderRepository orderRepository,
                                 CustomerRepository customerRepository,
                                 UserRepository userRepository,
                                 ProductRepository productRepository,
                                 WarehouseStockRepository warehouseStockRepository,
                                 InvoiceAuthorizationService authorizationService,
                                 NotificationService notificationService,
                                 QuoteRepository quoteRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
        this.quoteRepository = quoteRepository;
    }

    public CreateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                OrderRepository orderRepository,
                                CustomerRepository customerRepository,
                                UserRepository userRepository,
                                ProductRepository productRepository,
                                WarehouseStockRepository warehouseStockRepository,
                                InvoiceAuthorizationService authorizationService,
                                NotificationService notificationService) {
        this(invoiceRepository, orderRepository, customerRepository, userRepository, productRepository,
                warehouseStockRepository, authorizationService, notificationService, null);
    }

    @Transactional
    public InvoiceDetailResponse execute(CreateInvoiceRequest request, UUID authenticatedUserId) {
        UUID resolvedCustomerId;
        String customerName;
        String customerRif;
        UUID resolvedCreatedBy;
        String createdByName;
        UUID orderId = null;
        String orderNumber = null;
        List<UUID> orderIds = request.orderIds() == null ? List.of() : request.orderIds();
        List<UUID> quoteIds = request.quoteIds() == null ? List.of() : request.quoteIds();
        List<InvoiceLine> lines;

        if (request.orderId() != null) {
            Order order = orderRepository.findById(OrderId.of(request.orderId()))
                    .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

            resolvedCustomerId = order.getCustomerId();
            customerName = order.getCustomerName();
            customerRif = order.getCustomerRif();
            resolvedCreatedBy = authenticatedUserId;
            User user = userRepository.findById(new UserId(authenticatedUserId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));
            createdByName = user.getUserName().getValue();
            orderId = order.getId().value();
            orderNumber = order.getOrderNumber().value();

            lines = order.getLines().stream()
                    .map(ol -> InvoiceLine.create(
                            ol.getProductId(),
                            ol.getProductName(),
                            ol.getProductCode(),
                            ol.getQuantity(),
                            ol.getUnitPrice(),
                            ol.getAllocations().stream()
                                    .map(a -> InvoiceLineAllocation.create(
                                            a.getWarehouseId(),
                                            a.getAllocated()
                                    ))
                                    .toList()
                    ))
                    .toList();
        } else {
            Customer customer = customerRepository.findById(new CustomerId(request.customerId()))
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

            User user = userRepository.findById(new UserId(authenticatedUserId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

            resolvedCustomerId = customer.getId().getValue();
            customerName = customer.getLegalName().getValue();
            customerRif = customer.getRif().getValue();
            resolvedCreatedBy = user.getId().getValue();
            createdByName = user.getUserName().getValue();

            lines = request.lines().stream()
                    .map(lineRequest -> {
                        Product product = productRepository.findById(lineRequest.productId())
                                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                        List<InvoiceLineAllocation> allocations = applyStockAllocations(lineRequest, product.getId());

                        return InvoiceLine.create(
                                product.getId(),
                                product.getName(),
                                product.getCode(),
                                lineRequest.quantity(),
                                lineRequest.unitPrice(),
                                allocations
                        );
                    })
                    .toList();
        }

        validateSources(orderIds, quoteIds, resolvedCustomerId);

        Customer customer = customerRepository.findById(new CustomerId(resolvedCustomerId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + resolvedCustomerId));

        PaymentType paymentType = PaymentType.valueOf(request.paymentType());
        LocalDate issueDate = request.issueDate() != null ? request.issueDate() : LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(customer.getCreditDays().getValue());

        int year = LocalDate.now().getYear();
        int sequence = invoiceRepository.getNextSequenceForYear(year);
        InvoiceNumber invoiceNumber = InvoiceNumber.of(year, sequence);

        Invoice invoice = Invoice.create(
                invoiceNumber,
                resolvedCustomerId,
                customerName,
                customerRif,
                resolvedCreatedBy,
                createdByName,
                orderId,
                orderNumber,
                orderIds,
                quoteIds,
                paymentType,
                request.notes(),
                lines,
                issueDate,
                dueDate
        );

        if (orderId != null) {
            User user = userRepository.findById(new UserId(authenticatedUserId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            authorizationService.authorize(invoice, user,
                    request.confirmationPassword(), request.useBalance(),
                    request.balanceAmount(), request.paymentMethodId(),
                    authenticatedUserId);
            invoice.confirm();
        }

        Invoice saved = invoiceRepository.save(invoice);
        notificationService.notifyCustomerInvoiceCreated(
                saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value());
        if (orderId != null) {
            notificationService.notifyCustomerInvoiceConfirmed(
                    saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value());
        }

        return InvoiceMapper.toDetailResponse(saved);
    }

    private void validateSources(List<UUID> orderIds, List<UUID> quoteIds, UUID customerId) {
        for (UUID sourceId : orderIds) {
            Order order = orderRepository.findById(OrderId.of(sourceId))
                    .orElseThrow(() -> new OrderNotFoundException(sourceId));
            if (!customerId.equals(order.getCustomerId())) {
                throw new IllegalArgumentException("Order does not belong to the invoice customer: " + sourceId);
            }
            if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.CONFIRMED) {
                throw new IllegalArgumentException("Only draft or confirmed orders can be linked to an invoice: " + sourceId);
            }
        }
        for (UUID sourceId : quoteIds) {
            if (quoteRepository == null) {
                throw new IllegalStateException("Quote repository is not configured");
            }
            Quote quote = quoteRepository.findById(sourceId)
                    .orElseThrow(() -> new IllegalArgumentException("Quote not found: " + sourceId));
            if (!customerId.equals(quote.getCustomerId())) {
                throw new IllegalArgumentException("Quote does not belong to the invoice customer: " + sourceId);
            }
            if (quote.getStatus() != QuoteStatus.DRAFT && quote.getStatus() != QuoteStatus.CUSTOMER_DRAFT) {
                throw new IllegalArgumentException("Only draft quotes can be linked to an invoice: " + sourceId);
            }
        }
    }

    private List<InvoiceLineAllocation> applyStockAllocations(CreateInvoiceRequest.InvoiceLineRequest lineRequest, UUID productId) {
        List<InvoiceLineAllocation> allocations = new ArrayList<>();
        for (CreateInvoiceRequest.WarehouseAllocationRequest allocation : lineRequest.warehouseAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.warehouseId(), productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No stock available for warehouse=" + allocation.warehouseId()
                                    + " product=" + productId));

            stock.reserve(allocation.allocated());
            warehouseStockRepository.save(stock);

            allocations.add(InvoiceLineAllocation.create(allocation.warehouseId(), BigDecimal.valueOf(allocation.allocated())));
        }
        return allocations;
    }
}
