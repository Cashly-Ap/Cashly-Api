package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.quotes.application.dto.request.CreateQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateQuoteUseCase Unit Tests")
class CreateQuoteUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private CreateQuoteUseCase createQuoteUseCase;

    @BeforeEach
    void setUp() {
        createQuoteUseCase = new CreateQuoteUseCase(
                quoteRepository, customerRepository, userRepository, productRepository, warehouseRepository
        );
    }

    @Test
    @DisplayName("should create quote successfully when all dependencies exist")
    void shouldCreateQuoteSuccessfully() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreateQuoteRequest.QuoteLineRequest lineRequest =
                new CreateQuoteRequest.QuoteLineRequest(
                        productId, BigDecimal.ONE, new BigDecimal("100.0000"),
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(warehouseId, BigDecimal.ONE))
                );

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, LocalDate.of(2026, 6, 30), "Test notes", List.of(lineRequest)
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(buildWarehouse(warehouseId)));
        when(quoteRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteDetailResponse response = createQuoteUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        assertEquals("2026-0001", response.quoteNumber());
        assertEquals(customerId, response.customerId());
        assertEquals("Empresa Test C.A.", response.customerName());
        assertEquals("J-12345678-9", response.customerRif());
        assertEquals(authenticatedUserId, response.createdBy());
        assertEquals("Test User", response.createdByName());
        assertEquals("DRAFT", response.status());
        assertEquals(LocalDate.of(2026, 6, 30), response.expiryDate());
        assertEquals("Test notes", response.notes());
        assertNotNull(response.subtotal());
        assertNull(response.orderId());
        assertNull(response.invoiceId());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        assertEquals(1, response.lines().size());
        verify(quoteRepository).save(any(Quote.class));
    }

    @Test
    @DisplayName("should throw when customer is not found")
    void shouldThrowWhenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, null, null,
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.ONE))
                ))
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createQuoteUseCase.execute(request, authenticatedUserId)
        );

        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(quoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when user is not found")
    void shouldThrowWhenUserNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, null, null,
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.ONE))
                ))
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createQuoteUseCase.execute(request, authenticatedUserId)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        verify(quoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when product is not found")
    void shouldThrowWhenProductNotFound() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, null, null,
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.ONE))
                ))
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createQuoteUseCase.execute(request, authenticatedUserId)
        );

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(quoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("should generate correct quote number from sequence")
    void shouldGenerateCorrectQuoteNumber() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, null, null,
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.ONE))
                ))
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(any())).thenReturn(Optional.of(buildWarehouse(UUID.randomUUID())));
        when(quoteRepository.getNextSequenceForYear(anyInt())).thenReturn(42);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteDetailResponse response = createQuoteUseCase.execute(request, authenticatedUserId);

        assertEquals("2026-0042", response.quoteNumber());
        verify(quoteRepository).getNextSequenceForYear(LocalDate.now().getYear());
    }

    @Test
    @DisplayName("should snapshot product data on each line")
    void shouldSnapshotProductDataOnEachLine() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, null, null,
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        productId, BigDecimal.valueOf(2), new BigDecimal("150.0000"),
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.valueOf(2)))
                ))
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(any())).thenReturn(Optional.of(buildWarehouse(UUID.randomUUID())));
        when(quoteRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteDetailResponse response = createQuoteUseCase.execute(request, authenticatedUserId);

        assertEquals(1, response.lines().size());
        var lineResponse = response.lines().get(0);
        assertEquals(productId, lineResponse.productId());
        assertEquals("Product A", lineResponse.productName());
        assertEquals("PROD-001", lineResponse.productCode());
        assertEquals(BigDecimal.valueOf(2), lineResponse.quantity());
        assertEquals(new BigDecimal("150.0000"), lineResponse.unitPrice());
        assertEquals(new BigDecimal("300.0000"), lineResponse.lineTotal());
    }

    @Test
    @DisplayName("should build warehouse allocations on lines")
    void shouldBuildWarehouseAllocationsOnLines() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, null, null,
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        productId, BigDecimal.valueOf(3), new BigDecimal("100.0000"),
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(warehouseId, BigDecimal.valueOf(3)))
                ))
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(buildWarehouse(warehouseId)));
        when(quoteRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteDetailResponse response = createQuoteUseCase.execute(request, authenticatedUserId);

        assertEquals(1, response.lines().size());
        var lineResponse = response.lines().get(0);
        assertEquals(1, lineResponse.allocations().size());
        var allocationResponse = lineResponse.allocations().get(0);
        assertEquals(warehouseId, allocationResponse.warehouseId());
        assertEquals(BigDecimal.valueOf(3), allocationResponse.allocatedQuantity());
    }

    @Test
    @DisplayName("should calculate subtotal from all lines")
    void shouldCalculateSubtotalFromAllLines() {
        UUID customerId = UUID.randomUUID();
        UUID product1Id = UUID.randomUUID();
        UUID product2Id = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId);
        User user = buildUser(authenticatedUserId);
        Product product1 = Product.reconstitute(product1Id, "PROD-001", "Product 1", null, null, false, true, null, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());
        Product product2 = Product.reconstitute(product2Id, "PROD-002", "Product 2", null, null, false, true, null, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        CreateQuoteRequest request = new CreateQuoteRequest(
                customerId, null, null,
                List.of(
                        new CreateQuoteRequest.QuoteLineRequest(
                                product1Id, BigDecimal.ONE, new BigDecimal("100.0000"),
                                List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.ONE))
                        ),
                        new CreateQuoteRequest.QuoteLineRequest(
                                product2Id, BigDecimal.valueOf(2), new BigDecimal("50.0000"),
                                List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.valueOf(2)))
                        )
                )
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(product1Id)).thenReturn(Optional.of(product1));
        when(productRepository.findById(product2Id)).thenReturn(Optional.of(product2));
        when(warehouseRepository.findById(any())).thenReturn(Optional.of(buildWarehouse(UUID.randomUUID())));
        when(quoteRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteDetailResponse response = createQuoteUseCase.execute(request, authenticatedUserId);

        assertEquals(2, response.lines().size());
        assertEquals(0, new BigDecimal("200.0000").compareTo(response.subtotal()));
    }

    // --- Test Helpers ---

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
                productId,
                "PROD-001",
                "Product A",
                null,
                null,
                false,
                true,
                null,
                BigDecimal.TEN,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(0.25),
                List.of(),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Warehouse buildWarehouse(UUID warehouseId) {
        return Warehouse.reconstitute(warehouseId, "WH-001", "Main Warehouse", "123 Main St", null, null, null, true, LocalDateTime.now(), LocalDateTime.now());
    }
}
