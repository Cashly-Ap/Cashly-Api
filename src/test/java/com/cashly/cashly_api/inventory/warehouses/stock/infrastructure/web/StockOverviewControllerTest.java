package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockOverviewQueryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewGroupBy;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case.GetStockOverviewUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(StockOverviewControllerTest.Config.class)
class StockOverviewControllerTest {
    @Autowired private StockOverviewController controller;
    @Autowired private StockOverviewQueryGateway gateway;

    @BeforeEach
    void resetMocks() {
        reset(gateway);
    }

    @Test
    @WithMockUser(authorities = "INVENTORY:READ")
    void readsStockAndOptionsWithoutCatalogPermission() throws Exception {
        var response = new StockOverviewResponse(new PageResponse<>(List.of(), 0, 0, 10, 0, true, true, true),
                new StockOverviewResponse.Summary(0, 0, 0, 0, 0),
                List.of(new StockOverviewResponse.Option("warehouse-id", "Main")),
                List.of(new StockOverviewResponse.Option("category-id", "Category")));
        when(gateway.search(any())).thenReturn(response);
        MockMvcBuilders.standaloneSetup(controller).build()
                .perform(get("/api/warehouses/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.totalElements").value(0))
                .andExpect(jsonPath("$.summary.availableQuantity").value(0))
                .andExpect(jsonPath("$.warehouses[0].name").value("Main"))
                .andExpect(jsonPath("$.categories[0].name").value("Category"));
        verify(gateway).search(new StockOverviewFilter(null, null, "", 0, 10));
    }

    @Test
    @WithMockUser(authorities = "CATALOG:READ")
    void rejectsCallersWithoutInventoryRead() {
        assertThatThrownBy(() -> controller.getStock(null, null, null, 0, 10)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(gateway);
    }

    @Test
    @WithMockUser(authorities = "INVENTORY:READ")
    void bindsGroupingAndRejectsUnknownGrouping() throws Exception {
        var response = new StockOverviewResponse(null, new StockOverviewResponse.Summary(0, 0, 0, 0, 0),
                List.of(), List.of(), StockOverviewGroupBy.PRODUCT,
                new PageResponse<>(List.of(), 0, 0, 1, 0, true, true, true));
        when(gateway.search(any())).thenReturn(response);
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(get("/api/warehouses/stock").param("groupBy", "PRODUCT").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupBy").value("PRODUCT"))
                .andExpect(jsonPath("$.groups.totalElements").value(0));
        verify(gateway).search(new StockOverviewFilter(null, null, "", 0, 1, StockOverviewGroupBy.PRODUCT));
        clearInvocations(gateway);
        mvc.perform(get("/api/warehouses/stock").param("groupBy", "UNKNOWN"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(gateway);
    }

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean
        StockOverviewQueryGateway gateway() {
            return mock(StockOverviewQueryGateway.class);
        }

        @Bean
        GetStockOverviewUseCase useCase(StockOverviewQueryGateway gateway) {
            return new GetStockOverviewUseCase(gateway);
        }

        @Bean
        StockOverviewController controller(GetStockOverviewUseCase useCase) {
            return new StockOverviewController(useCase);
        }
    }
}
