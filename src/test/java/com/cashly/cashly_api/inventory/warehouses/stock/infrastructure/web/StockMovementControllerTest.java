package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.infrastructure.config.GlobalExceptionHandler;
import com.cashly.cashly_api.inventory.warehouses.management.application.create.use_case.CreateWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.delete.use_case.DeleteWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.queries.use_case.GetWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.queries.use_case.ListWarehousesUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.application.update.use_case.UpdateWarehouseUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.infrastructure.web.WarehouseController;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementGroupBy;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementGroup;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.ProductOption;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.Summary;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.WarehouseOption;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case.GetStockMovementsUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementController Unit Tests")
class StockMovementControllerTest {

    @Mock
    private GetStockMovementsUseCase getStockMovementsUseCase;

    @InjectMocks
    private StockMovementController stockMovementController;

    @Test
    @DisplayName("should get movements and return page")
    void should_get_movements_and_return_page() {
        UUID warehouseId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);
        PageResponse<StockMovementResponse> pageResponse = new PageResponse<>(
                List.of(movement), 1L, 1, 20, 0, true, true, false);

        when(getStockMovementsUseCase.execute(eq(warehouseId), any(), any(),
                anyInt(), anyInt())).thenReturn(pageResponse);

        PageResponse<StockMovementResponse> result = stockMovementController
                .getMovements(warehouseId, null, null, 0, 20);

