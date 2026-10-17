package com.cashly.cashly_api.catalog.price_types.application.queries.use_case;

import com.cashly.cashly_api.catalog.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.catalog.price_types.domain.entity.PriceType;
import com.cashly.cashly_api.catalog.price_types.application.ports.out.PriceTypeUsageGateway;
import com.cashly.cashly_api.catalog.price_types.domain.repository.PriceTypeRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPriceTypesUseCase Tests")
class ListPriceTypesUseCaseTest {

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @Mock private PriceTypeUsageGateway usageGateway;

    @InjectMocks
    private ListPriceTypesUseCase useCase;

    private PriceType priceType1;
    private PriceType priceType2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        priceType1 = PriceType.reconstitute(UUID.randomUUID(), "Mayorista", "Wholesale pricing", true, now, now);
        priceType2 = PriceType.reconstitute(UUID.randomUUID(), "Minorista", "Retail pricing", true, now, now);
    }

    private static PageResponse<PriceType> pageOf(List<PriceType> items) {
        return new PageResponse<>(items, items.size(), 1, 10, 0, true, true, items.isEmpty());
    }

    @Test
    @DisplayName("should_ReturnList_When_PriceTypesExist")
    void should_ReturnList_When_PriceTypesExist() {
        when(priceTypeRepository.findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(pageOf(List.of(priceType1, priceType2)));
        when(usageGateway.findIdsInUseByProducts(any())).thenReturn(Set.of());

        PageResponse<PriceTypeResponse> result = useCase.execute(null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("Mayorista", result.content().get(0).name());
        assertEquals("Minorista", result.content().get(1).name());
        verify(priceTypeRepository).findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_ReturnEmptyList_When_NoPriceTypesFound")
    void should_ReturnEmptyList_When_NoPriceTypesFound() {
        when(priceTypeRepository.findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("name"), eq("asc")))
            .thenReturn(pageOf(List.of()));
        when(usageGateway.findIdsInUseByProducts(any())).thenReturn(Set.of());

        PageResponse<PriceTypeResponse> result = useCase.execute(null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(priceTypeRepository).findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_PassOrderByToRepository")
    void should_PassOrderByToRepository() {
        when(priceTypeRepository.findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("createdAt"), eq("desc")))
            .thenReturn(pageOf(List.of()));
        when(usageGateway.findIdsInUseByProducts(any())).thenReturn(Set.of());

        useCase.execute(null, null, 0, 10, "createdAt", "desc");

        verify(priceTypeRepository).findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("createdAt"), eq("desc"));
    }

    @Test
    @DisplayName("should_PassSearchAndActiveFiltersToRepository")
    void should_PassSearchAndActiveFiltersToRepository() {
        when(priceTypeRepository.findAllWithFilters(eq("Mayo"), eq(true), eq(1), eq(5), eq("name"), eq("asc")))
            .thenReturn(pageOf(List.of(priceType1)));
        when(usageGateway.findIdsInUseByProducts(any())).thenReturn(Set.of());

        useCase.execute("Mayo", true, 1, 5, "name", "asc");

        verify(priceTypeRepository).findAllWithFilters(eq("Mayo"), eq(true), eq(1), eq(5), eq("name"), eq("asc"));
    }
    @Test
    void shouldPreserveDeletabilityUsingOneUsageBatchForReturnedPage() {
        Set<UUID> ids = Set.of(priceType1.getId(), priceType2.getId());
        when(priceTypeRepository.findAllWithFilters(null, null, 0, 10, "name", "asc"))
                .thenReturn(pageOf(List.of(priceType1, priceType2)));
        when(usageGateway.findIdsInUseByProducts(ids)).thenReturn(Set.of(priceType1.getId()));
        var result = useCase.execute(null, null, 0, 10, "name", "asc");
        assertFalse(result.content().get(0).deletable());
        assertTrue(result.content().get(1).deletable());
        verify(usageGateway).findIdsInUseByProducts(ids);
        verifyNoMoreInteractions(usageGateway);
    }
}