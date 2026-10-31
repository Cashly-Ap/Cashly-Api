package com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case;

import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementGroupBy;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementGroup;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.ProductOption;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.Summary;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.WarehouseOption;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockMovementQueryGateway;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStockMovementsUseCase Tests")
class GetStockMovementsUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementQueryGateway stockMovementQueryGateway;

    private GetStockMovementsUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetStockMovementsUseCase(warehouseRepository,
                stockMovementRepository, new LocalStockCatalogGatewayAdapter(
                        new RepositoryStockCatalogQuery(productRepository)),
                stockMovementQueryGateway);
    }

    @Test
    @DisplayName("returns paginated movements with product info")
    void shouldReturnPaginatedMovementsWithProductInfo() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        StockMovement movement = StockMovement.create(warehouseId, productId,
                MovementType.ENTRY, 10, 0, 10, null, null, "Entry");

        PageResponse<StockMovement> page = new PageResponse<>(List.of(movement),
                1L, 1, 10, 0, true, true, false);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseId(warehouseId, 0, 10))
                .thenReturn(page);
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        PageResponse<StockMovementResponse> response = useCase
                .execute(warehouseId, null, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("PROD-001", response.content().get(0).productCode());
        assertEquals("Test Product", response.content().get(0).productName());
        assertEquals(MovementType.ENTRY,
                response.content().get(0).movementType());
        verify(warehouseRepository).findById(warehouseId);
        verify(stockMovementRepository).findByWarehouseId(warehouseId, 0, 10);
    }

    @Test
    @DisplayName("with productId filter: calls findByWarehouseIdAndProductId")
    void shouldCallFindByWarehouseIdAndProductIdWhenProductIdProvided() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        StockMovement movement = StockMovement.create(warehouseId, productId,
                MovementType.EXIT, 5, 100, 95, null, null, "Exit");

        PageResponse<StockMovement> page = new PageResponse<>(List.of(movement),
                1L, 1, 10, 0, true, true, false);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseIdAndProductId(warehouseId,
                productId, 0, 10)).thenReturn(page);
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        PageResponse<StockMovementResponse> response = useCase
                .execute(warehouseId, productId, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        verify(stockMovementRepository)
                .findByWarehouseIdAndProductId(warehouseId, productId, 0, 10);
        verify(stockMovementRepository, never()).findByWarehouseId(any(),
                anyInt(), anyInt());
    }

    @Test
    @DisplayName("without productId: calls findByWarehouseId")
    void shouldCallFindByWarehouseIdWhenNoProductId() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);

        PageResponse<StockMovement> page = new PageResponse<>(List.of(), 0L, 0,
                10, 0, true, true, true);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseId(warehouseId, 0, 10))
                .thenReturn(page);

        PageResponse<StockMovementResponse> response = useCase
                .execute(warehouseId, null, null, 0, 10);

        assertNotNull(response);
        verify(stockMovementRepository).findByWarehouseId(warehouseId, 0, 10);
        verify(stockMovementRepository, never()).findByWarehouseIdAndProductId(
                any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("with movement type filter: delegates filtering before pagination")
    void shouldFilterByMovementTypeBeforePagination() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        PageResponse<StockMovement> page = new PageResponse<>(List.of(), 0L, 0,
                10, 0, true, true, true);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseIdAndMovementType(
                warehouseId, MovementType.EXIT, 0, 10)).thenReturn(page);

        useCase.execute(warehouseId, null, MovementType.EXIT, 0, 10);

        verify(stockMovementRepository).findByWarehouseIdAndMovementType(
                warehouseId, MovementType.EXIT, 0, 10);
        verify(stockMovementRepository, never()).findByWarehouseId(any(),
                anyInt(), anyInt());
    }

    @Test
    @DisplayName("with product and movement type filters: combines both filters")
    void shouldCombineProductAndMovementTypeFilters() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        PageResponse<StockMovement> page = new PageResponse<>(List.of(), 0L, 0,
                10, 0, true, true, true);

        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        when(stockMovementRepository
                .findByWarehouseIdAndProductIdAndMovementType(warehouseId,
                        productId, MovementType.ENTRY, 0, 10))
                .thenReturn(page);

        useCase.execute(warehouseId, productId, MovementType.ENTRY, 0, 10);

        verify(stockMovementRepository)
                .findByWarehouseIdAndProductIdAndMovementType(warehouseId,
                        productId, MovementType.ENTRY, 0, 10);
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(warehouseId, null, null, 0, 10));

        verify(warehouseRepository).findById(warehouseId);
        verify(stockMovementRepository, never()).findByWarehouseId(any(),
                anyInt(), anyInt());
    }

    @Test
    void shouldPreserveUnknownProductLabelsInHistoricalMovements() {
        StockMovement movement = StockMovement.create(warehouseId, productId,
                MovementType.ENTRY, 10, 0, 10, null, null, "Historical");
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(buildActiveWarehouse(warehouseId)));
        when(stockMovementRepository.findByWarehouseId(warehouseId, 0, 10))
                .thenReturn(new PageResponse<>(List.of(movement), 1L, 1, 10, 0,
                        true, true, false));
        var response = useCase.execute(warehouseId, null, null, 0, 10);
        assertEquals("UNKNOWN", response.content().getFirst().productCode());
        assertEquals("Unknown", response.content().getFirst().productName());
        assertEquals(MovementType.ENTRY,
                response.content().getFirst().movementType());
        assertEquals(1L, response.totalElements());
        assertEquals(10, response.size());
    }
    @Test
    @DisplayName("global load: delegates to the query gateway without warehouse validation")
    void shouldLoadGlobalMovementsFromQueryGateway() {
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                null, 0, 20, StockMovementGroupBy.NONE);
        StockMovementsResponse expected = globalResponse(
                StockMovementGroupBy.NONE);
        when(stockMovementQueryGateway.search(filter)).thenReturn(expected);

        StockMovementsResponse response = useCase.execute(filter);

        assertSame(expected, response);
        assertSame(expected.summary(), response.summary());
        assertEquals(2, response.warehouses().size());
        assertEquals(1, response.products().size());
        verify(stockMovementQueryGateway).search(filter);
        verifyNoInteractions(warehouseRepository);
    }

    @Test
    @DisplayName("global open view: keeps summary and filter options untouched")
    void shouldKeepSummaryAndOptionsOnTheOpenView() {
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                "", 0, 20, StockMovementGroupBy.PRODUCT);
        StockMovementsResponse expected = globalResponse(
                StockMovementGroupBy.PRODUCT);
        when(stockMovementQueryGateway.search(filter)).thenReturn(expected);

        StockMovementsResponse response = useCase.execute(filter);

        assertSame(expected, response);
        assertEquals(8L, response.summary().entryQuantity());
        assertEquals(2L, response.summary().exitQuantity());
        assertEquals(6L, response.summary().netQuantity());
        assertEquals(List.of("P-1"),
                response.products().stream().map(ProductOption::code).toList());
        assertEquals(List.of("Main", "North"),
                response.warehouses().stream().map(WarehouseOption::name)
                        .toList());
        verifyNoInteractions(warehouseRepository);
    }

    @Test
    @DisplayName("warehouse filter: validates the warehouse and forwards the filter")
    void shouldValidateWarehouseWhenFilteringGlobalMovements() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        StockMovementFilter filter = new StockMovementFilter(warehouseId, null,
                null, null, 0, 20, StockMovementGroupBy.NONE);
        StockMovementsResponse expected = globalResponse(
                StockMovementGroupBy.NONE);
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        when(stockMovementQueryGateway.search(filter)).thenReturn(expected);

        assertSame(expected, useCase.execute(filter));

        verify(warehouseRepository).findById(warehouseId);
        verify(stockMovementQueryGateway).search(filter);
    }

    @Test
    @DisplayName("warehouse filter: rejects an unknown warehouse before querying")
    void shouldRejectUnknownWarehouseOnGlobalLoad() {
        StockMovementFilter filter = new StockMovementFilter(warehouseId, null,
                MovementType.ENTRY, null, 0, 20,
                StockMovementGroupBy.WAREHOUSE);
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(filter));

        verifyNoInteractions(stockMovementQueryGateway);
    }

    @Test
    @DisplayName("movement type and grouping: forwarded as one server-side filter")
    void shouldForwardMovementTypeAndGroupingToTheQueryGateway() {
        StockMovementFilter filter = new StockMovementFilter(null, productId,
                MovementType.EXIT, "  APP  ", 2, 10,
                StockMovementGroupBy.MOVEMENT_TYPE);
        StockMovementsResponse expected = globalResponse(
                StockMovementGroupBy.MOVEMENT_TYPE);
        when(stockMovementQueryGateway.search(filter)).thenReturn(expected);

        assertSame(expected, useCase.execute(filter));

        verify(stockMovementQueryGateway).search(new StockMovementFilter(null,
                productId, MovementType.EXIT, "APP", 2, 10,
                StockMovementGroupBy.MOVEMENT_TYPE));
        verifyNoInteractions(warehouseRepository);
    }

    @Test
    @DisplayName("empty query: returns the gateway page untouched")
    void shouldReturnEmptyGlobalPage() {
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                "missing", 0, 20, StockMovementGroupBy.NONE);
        PageResponse<StockMovementResponse> empty = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);
        StockMovementsResponse expected = StockMovementsResponse.ungrouped(
                Summary.empty(), List.of(), List.of(), empty);
        when(stockMovementQueryGateway.search(filter)).thenReturn(expected);

        StockMovementsResponse response = useCase.execute(filter);

        assertTrue(response.movements().empty());
        assertEquals(0L, response.movements().totalElements());
        assertEquals(0L, response.summary().movementCount());
        assertTrue(response.warehouses().isEmpty());
        assertTrue(response.products().isEmpty());
        assertNull(response.groups());
    }

    @ParameterizedTest
    @EnumSource(StockMovementGroupBy.class)
    @DisplayName("grouping: forwards every supported mode to the query gateway")
    void shouldForwardEveryGroupingMode(StockMovementGroupBy groupBy) {
        StockMovementFilter filter = new StockMovementFilter(null, productId,
                MovementType.EXIT, " APP ", 1, 25, groupBy);
        StockMovementsResponse expected = globalResponse(groupBy);
        when(stockMovementQueryGateway.search(filter)).thenReturn(expected);

        assertSame(expected, useCase.execute(filter));

        verify(stockMovementQueryGateway).search(new StockMovementFilter(null,
                productId, MovementType.EXIT, "APP", 1, 25, groupBy));
        verifyNoInteractions(warehouseRepository);
    }

    @Test
    @DisplayName("combined filters: validates the warehouse and forwards everything")
    void shouldValidateWarehouseAndForwardCombinedFilters() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        StockMovementFilter filter = new StockMovementFilter(warehouseId,
                productId, MovementType.ENTRY, " bolt ", 2, 10,
                StockMovementGroupBy.PRODUCT);
        when(warehouseRepository.findById(warehouseId))
                .thenReturn(Optional.of(warehouse));
        when(stockMovementQueryGateway.search(filter))
                .thenReturn(globalResponse(StockMovementGroupBy.PRODUCT));

        useCase.execute(filter);

        verify(warehouseRepository).findById(warehouseId);
        verify(stockMovementQueryGateway)
                .search(new StockMovementFilter(warehouseId, productId,
                        MovementType.ENTRY, "bolt", 2, 10,
                        StockMovementGroupBy.PRODUCT));
    }

    private StockMovementsResponse globalResponse(
            StockMovementGroupBy groupBy) {
        Summary summary = new Summary(3, 1, 2, 8, 2, 6);
        List<WarehouseOption> warehouses = List.of(
                new WarehouseOption("w-1", "Main"),
                new WarehouseOption("w-2", "North"));
        List<ProductOption> products = List.of(new ProductOption("p-1",
                "Product", "P-1"));
        PageResponse<StockMovementResponse> movements = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);
        PageResponse<StockMovementGroup> groups = new PageResponse<>(List.of(),
                0L, 0, 20, 0, true, true, true);
        return groupBy == StockMovementGroupBy.NONE
                ? StockMovementsResponse.ungrouped(summary, warehouses, products,
                        movements)
                : StockMovementsResponse.grouped(summary, warehouses, products,
                        groupBy, groups);
    }

    private Warehouse buildActiveWarehouse(UUID id) {
        return Warehouse.reconstitute(id, "WH-001", "Main", "Addr", "State",
                "Mun", "10001", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Product buildProduct(UUID id, String code, String name) {
        return Product.reconstitute(id, code, name, "Desc", null, false, true,
                null, BigDecimal.ZERO, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
