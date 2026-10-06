package com.cashly.cashly_api.reports.inventory.application.usecases;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.reports.inventory.application.port.CatalogReportRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.query.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCatalogReportUseCaseImpl Unit Tests")
class GetCatalogReportUseCaseImplTest {

    @Mock
    private CatalogReportRepositoryPort catalogReportRepositoryPort;

    @InjectMocks
    private GetCatalogReportUseCaseImpl useCase;

    @Test
    @DisplayName("should return catalog report page")
    void should_get_catalog_report() {
        CatalogReportQuery query = new CatalogReportQuery(
                "search", UUID.randomUUID(), UUID.randomUUID(),
                true, true, false, 0, 20);
        List<CatalogReportResponse> items = List.of(
                new CatalogReportResponse(UUID.randomUUID(), "Product A", "SKU001",
                        UUID.randomUUID(), "Category", false,
                        UUID.randomUUID(), "Warehouse", BigDecimal.TEN, true));

        when(catalogReportRepositoryPort.findCatalog(query))
                .thenReturn(PageResponse.from(new PageImpl<>(items)));

        PageResponse<CatalogReportResponse> result = useCase.execute(query);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(catalogReportRepositoryPort).findCatalog(query);
    }

    @Test
    @DisplayName("should return empty catalog report")
    void should_get_empty_catalog_report() {
        CatalogReportQuery query = new CatalogReportQuery(
                null, null, null, null, null, null, 0, 20);
        List<CatalogReportResponse> items = List.of();

        when(catalogReportRepositoryPort.findCatalog(query))
                .thenReturn(PageResponse.from(new PageImpl<>(items)));

        PageResponse<CatalogReportResponse> result = useCase.execute(query);

        assertTrue(result.content().isEmpty());
        verify(catalogReportRepositoryPort).findCatalog(query);
    }
}
