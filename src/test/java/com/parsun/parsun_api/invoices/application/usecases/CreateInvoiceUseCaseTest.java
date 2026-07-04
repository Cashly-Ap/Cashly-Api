package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import com.parsun.parsun_api.invoices.application.dto.request.CreateInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.invoices.application.services.InvoiceAuthorizationService;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private InvoiceAuthorizationService authorizationService;

    private CreateInvoiceUseCase createInvoiceUseCase;

    @BeforeEach
    void setUp() {
        createInvoiceUseCase = new CreateInvoiceUseCase(
                invoiceRepository, orderRepository, customerRepository,
                userRepository, productRepository, warehouseStockRepository, authorizationService);
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
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = createInvoiceUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
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
                com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException.class,
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
                List.of(), List.of(), List.of(), false, null, null, null
        );
    }

    private User buildUser(UUID userId) {
        return new User(
                new UserId(userId), new Email("user@parsun.com"),
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
