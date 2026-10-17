package com.cashly.cashly_api.orders.application.create.use_case;

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
import com.cashly.cashly_api.orders.application.create.request.CreateOrderRequest;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.ports.out.OrderActorGateway;
import com.cashly.cashly_api.orders.application.ports.out.OrderCustomerGateway;
import com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
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
@DisplayName("CreateOrderUseCase Unit Tests")
class CreateOrderUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderCustomerGateway customerRepository;

    @Mock
    private OrderProductCatalogGateway productRepository;

    @Mock
    private OrderActorGateway userRepository;

    @Mock
    private OrderInventoryGateway warehouseStockRepository;

    private CreateOrderUseCase createOrderUseCase;

    @BeforeEach
    void setUp() {
        createOrderUseCase = new CreateOrderUseCase(
                orderRepository, customerRepository, productRepository,
                userRepository, warehouseStockRepository);
    }

    @Test
    void should_CreateOrder_When_AllDataIsValid() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);
        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 0,
                LocalDateTime.now(), LocalDateTime.now());

        CreateOrderRequest request = new CreateOrderRequest(
                customerId, "Some notes",
                List.of(new CreateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateOrderRequest.WarehouseAllocationRequest(warehouseId, 5))
                ))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerRef(customerId, customer)));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(actorRef(authenticatedUserId, user)));
        when(productRepository.findProduct(productId)).thenReturn(Optional.of(productRef(product)));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(orderRepository.save(any(com.cashly.cashly_api.orders.domain.entity.Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderDetailResponse response = createOrderUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        assertEquals("DRAFT", response.status());
        assertNotNull(response.id());
        assertNotNull(response.orderNumber());
        verify(orderRepository).save(any(com.cashly.cashly_api.orders.domain.entity.Order.class));
    }

    @Test
    void should_ReserveStock_When_CreatingOrder() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);
        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 2,
                LocalDateTime.now(), LocalDateTime.now());

        CreateOrderRequest request = new CreateOrderRequest(
                customerId, null,
                List.of(new CreateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateOrderRequest.WarehouseAllocationRequest(warehouseId, 5))
                ))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerRef(customerId, customer)));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(actorRef(authenticatedUserId, user)));
        when(productRepository.findProduct(productId)).thenReturn(Optional.of(productRef(product)));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(orderRepository.save(any(com.cashly.cashly_api.orders.domain.entity.Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        createOrderUseCase.execute(request, authenticatedUserId);

        verify(warehouseStockRepository).reserve(List.of(new OrderInventoryGateway.Allocation(warehouseId,
                productId, 5)));
    }

    @Test
    void should_ThrowIllegalArgumentException_When_CustomerNotFound() {
        UUID customerId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                customerId, null,
                List.of(new CreateOrderRequest.OrderLineRequest(
                        UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN, List.of()
                ))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> createOrderUseCase.execute(request, UUID.randomUUID())
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_UserNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);

        CreateOrderRequest request = new CreateOrderRequest(
                customerId, null,
                List.of(new CreateOrderRequest.OrderLineRequest(
                        UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN, List.of()
                ))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerRef(customerId, customer)));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> createOrderUseCase.execute(request, authenticatedUserId)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_ProductNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);

        CreateOrderRequest request = new CreateOrderRequest(
                customerId, null,
                List.of(new CreateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN, List.of()
                ))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerRef(customerId, customer)));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(actorRef(authenticatedUserId, user)));
        when(productRepository.findProduct(productId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> createOrderUseCase.execute(request, authenticatedUserId)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_StockNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreateOrderRequest request = new CreateOrderRequest(
                customerId, null,
                List.of(new CreateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateOrderRequest.WarehouseAllocationRequest(warehouseId, 5))
                ))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerRef(customerId, customer)));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(actorRef(authenticatedUserId, user)));
        when(productRepository.findProduct(productId)).thenReturn(Optional.of(productRef(product)));
        doThrow(new IllegalArgumentException("Stock not found")).when(warehouseStockRepository).reserve(anyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> createOrderUseCase.execute(request, authenticatedUserId)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_SaveExactlyOnce_When_CreationSucceeds() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);
        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 0,
                LocalDateTime.now(), LocalDateTime.now());

        CreateOrderRequest request = new CreateOrderRequest(
                customerId, null,
                List.of(new CreateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateOrderRequest.WarehouseAllocationRequest(warehouseId, 5))
                ))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerRef(customerId, customer)));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(actorRef(authenticatedUserId, user)));
        when(productRepository.findProduct(productId)).thenReturn(Optional.of(productRef(product)));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(orderRepository.save(any(com.cashly.cashly_api.orders.domain.entity.Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        createOrderUseCase.execute(request, authenticatedUserId);

        verify(orderRepository, times(1)).save(any(com.cashly.cashly_api.orders.domain.entity.Order.class));
    }

    private Customer buildCustomer(UUID customerId) {
        return Customer.reconstitute(
                new CustomerId(customerId),
                new CustomerCode(1001L),
                new LegalName("Empresa Test C.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Test"),
                new CreditDays(30),
                new CreditLimit(BigDecimal.ZERO),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(), List.of(), List.of(),
                null, null
        );
    }

    private User buildUser(UUID userId) {
        return new User(
                new UserId(userId),
                new Email("user@cashly.com"),
                new UserName("Test User"),
                Password.fromHash("hashed-password")
        );
    }

    private Product buildProduct(UUID productId) {
        return Product.reconstitute(
                productId, "PROD-001", "Product A",
                null, null, false, true, null,
                BigDecimal.valueOf(8),
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private OrderCustomerGateway.Customer customerRef(UUID id, Customer customer) {
        return new OrderCustomerGateway.Customer(id, customer.getLegalName().getValue(), customer.getRif().getValue());
    }

    private OrderActorGateway.Actor actorRef(UUID id, User user) {
        return new OrderActorGateway.Actor(id, user.getUserName().getValue());
    }

    private OrderProductCatalogGateway.ProductReference productRef(Product product) {
        return new OrderProductCatalogGateway.ProductReference(product.getId(), product.getCode(), product.getName(),
                product.getCategoryId());
    }
}
