package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWarehousesForQuoteUseCaseTest {
    @Mock QuoteWarehouseGateway warehouses;
    private GetWarehousesForQuoteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetWarehousesForQuoteUseCase(warehouses);
    }

    @Test
    void shouldReturnAccessibleWarehousesUsingTheirPublicDirectoryData() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(warehouses.accessibleWarehouseIds(customerId)).thenReturn(Set.of(warehouseId));
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(
                new QuoteWarehouseGateway.Warehouse(warehouseId, "Main", "Street 1")));

        var result = useCase.execute(customerId);

        assertThat(result).singleElement().satisfies(warehouse -> {
            assertThat(warehouse.id()).isEqualTo(warehouseId.toString());
            assertThat(warehouse.name()).isEqualTo("Main");
            assertThat(warehouse.location()).isEqualTo("Street 1");
        });
    }

    @Test
    void shouldSkipDirectoryEntriesThatNoLongerExist() {
        UUID customerId = UUID.randomUUID();
        when(warehouses.accessibleWarehouseIds(customerId)).thenReturn(Set.of(UUID.randomUUID()));

        assertThat(useCase.execute(customerId)).isEmpty();
    }
}
