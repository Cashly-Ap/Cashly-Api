package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.invoices.application.dto.request.CreateInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.application.services.InvoiceAuthorizationService;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final InvoiceAuthorizationService authorizationService;

    public CreateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                 OrderRepository orderRepository,
                                 CustomerRepository customerRepository,
                                 UserRepository userRepository,
                                 ProductRepository productRepository,
                                 WarehouseStockRepository warehouseStockRepository,
                                 InvoiceAuthorizationService authorizationService) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.authorizationService = authorizationService;
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

        PaymentType paymentType = PaymentType.valueOf(request.paymentType());

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
                paymentType,
                request.notes(),
                lines
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

        return InvoiceMapper.toDetailResponse(saved);
    }

    private List<InvoiceLineAllocation> applyStockAllocations(CreateInvoiceRequest.InvoiceLineRequest lineRequest, UUID productId) {
        List<InvoiceLineAllocation> allocations = new ArrayList<>();
        for (CreateInvoiceRequest.WarehouseAllocationRequest allocation : lineRequest.warehouseAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.warehouseId(), productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock not found for warehouse=" + allocation.warehouseId() + " product=" + productId));

            stock.reserve(allocation.allocated());
            warehouseStockRepository.save(stock);

            allocations.add(InvoiceLineAllocation.create(allocation.warehouseId(), BigDecimal.valueOf(allocation.allocated())));
        }
        return allocations;
    }
}
