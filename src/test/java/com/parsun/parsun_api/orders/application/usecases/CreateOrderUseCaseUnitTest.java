package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import com.parsun.parsun_api.orders.application.dto.request.CreateOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
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
@DisplayName("CreateOrderUseCase Unit Tests")
class CreateOrderUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

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

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(orderRepository.save(any(com.parsun.parsun_api.orders.domain.entities.Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderDetailResponse response = createOrderUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        assertEquals("DRAFT", response.status());
        assertNotNull(response.id());
        assertNotNull(response.orderNumber());
        verify(orderRepository).save(any(com.parsun.parsun_api.orders.domain.entities.Order.class));
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

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(orderRepository.save(any(com.parsun.parsun_api.orders.domain.entities.Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        createOrderUseCase.execute(request, authenticatedUserId);

        assertEquals(7, stock.getReserved());  
        assertEquals(50, stock.getQuantity());  
        verify(warehouseStockRepository).save(stock);
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

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

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

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.empty());

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

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

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

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.empty());

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

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(orderRepository.save(any(com.parsun.parsun_api.orders.domain.entities.Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        createOrderUseCase.execute(request, authenticatedUserId);

        verify(orderRepository, times(1)).save(any(com.parsun.parsun_api.orders.domain.entities.Order.class));
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
                false, null, null, null
        );
    }

    private User buildUser(UUID userId) {
        return new User(
                new UserId(userId),
                new Email("user@parsun.com"),
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
}
