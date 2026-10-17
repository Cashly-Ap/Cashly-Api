package com.cashly.cashly_api.finance.billing.invoices.application.create.use_case;

import com.cashly.cashly_api.auth.user.domain.entity.User;
import com.cashly.cashly_api.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CreditLimit;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerAlias;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerCode;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerType;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.LegalName;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.Rif;
import com.cashly.cashly_api.finance.billing.invoices.application.create.request.CreateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.InvoicePaymentService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditTermsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceCatalogGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceCustomerGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNotificationGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceOrderGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceQuoteGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateInvoiceUseCase Unit Tests")
class CreateInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository quoteRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private InvoiceIssueValidator authorizationService;

    @Mock
    private InvoiceNotificationGateway notificationService;

    private CreateInvoiceUseCase createInvoiceUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(authorizationService.validate(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new InvoiceIssueValidator.CashPaymentPlan(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));
        notificationService = mock(InvoiceNotificationGateway.class);
        InvoiceCustomerGateway customers = customerId -> customerRepository
                .findById(new CustomerId(customerId))
                .map(customer -> new InvoiceCustomerGateway.Customer(customer.getId().getValue(),
                        customer.getLegalName().getValue(), customer.getRif().getValue()));
        CustomerCreditTermsGateway creditTerms = customerId -> customerRepository
                .findById(new CustomerId(customerId)).orElseThrow().getCreditDays().getValue();
        ActorIdentityGateway actors = new ActorIdentityGateway() {
            @Override
            public Actor requireActor(UUID id) {
                return findActor(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
            }

            @Override
            public Optional<Actor> findActor(UUID id) {
                return userRepository.findById(new UserId(id))
                        .map(user -> new Actor(user.getId().getValue(), user.getUserName().getValue()));
            }

            @Override
            public boolean matchesPassword(UUID id, String password) {
                return false;
            }
        };
        InvoiceCatalogGateway catalog = productId -> productRepository.findById(productId)
                .map(product -> new InvoiceCatalogGateway.Product(product.getId(), product.getName(), product.getCode()));
        InvoiceOrderGateway orderGateway = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoiceOrderGateway(orderRepository);
        InvoiceQuoteGateway quoteGateway = quoteId -> quoteRepository.findById(quoteId)
                .map(quote -> new InvoiceQuoteGateway.Quote(quote.getId(), quote.getCustomerId(), quote.getStatus().name()));
        createInvoiceUseCase = new CreateInvoiceUseCase(
                invoiceRepository, orderGateway, customers, creditTerms, actors, catalog,
                new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInventoryGatewayAdapter(new com.cashly.cashly_api.inventory.warehouses.stock.application.invoices.services.InvoiceInventoryService(warehouseStockRepository, mock(com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository.class))), new IssueInvoiceService(invoiceRepository, actors, authorizationService,
                    mock(InvoicePaymentService.class), mock(com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway.class),
                    mock(com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockDeductionGateway.class), notificationService,
                    mock(com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CashRegisterGateway.class)),
                notificationService, quoteGateway);
    }

    @Test
    void should_CreateInvoice_When_OrderIdProvided() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Order order = buildOrder(orderId, customerId);
        User user = buildUser(authenticatedUserId);

        CreateInvoiceRequest request = new CreateInvoiceRequest(
                orderId, null, null, "CASH", null,
                null, null, null, null, List.of()
        );

        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(buildCustomer(customerId)));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = createInvoiceUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
        verify(notificationService).notifyCustomerInvoiceCreated(any(), any(), any());
        verify(notificationService).notifyCustomerInvoiceConfirmed(any(), any(), any());
    }

    @Test
    void should_CreateInvoice_When_ManualLinesProvided() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreateInvoiceRequest.InvoiceLineRequest lineRequest = new CreateInvoiceRequest.InvoiceLineRequest(
                productId, BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                null, customerId, null, "CASH", "Notes",
                null, null, null, null, List.of(lineRequest)
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = createInvoiceUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        verify(invoiceRepository).save(any(Invoice.class));
        verify(notificationService).notifyCustomerInvoiceCreated(any(), any(), any());
    }

    @Test
    void should_ThrowOrderNotFoundException_When_OrderNotFound() {
        UUID orderId = UUID.randomUUID();

        CreateInvoiceRequest request = new CreateInvoiceRequest(
                orderId, null, null, "CASH", null,
                null, null, null, null, List.of()
        );

        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.empty());

        assertThrows(
                com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException.class,
                () -> createInvoiceUseCase.execute(request, UUID.randomUUID())
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_CustomerNotFound() {
        UUID customerId = UUID.randomUUID();

        CreateInvoiceRequest.InvoiceLineRequest lineRequest = new CreateInvoiceRequest.InvoiceLineRequest(
                UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                null, customerId, null, "CASH", null,
                null, null, null, null, List.of(lineRequest)
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> createInvoiceUseCase.execute(request, UUID.randomUUID())
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_UserNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);

        CreateInvoiceRequest.InvoiceLineRequest lineRequest = new CreateInvoiceRequest.InvoiceLineRequest(
                UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                null, customerId, null, "CASH", null,
                null, null, null, null, List.of(lineRequest)
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> createInvoiceUseCase.execute(request, authenticatedUserId)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_ProductNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);

        CreateInvoiceRequest.InvoiceLineRequest lineRequest = new CreateInvoiceRequest.InvoiceLineRequest(
                productId, BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                null, customerId, null, "CASH", null,
                null, null, null, null, List.of(lineRequest)
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> createInvoiceUseCase.execute(request, authenticatedUserId)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_RejectInvoice_When_AllocatedQuantityExceedsAvailableStock() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);
        WarehouseStock stock = new WarehouseStock(
                UUID.randomUUID(), warehouseId, productId, 2, 0,
                LocalDateTime.now(), LocalDateTime.now());
        CreateInvoiceRequest.InvoiceLineRequest lineRequest = new CreateInvoiceRequest.InvoiceLineRequest(
                productId, BigDecimal.valueOf(3), BigDecimal.TEN,
                List.of(new CreateInvoiceRequest.WarehouseAllocationRequest(warehouseId, 3))
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                null, customerId, null, "CASH", null,
                null, null, null, null, List.of(lineRequest)
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> createInvoiceUseCase.execute(request, authenticatedUserId)
        );

        assertTrue(exception.getMessage().contains("Insufficient available stock"));
        verify(invoiceRepository, never()).save(any());
    }

    private Order buildOrder(UUID orderId, UUID customerId) {
        OrderLine line = OrderLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Order.reconstitute(
                OrderId.of(orderId), OrderNumber.of(2026, 1),
                customerId, "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name",
                OrderStatus.CONFIRMED, null, null, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Customer buildCustomer(UUID customerId) {
        return Customer.reconstitute(
                new CustomerId(customerId), new CustomerCode(1001L),
                new LegalName("Empresa Test C.A."), CustomerType.BUSINESS,
                new Rif("J-12345678-9"), new CustomerAlias("Test"),
                new CreditDays(30), new CreditLimit(BigDecimal.ZERO),
                true, LocalDateTime.now(), LocalDateTime.now(),
                List.of(), List.of(), List.of(), null, null
        );
    }

    private User buildUser(UUID userId) {
        return new User(
                new UserId(userId), new Email("user@cashly.com"),
                new UserName("Test User"), Password.fromHash("hashed-password")
        );
    }

    private Product buildProduct(UUID productId) {
        return Product.reconstitute(
                productId, "PROD-001", "Product A",
                null, null, false, true, null,
                BigDecimal.valueOf(8),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
