package com.cashly.cashly_api.warehouses.stock.application.queries;

import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.StockAvailabilityQuery;
import com.cashly.cashly_api.warehouses.stock.infrastructure.integration.CustomerWarehouseAccessAdapter;
import com.cashly.cashly_api.customers.warehouse_access.application.queries.RepositoryCustomerWarehouseAccessQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockAvailabilityQueryService Unit Tests")
class StockAvailabilityQueryServiceTest {

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private CustomerWarehouseAccessRepository customerWarehouseAccessRepository;

    private StockAvailabilityQueryService service;

    @BeforeEach
    void setUp() {
        service = new StockAvailabilityQueryService(
                warehouseStockRepository, warehouseRepository, new CustomerWarehouseAccessAdapter(new RepositoryCustomerWarehouseAccessQuery(customerWarehouseAccessRepository))
        );
    }

    @Test
    void should_EnrichStock_When_AllocationsProvided() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Warehouse warehouse = Warehouse.reconstitute(
                warehouseId, "WH-MAIN", "Deposito Principal",
                "Dir", "Estado", "Municipio", "9999", true,
                LocalDateTime.now(), LocalDateTime.now()
        );
        WarehouseStock stock = new WarehouseStock(
                UUID.randomUUID(), warehouseId, productId, 50, 10,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        List<StockAvailabilityQuery.AllocationData> allocations = List.of(
                new StockAvailabilityQuery.AllocationData(warehouseId, productId, 5)
        );

        List<StockAvailabilityQuery.EnrichedStock> result = service.enrich(allocations);

        assertEquals(1, result.size());
        assertEquals(warehouseId, result.get(0).warehouseId());
        assertEquals(productId, result.get(0).productId());
        assertEquals("WH-MAIN", result.get(0).warehouseCode());
        assertEquals("Deposito Principal", result.get(0).warehouseName());
        assertEquals(55, result.get(0).availableStock());
        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseStockRepository).findByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Test
    void should_EnrichStock_When_MultipleAllocationsSameWarehouse() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getCode()).thenReturn("WH-MAIN");
        when(warehouse.getName()).thenReturn("Principal");
        WarehouseStock stock = new WarehouseStock(
                UUID.randomUUID(), warehouseId, productId, 100, 0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        List<StockAvailabilityQuery.AllocationData> allocations = List.of(
                new StockAvailabilityQuery.AllocationData(warehouseId, productId, 3),
                new StockAvailabilityQuery.AllocationData(warehouseId, productId, 7)
        );

        List<StockAvailabilityQuery.EnrichedStock> result = service.enrich(allocations);

        assertEquals(1, result.size());
        assertEquals(110, result.get(0).availableStock());
        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseStockRepository).findByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Test
    void should_EnrichStock_When_WarehouseNotFound() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        List<StockAvailabilityQuery.AllocationData> allocations = List.of(
                new StockAvailabilityQuery.AllocationData(warehouseId, productId, 5)
        );

        List<StockAvailabilityQuery.EnrichedStock> result = service.enrich(allocations);

        assertTrue(result.isEmpty());
    }

    @Test
    void should_EnrichStock_When_StockNotFound() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getCode()).thenReturn("WH-MAIN");
        when(warehouse.getName()).thenReturn("Principal");

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.empty());

        List<StockAvailabilityQuery.AllocationData> allocations = List.of(
                new StockAvailabilityQuery.AllocationData(warehouseId, productId, 5)
        );

        List<StockAvailabilityQuery.EnrichedStock> result = service.enrich(allocations);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).availableStock());
    }

    @Test
    void should_ReturnEmptyList_When_NoAllocations() {
        List<StockAvailabilityQuery.EnrichedStock> result = service.enrich(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(warehouseRepository, warehouseStockRepository);
    }

    @Test
    void should_GetAllStockByProducts_When_CustomerHasAccess() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                UUID.randomUUID(), customerId, warehouseId, LocalDateTime.now(), UUID.randomUUID()
        );
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getCode()).thenReturn("WH-MAIN");
        when(warehouse.getName()).thenReturn("Deposito Principal");
        WarehouseStock stock = new WarehouseStock(
                UUID.randomUUID(), warehouseId, productId, 50, 10,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));
        when(warehouseStockRepository.findByProductId(productId))
                .thenReturn(List.of(stock));
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));

        Map<UUID, List<StockAvailabilityQuery.WarehouseStockInfo>> result =
                service.getAllStockByProducts(customerId, List.of(productId));

        assertEquals(1, result.size());
        assertTrue(result.containsKey(productId));
        assertEquals(1, result.get(productId).size());
        assertEquals("WH-MAIN", result.get(productId).get(0).warehouseCode());
        assertEquals(50, result.get(productId).get(0).quantity());
        assertEquals(10, result.get(productId).get(0).reserved());
        assertEquals(40, result.get(productId).get(0).available());
        verify(customerWarehouseAccessRepository).findByCustomerId(customerId);
    }

    @Test
    void should_GetAllStockByProducts_When_CustomerHasNoAccess() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of());

        Map<UUID, List<StockAvailabilityQuery.WarehouseStockInfo>> result =
                service.getAllStockByProducts(customerId, List.of(productId));

        assertEquals(1, result.size());
        assertTrue(result.containsKey(productId));
        assertTrue(result.get(productId).isEmpty());
    }

    @Test
    void should_GetAllStockByProducts_When_MultipleProducts() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                UUID.randomUUID(), customerId, warehouseId, LocalDateTime.now(), UUID.randomUUID()
        );
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getCode()).thenReturn("WH-MAIN");
        when(warehouse.getName()).thenReturn("Principal");
        WarehouseStock stock1 = new WarehouseStock(
                UUID.randomUUID(), warehouseId, productId1, 30, 5, LocalDateTime.now(), LocalDateTime.now()
        );
        WarehouseStock stock2 = new WarehouseStock(
                UUID.randomUUID(), warehouseId, productId2, 20, 2, LocalDateTime.now(), LocalDateTime.now()
        );

        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));
        when(warehouseStockRepository.findByProductId(productId1))
                .thenReturn(List.of(stock1));
        when(warehouseStockRepository.findByProductId(productId2))
                .thenReturn(List.of(stock2));
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));

        Map<UUID, List<StockAvailabilityQuery.WarehouseStockInfo>> result =
                service.getAllStockByProducts(customerId, List.of(productId1, productId2));

        assertEquals(2, result.size());
        assertEquals(25, result.get(productId1).get(0).available());
        assertEquals(18, result.get(productId2).get(0).available());
    }

    @Test
    void should_GetAllStockByProducts_SkipWarehousesNotAccessible() {
        UUID customerId = UUID.randomUUID();
        UUID accessibleWarehouseId = UUID.randomUUID();
        UUID restrictedWarehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CustomerWarehouseAccess access = new CustomerWarehouseAccess(
                UUID.randomUUID(), customerId, accessibleWarehouseId, LocalDateTime.now(), UUID.randomUUID()
        );
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getCode()).thenReturn("WH-ACC");
        when(warehouse.getName()).thenReturn("Accessible");
        WarehouseStock accessibleStock = new WarehouseStock(
                UUID.randomUUID(), accessibleWarehouseId, productId, 10, 0,
                LocalDateTime.now(), LocalDateTime.now()
        );
        WarehouseStock restrictedStock = new WarehouseStock(
                UUID.randomUUID(), restrictedWarehouseId, productId, 99, 0,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(customerWarehouseAccessRepository.findByCustomerId(customerId))
                .thenReturn(List.of(access));
        when(warehouseStockRepository.findByProductId(productId))
                .thenReturn(List.of(accessibleStock, restrictedStock));
        when(warehouseRepository.findById(accessibleWarehouseId))
                .thenReturn(Optional.of(warehouse));

        Map<UUID, List<StockAvailabilityQuery.WarehouseStockInfo>> result =
                service.getAllStockByProducts(customerId, List.of(productId));

        assertEquals(1, result.get(productId).size());
        assertEquals(10, result.get(productId).get(0).available());
    }
}