        assertEquals(1, result.content().size());
        verify(getStockMovementsUseCase).execute(warehouseId, null, null, 0,
                20);
    }

    @Test
    @DisplayName("should get movements filtered by product")
    void should_get_movements_filtered_by_product() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockMovementResponse movement = mock(StockMovementResponse.class);
        PageResponse<StockMovementResponse> pageResponse = new PageResponse<>(
                List.of(movement), 1L, 1, 20, 0, true, true, false);

        when(getStockMovementsUseCase.execute(eq(warehouseId), eq(productId),
                any(), anyInt(), anyInt())).thenReturn(pageResponse);

        PageResponse<StockMovementResponse> result = stockMovementController
                .getMovements(warehouseId, productId, null, 0, 20);

        assertEquals(1, result.content().size());
        verify(getStockMovementsUseCase).execute(warehouseId, productId, null,
                0, 20);
    }

    @Test
    @DisplayName("should get movements filtered by movement type")
    void should_get_movements_filtered_by_movement_type() {
        UUID warehouseId = UUID.randomUUID();
        PageResponse<StockMovementResponse> pageResponse = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);

        when(getStockMovementsUseCase.execute(warehouseId, null,
                MovementType.EXIT, 0, 20)).thenReturn(pageResponse);

        stockMovementController.getMovements(warehouseId, null,
                MovementType.EXIT, 0, 20);

        verify(getStockMovementsUseCase).execute(warehouseId, null,
                MovementType.EXIT, 0, 20);
    }

    @Test
    @DisplayName("should return empty movements page")
    void should_return_empty_movements_page() {
        UUID warehouseId = UUID.randomUUID();
        PageResponse<StockMovementResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);

        when(getStockMovementsUseCase.execute(eq(warehouseId), any(), any(),
                anyInt(), anyInt())).thenReturn(emptyPage);

        PageResponse<StockMovementResponse> result = stockMovementController
                .getMovements(warehouseId, null, null, 0, 20);

        assertTrue(result.content().isEmpty());
        verify(getStockMovementsUseCase).execute(warehouseId, null, null, 0,
                20);
    }

    @Test
    @DisplayName("should load global movements without warehouse filter")
    void should_load_global_movements() {
        StockMovementsResponse response = emptyGlobalResponse();
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                null, 0, 20, StockMovementGroupBy.NONE);
        when(getStockMovementsUseCase.execute(filter)).thenReturn(response);

        StockMovementsResponse result = stockMovementController
                .getGlobalMovements(null, null, null, null, null, null, null);

        assertSame(response, result);
        verify(getStockMovementsUseCase).execute(filter);
    }

    @Test
    @DisplayName("should forward warehouse filter and grouping")
    void should_forward_warehouse_filter_and_grouping() {
        UUID warehouseId = UUID.randomUUID();
        StockMovementsResponse response = StockMovementsResponse.grouped(
                Summary.empty(), List.of(), List.of(),
                StockMovementGroupBy.WAREHOUSE,
                new PageResponse<>(List.of(), 0L, 0, 20, 0, true, true, true));
        StockMovementFilter filter = new StockMovementFilter(warehouseId,
                null, null, null, 0, 20, StockMovementGroupBy.WAREHOUSE);
        when(getStockMovementsUseCase.execute(filter)).thenReturn(response);

        StockMovementsResponse result = stockMovementController
                .getGlobalMovements(warehouseId.toString(), null, null, null,
                        null, null, "WAREHOUSE");

        assertSame(response, result);
        verify(getStockMovementsUseCase).execute(filter);
    }

    @Test
    @DisplayName("should forward movement type and search filters")
    void should_forward_movement_type_and_search_filters() {
        UUID productId = UUID.randomUUID();
        StockMovementsResponse response = emptyGlobalResponse();
        StockMovementFilter filter = new StockMovementFilter(null, productId,
                MovementType.EXIT, "APP", 1, 10, StockMovementGroupBy.NONE);
        when(getStockMovementsUseCase.execute(filter)).thenReturn(response);

        StockMovementsResponse result = stockMovementController
                .getGlobalMovements(null, productId.toString(), "EXIT", "APP",
                        "1", "10", "NONE");

        assertSame(response, result);
        verify(getStockMovementsUseCase).execute(filter);
    }

    @Test
    @DisplayName("should return empty grouped movements")
    void should_return_empty_grouped_movements() {
        StockMovementsResponse response = StockMovementsResponse.grouped(
                Summary.empty(), List.of(), List.of(),
                StockMovementGroupBy.MOVEMENT_TYPE,
                new PageResponse<>(List.of(), 0L, 0, 20, 0, true, true, true));
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                "missing", 0, 20, StockMovementGroupBy.MOVEMENT_TYPE);
        when(getStockMovementsUseCase.execute(filter)).thenReturn(response);

        StockMovementsResponse result = stockMovementController
                .getGlobalMovements(null, null, null, "missing", "0", "20",
                        "MOVEMENT_TYPE");

        assertSame(response, result);
        assertTrue(result.groups().empty());
        verify(getStockMovementsUseCase).execute(filter);
    }

    @Test
    @DisplayName("should open the consolidated view without any filter over HTTP")
    void should_default_global_query_over_http() throws Exception {
        StockMovementFilter expected = new StockMovementFilter(null, null,
                null, null, 0, 20, StockMovementGroupBy.NONE);
        when(getStockMovementsUseCase.execute(expected))
                .thenReturn(emptyGlobalResponse());

        mvc().perform(get("/api/warehouses/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy").value("NONE"))
                .andExpect(jsonPath("$.groups").value(nullValue()))
                .andExpect(jsonPath("$.movements.totalElements").value(0));

        verify(getStockMovementsUseCase).execute(expected);
    }

    @Test
    @DisplayName("should ignore absent and blank optional filters over HTTP")
    void should_accept_blank_optional_filters_over_http() throws Exception {
        StockMovementFilter expected = new StockMovementFilter(null, null,
                null, null, 0, 20, StockMovementGroupBy.NONE);
        when(getStockMovementsUseCase.execute(expected))
                .thenReturn(emptyGlobalResponse());

        mvc().perform(get("/api/warehouses/movements")
                .param("warehouseId", "").param("productId", "")
                .param("movementType", "").param("search", "   ")
                .param("page", "").param("size", "").param("groupBy", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movements.totalElements").value(0));

        verify(getStockMovementsUseCase).execute(expected);
    }

    @Test
    @DisplayName("should bind combined filters and grouping over HTTP")
    void should_bind_combined_global_filters_over_http() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockMovementFilter expected = new StockMovementFilter(warehouseId,
                productId, MovementType.EXIT, "M-01", 2, 25,
                StockMovementGroupBy.MOVEMENT_TYPE);
        when(getStockMovementsUseCase.execute(expected))
                .thenReturn(StockMovementsResponse.grouped(Summary.empty(),
                        List.of(), List.of(),
                        StockMovementGroupBy.MOVEMENT_TYPE,
                        new PageResponse<>(List.of(), 0L, 0, 25, 2, false, true,
                                true)));

        mvc().perform(get("/api/warehouses/movements")
                .param("warehouseId", warehouseId.toString())
                .param("productId", productId.toString())
                .param("movementType", "EXIT")
                .param("search", "  M-01  ").param("page", "2")
                .param("size", "25")
                .param("groupBy", "MOVEMENT_TYPE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy").value("MOVEMENT_TYPE"))
                .andExpect(jsonPath("$.groups.number").value(2));

        verify(getStockMovementsUseCase).execute(expected);
    }

    @ParameterizedTest
    @EnumSource(StockMovementGroupBy.class)
    @DisplayName("should bind every supported grouping over HTTP")
    void should_bind_every_supported_grouping(StockMovementGroupBy groupBy)
            throws Exception {
        StockMovementFilter expected = new StockMovementFilter(null, null,
                null, null, 0, 20, groupBy);
        when(getStockMovementsUseCase.execute(expected))
                .thenReturn(emptyGlobalResponse(groupBy));

        mvc().perform(get("/api/warehouses/movements").param("groupBy",
                groupBy.name())).andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy").value(groupBy.name()));

        verify(getStockMovementsUseCase).execute(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"product", "Product", " warehouse "})
    @DisplayName("should accept case-insensitive and padded grouping values")
    void should_accept_lenient_grouping_values(String rawGroupBy)
            throws Exception {
        StockMovementGroupBy expectedGroupBy = switch (rawGroupBy.trim()
                .toLowerCase(java.util.Locale.ROOT)) {
            case "product" -> StockMovementGroupBy.PRODUCT;
            case "warehouse" -> StockMovementGroupBy.WAREHOUSE;
            default -> throw new IllegalStateException();
        };
        StockMovementFilter expected = new StockMovementFilter(null, null,
                null, null, 0, 20, expectedGroupBy);
        when(getStockMovementsUseCase.execute(expected))
                .thenReturn(emptyGlobalResponse(expectedGroupBy));

        mvc().perform(get("/api/warehouses/movements").param("groupBy",
                rawGroupBy)).andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy")
                        .value(expectedGroupBy.name()));

        verify(getStockMovementsUseCase).execute(expected);
    }

    @Test
    @DisplayName("should accept a lower-case movement type")
    void should_accept_lowercase_movement_type_over_http() throws Exception {
        StockMovementFilter expected = new StockMovementFilter(null, null,
                MovementType.ENTRY, null, 0, 20, StockMovementGroupBy.NONE);
        when(getStockMovementsUseCase.execute(expected))
                .thenReturn(emptyGlobalResponse());

        mvc().perform(get("/api/warehouses/movements").param("movementType",
                "entry")).andExpect(status().isOk());

        verify(getStockMovementsUseCase).execute(expected);
    }

    @Test
    @DisplayName("should reject unknown grouping before querying")
    void should_reject_unknown_grouping() throws Exception {
        mvc().perform(get("/api/warehouses/movements").param("groupBy",
                "UNKNOWN")).andExpect(status().isBadRequest());

        verifyNoInteractions(getStockMovementsUseCase);
    }

    @Test
    @DisplayName("should reject unknown movement types on the global endpoint")
    void should_reject_unknown_global_movement_type() throws Exception {
        mvc().perform(get("/api/warehouses/movements").param("movementType",
                "NOT_A_TYPE")).andExpect(status().isBadRequest());

        verifyNoInteractions(getStockMovementsUseCase);
    }

    @Test
    @DisplayName("should reject invalid UUID filters with a readable 400")
    void should_reject_invalid_uuid_filters() throws Exception {
        mvc().perform(get("/api/warehouses/movements").param("warehouseId",
                "not-a-uuid")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc().perform(get("/api/warehouses/movements").param("productId",
                "not-a-uuid")).andExpect(status().isBadRequest());

        verifyNoInteractions(getStockMovementsUseCase);
    }

    @Test
    @DisplayName("should reject invalid pagination values")
    void should_reject_invalid_pagination() throws Exception {
        mvc().perform(get("/api/warehouses/movements").param("page", "abc"))
                .andExpect(status().isBadRequest());
        mvc().perform(get("/api/warehouses/movements").param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getStockMovementsUseCase);
    }

    @Test
    @DisplayName("should reject a negative page over HTTP")
    void should_reject_negative_page() throws Exception {
        mvc().perform(get("/api/warehouses/movements").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getStockMovementsUseCase);
    }

    @Test
    @DisplayName("should expose the frontend summary, options, groups and items contract")
    void should_expose_the_frontend_contract() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockMovementResponse movement = new StockMovementResponse("m-1",
                warehouseId.toString(), "Central", productId.toString(), "M-01",
                "Motor 10HP", "Motores", MovementType.ENTRY, 5, 0, 5, null,
                null, null, null, null, null,
                LocalDateTime.of(2026, 10, 31, 10, 0));
        StockMovementGroup group = new StockMovementGroup(productId.toString(),
                "Motor 10HP", null, "M-01", "Motores", null, 1, 5, 0, 5,
                List.of(movement));
        StockMovementsResponse response = StockMovementsResponse.grouped(
                new Summary(1, 1, 1, 5, 0, 5),
                List.of(new WarehouseOption(warehouseId.toString(), "Central")),
                List.of(new ProductOption(productId.toString(), "Motor 10HP",
                        "M-01")),
                StockMovementGroupBy.PRODUCT,
                new PageResponse<>(List.of(group), 1L, 1, 10, 0, true, true,
                        false));
        when(getStockMovementsUseCase.execute(any())).thenReturn(response);

        mvc().perform(get("/api/warehouses/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.movementCount").value(1))
                .andExpect(jsonPath("$.summary.productCount").value(1))
                .andExpect(jsonPath("$.summary.warehouseCount").value(1))
                .andExpect(jsonPath("$.summary.entryQuantity").value(5))
                .andExpect(jsonPath("$.summary.exitQuantity").value(0))
                .andExpect(jsonPath("$.summary.netQuantity").value(5))
                .andExpect(jsonPath("$.warehouses[0].name").value("Central"))
                .andExpect(jsonPath("$.products[0].code").value("M-01"))
                .andExpect(jsonPath("$.movements").value(nullValue()))
                .andExpect(jsonPath("$.groups.content[0].categoryName")
                        .value("Motores"))
                .andExpect(jsonPath("$.groups.content[0].entryQuantity")
                        .value(5))
                .andExpect(jsonPath("$.groups.content[0].exitQuantity")
                        .value(0))
                .andExpect(jsonPath("$.groups.content[0].netQuantity")
                        .value(5))
                .andExpect(jsonPath("$.groups.content[0].items[0].categoryName")
                        .value("Motores"))
                .andExpect(jsonPath(
                        "$.groups.content[0].items[0].warehouseName")
                        .value("Central"));
    }

    @Test
    @DisplayName("should resolve the consolidated view before the warehouse id route")
    void should_route_movements_before_the_warehouse_id_pattern()
            throws Exception {
        var getWarehouseUseCase = mock(GetWarehouseUseCase.class);
        var warehouseController = new WarehouseController(
                mock(CreateWarehouseUseCase.class), getWarehouseUseCase,
                mock(ListWarehousesUseCase.class),
                mock(UpdateWarehouseUseCase.class),
                mock(DeleteWarehouseUseCase.class));
        when(getStockMovementsUseCase.execute(any()))
                .thenReturn(emptyGlobalResponse());

        MockMvcBuilders
                .standaloneSetup(stockMovementController, warehouseController)
                .setControllerAdvice(new GlobalExceptionHandler()).build()
                .perform(get("/api/warehouses/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy").value("NONE"))
                .andExpect(jsonPath("$.movements.totalElements").value(0));

        verifyNoInteractions(getWarehouseUseCase);
    }

    @Test
    @DisplayName("should accept blank optional filters on the per-warehouse endpoint")
    void should_accept_blank_filters_on_per_warehouse_endpoint()
            throws Exception {
        UUID warehouseId = UUID.randomUUID();
        PageResponse<StockMovementResponse> empty = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);
        when(getStockMovementsUseCase.execute(eq(warehouseId), isNull(),
                isNull(), eq(0), eq(20))).thenReturn(empty);

        mvc().perform(get("/api/warehouses/{warehouseId}/movements",
                warehouseId).param("productId", "")
                .param("movementType", "")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(getStockMovementsUseCase).execute(warehouseId, null, null, 0,
                20);
    }

    @Test
    @DisplayName("should keep the per-warehouse movements endpoint compatible")
    void should_keep_per_warehouse_endpoint_compatible() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        StockMovementResponse movement = new StockMovementResponse("m-1",
                warehouseId.toString(), "Central", UUID.randomUUID().toString(),
                "M-01", "Motor 10HP", "Motores", MovementType.ENTRY, 5, 0, 5,
                null, null, null, null, null, null,
                LocalDateTime.of(2026, 10, 31, 10, 0));
        PageResponse<StockMovementResponse> page = new PageResponse<>(
                List.of(movement), 1L, 1, 20, 0, true, true, false);
        when(getStockMovementsUseCase.execute(eq(warehouseId), isNull(),
                isNull(), eq(0), eq(20))).thenReturn(page);

        mvc().perform(get("/api/warehouses/{warehouseId}/movements",
                warehouseId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productCode").value("M-01"))
                .andExpect(jsonPath("$.content[0].categoryName")
                        .value("Motores"))
                .andExpect(jsonPath("$.content[0].warehouseName")
                        .value("Central"));

        verify(getStockMovementsUseCase).execute(warehouseId, null, null, 0, 20);
    }

    private StockMovementsResponse emptyGlobalResponse() {
        return emptyGlobalResponse(StockMovementGroupBy.NONE);
    }

    private StockMovementsResponse emptyGlobalResponse(
            StockMovementGroupBy groupBy) {
        PageResponse<StockMovementResponse> movements = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true);
        PageResponse<StockMovementGroup> groups = new PageResponse<>(List.of(),
                0L, 0, 20, 0, true, true, true);
        return groupBy == StockMovementGroupBy.NONE
                ? StockMovementsResponse.ungrouped(Summary.empty(), List.of(),
                        List.of(), movements)
                : StockMovementsResponse.grouped(Summary.empty(), List.of(),
                        List.of(), groupBy, groups);
    }

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(stockMovementController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
}
