package com.cashly.cashly_api.catalog.products.infrastructure.integration;

import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.in.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.partners.customers.profile.infrastructure.config.CustomerProductPricingConfig;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.config.ProductStockQueryConfig;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductStockGatewayIntegrationTest {
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CustomerWarehouseAccessQuery access = mock(
            CustomerWarehouseAccessQuery.class);
    private final LocalCustomerProductContextGatewayAdapter context = new LocalCustomerProductContextGatewayAdapter(
            new CustomerProductPricingConfig()
                    .customerProductPricingQuery(customers),
            access);
    private final UUID customerId = UUID.randomUUID();

    @Test
    void shouldReturnFirstCustomerPricingAndForwardPermissionQuery() {
        UUID priceTypeId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        when(customer.getPriceTypeId()).thenReturn(priceTypeId);
        when(customers.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(customer));
        when(access.accessibleWarehouseIds(customerId))
                .thenReturn(Set.of(warehouseId));
        assertEquals(priceTypeId, context
                .requirePriceType(List.of(customerId, UUID.randomUUID())));
        assertEquals(Set.of(warehouseId),
                context.accessibleWarehouseIds(customerId));
        verify(customers).findById(new CustomerId(customerId));
    }

    @Test
    void shouldAllowExistingCustomerWithoutPriceType() {
        when(customers.findById(new CustomerId(customerId)))
                .thenReturn(Optional.of(mock(Customer.class)));
        assertNull(context.requirePriceType(List.of(customerId)));
    }

    @Test
    void shouldPreserveMissingCustomerErrors() {
        var missing = assertThrows(CustomerNotFoundException.class,
                () -> context.requirePriceType(List.of(customerId)));
        assertEquals("Cliente no encontrado: " + customerId,
                missing.getMessage());
        var empty = assertThrows(CustomerNotFoundException.class,
                () -> context.requirePriceType(List.of()));
        assertEquals("Debe indicar al menos un cliente", empty.getMessage());
        assertThrows(CustomerNotFoundException.class,
                () -> context.requirePriceType(null));
    }

    @Test
    void shouldPreserveStockMetadataAndMissingWarehouseFallback() {
        WarehouseStockRepository stocks = mock(WarehouseStockRepository.class);
        WarehouseRepository warehouses = mock(WarehouseRepository.class);
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID deletedWarehouseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        WarehouseStock first = new WarehouseStock(UUID.randomUUID(),
                warehouseId, productId, 10, 3, now, now);
        WarehouseStock second = new WarehouseStock(UUID.randomUUID(),
                deletedWarehouseId, productId, 5, 1, now, now);
        when(stocks.findByProductId(productId))
                .thenReturn(List.of(first, second));
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getCode()).thenReturn("WH");
        when(warehouse.getName()).thenReturn("Warehouse");
        when(warehouses.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        var gateway = new LocalProductWarehouseStockGatewayAdapter(
                new ProductStockQueryConfig().productStockQuery(stocks,
                        warehouses));
        var result = gateway.findByProductId(productId);
        assertEquals(2, result.size());
        assertEquals("WH", result.getFirst().warehouseCode());
        assertEquals("Warehouse", result.getFirst().warehouseName());
        assertTrue(result.getFirst().warehouseExists());
        assertEquals(10, result.getFirst().quantity());
        assertEquals(3, result.getFirst().reserved());
        assertEquals(7, result.getFirst().available());
        assertEquals(now, result.getFirst().lastMovementAt());
        assertEquals("Unknown", result.getLast().warehouseName());
        assertFalse(result.getLast().warehouseExists());
        assertThrows(UnsupportedOperationException.class, () -> result.clear());
    }
}
