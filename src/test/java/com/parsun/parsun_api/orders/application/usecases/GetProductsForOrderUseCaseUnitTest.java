package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.categories.domain.entities.Category;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.orders.application.dto.response.OrderProductResponse;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.products.domain.valueobjects.ProductPrice;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductsForOrderUseCase Unit Tests")
class GetProductsForOrderUseCaseUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerWarehouseAccessRepository customerWarehouseAccessRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private GetProductsForOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductsForOrderUseCase(
                customerRepository,
                customerWarehouseAccessRepository,
                productRepository,
                categoryRepository,
                warehouseStockRepository,
                warehouseRepository
        );
    }

    @Test
    void should_ReturnProductsPage_When_CustomerExists() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Customer customer = mock(Customer.class);
        when(customer.getPriceTypeId()).thenReturn(null);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));

        CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                UUID.randomUUID(), customerId, warehouseId, LocalDateTime.now(), UUID.randomUUID());
        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));

        Warehouse warehouse = Warehouse.reconstitute(
                warehouseId, "WH-01", "Main Warehouse", "Addr", "State", "Mun", "PC", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));

        Product product = Product.reconstitute(
                productId, "PROD-001", "Test Product", "Desc", categoryId,
                false, true, null,
                BigDecimal.valueOf(100), BigDecimal.valueOf(60), BigDecimal.valueOf(40),
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        PageResponse<Product> productPage = new PageResponse<>(
                List.of(product), 1, 1, 20, 0, true, true, false);
        when(productRepository.findAllForOrder(
                any(), any(), any(), any(), anySet(), anyInt(), anyInt()))
                .thenReturn(productPage);

        Category category = Category.reconstitute(categoryId, "Category A", "Desc", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        WarehouseStock stock = new WarehouseStock(
                UUID.randomUUID(), warehouseId, productId, 50, 5,
                LocalDateTime.now(), LocalDateTime.now());
        when(warehouseStockRepository.findByProductId(productId))
                .thenReturn(List.of(stock));

        PageResponse<OrderProductResponse> result = useCase.execute(
                customerId, null, null, null, null, 0, 20);

        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(1, result.content().size());

        OrderProductResponse response = result.content().getFirst();
        assertEquals(productId.toString(), response.id());
        assertEquals("PROD-001", response.code());
        assertEquals("Test Product", response.name());
        assertEquals(categoryId.toString(), response.categoryId());
        assertEquals("Category A", response.categoryName());
        assertEquals(BigDecimal.valueOf(100), response.customerPrice());
        assertEquals(50, response.totalStock());
        assertEquals(1, response.stock().size());
        assertEquals(warehouseId.toString(), response.stock().getFirst().warehouseId());
        assertEquals("WH-01", response.stock().getFirst().warehouseCode());
        assertEquals("Main Warehouse", response.stock().getFirst().warehouseName());
        assertEquals(50, response.stock().getFirst().quantity());
    }

    @Test
    void should_ThrowCustomerNotFoundException_When_CustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> useCase.execute(customerId, null, null, null, null, 0, 20)
        );

        verifyNoInteractions(
                customerWarehouseAccessRepository,
                productRepository,
                categoryRepository,
                warehouseStockRepository,
                warehouseRepository
        );
    }

    @Test
    void should_ResolveCustomerPrice_When_PriceTypeMatches() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID priceTypeId = UUID.randomUUID();

        Customer customer = mock(Customer.class);
        when(customer.getPriceTypeId()).thenReturn(priceTypeId);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));

        CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                UUID.randomUUID(), customerId, warehouseId, LocalDateTime.now(), UUID.randomUUID());
        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));

        Warehouse warehouse = Warehouse.reconstitute(
                warehouseId, "WH-01", "Main Warehouse", "Addr", "State", "Mun", "PC", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));

        ProductPrice customerPrice = ProductPrice.create(priceTypeId, BigDecimal.valueOf(85));
        Product product = Product.reconstitute(
                productId, "PROD-002", "Priced Product", "Desc", categoryId,
                false, true, null,
                BigDecimal.valueOf(100), BigDecimal.valueOf(60), BigDecimal.valueOf(40),
                List.of(), List.of(customerPrice),
                LocalDateTime.now(), LocalDateTime.now());
        PageResponse<Product> productPage = new PageResponse<>(
                List.of(product), 1, 1, 20, 0, true, true, false);
        when(productRepository.findAllForOrder(
                any(), any(), any(), any(), anySet(), anyInt(), anyInt()))
                .thenReturn(productPage);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(Category.reconstitute(
                        categoryId, "Cat", "Desc", true, LocalDateTime.now(), LocalDateTime.now())));

        when(warehouseStockRepository.findByProductId(productId))
                .thenReturn(List.of());

        PageResponse<OrderProductResponse> result = useCase.execute(
                customerId, null, null, null, null, 0, 20);

        assertEquals(BigDecimal.valueOf(85), result.content().getFirst().customerPrice());
    }

    @Test
    void should_FallbackToBasePrice_When_NoPriceType() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Customer customer = mock(Customer.class);
        when(customer.getPriceTypeId()).thenReturn(null);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));

        CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                UUID.randomUUID(), customerId, warehouseId, LocalDateTime.now(), UUID.randomUUID());
        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));

        Warehouse warehouse = Warehouse.reconstitute(
                warehouseId, "WH-01", "WH", "Addr", "State", "Mun", "PC", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));

        Product product = Product.reconstitute(
                productId, "PROD-003", "Base Price Product", "Desc", categoryId,
                false, true, null,
                BigDecimal.valueOf(75), BigDecimal.valueOf(45), BigDecimal.valueOf(30),
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        PageResponse<Product> productPage = new PageResponse<>(
                List.of(product), 1, 1, 20, 0, true, true, false);
        when(productRepository.findAllForOrder(
                any(), any(), any(), any(), anySet(), anyInt(), anyInt()))
                .thenReturn(productPage);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(Category.reconstitute(
                        categoryId, "Cat", "Desc", true, LocalDateTime.now(), LocalDateTime.now())));

        when(warehouseStockRepository.findByProductId(productId))
                .thenReturn(List.of());

        PageResponse<OrderProductResponse> result = useCase.execute(
                customerId, null, null, null, null, 0, 20);

        assertEquals(BigDecimal.valueOf(75), result.content().getFirst().customerPrice());
    }

    @Test
    void should_ReturnEmptyPage_When_NoProductsMatch() {
        UUID customerId = UUID.randomUUID();

        
        Customer customer = mock(Customer.class);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));

        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of());

        PageResponse<Product> emptyPage = new PageResponse<>(
                List.of(), 0, 0, 20, 0, true, true, true);
        when(productRepository.findAllForOrder(
                any(), any(), any(), any(), anySet(), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        PageResponse<OrderProductResponse> result = useCase.execute(
                customerId, null, null, null, null, 0, 20);

        assertTrue(result.empty());
        assertEquals(0, result.content().size());
        verifyNoInteractions(warehouseRepository, categoryRepository, warehouseStockRepository);
    }

    @Test
    void should_FilterStockByAllowedWarehouses() {
        UUID customerId = UUID.randomUUID();
        UUID allowedWarehouseId = UUID.randomUUID();
        UUID otherWarehouseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Customer customer = mock(Customer.class);
        when(customer.getPriceTypeId()).thenReturn(null);

        when(customerRepository.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));

        CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                UUID.randomUUID(), customerId, allowedWarehouseId, LocalDateTime.now(), UUID.randomUUID());
        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));

        Warehouse warehouse = Warehouse.reconstitute(
                allowedWarehouseId, "WH-01", "Allowed WH", "Addr", "State", "Mun", "PC", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(warehouseRepository.findById(allowedWarehouseId))
                .thenReturn(Optional.of(warehouse));

        Product product = Product.reconstitute(
                productId, "PROD-004", "Stock Product", "Desc", categoryId,
                false, true, null,
                BigDecimal.valueOf(50), BigDecimal.valueOf(30), BigDecimal.valueOf(20),
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        PageResponse<Product> productPage = new PageResponse<>(
                List.of(product), 1, 1, 20, 0, true, true, false);
        when(productRepository.findAllForOrder(
                any(), any(), any(), any(), anySet(), anyInt(), anyInt()))
                .thenReturn(productPage);

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(Category.reconstitute(
                        categoryId, "Cat", "Desc", true, LocalDateTime.now(), LocalDateTime.now())));

        WarehouseStock allowedStock = new WarehouseStock(
                UUID.randomUUID(), allowedWarehouseId, productId, 30, 2,
                LocalDateTime.now(), LocalDateTime.now());
        WarehouseStock otherStock = new WarehouseStock(
                UUID.randomUUID(), otherWarehouseId, productId, 100, 10,
                LocalDateTime.now(), LocalDateTime.now());
        when(warehouseStockRepository.findByProductId(productId))
                .thenReturn(List.of(allowedStock, otherStock));

        PageResponse<OrderProductResponse> result = useCase.execute(
                customerId, null, null, null, null, 0, 20);

        assertEquals(1, result.content().size());
        OrderProductResponse response = result.content().getFirst();
        assertEquals(30, response.totalStock());
        assertEquals(1, response.stock().size());
        assertEquals(allowedWarehouseId.toString(), response.stock().getFirst().warehouseId());
    }
}
