package com.cashly.cashly_api.price_types.application.usecases;

import com.cashly.cashly_api.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.price_types.domain.entities.PriceType;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
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
        when(priceTypeRepository.findIdsInUseByProducts(any())).thenReturn(Set.of());

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
        when(priceTypeRepository.findIdsInUseByProducts(any())).thenReturn(Set.of());

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
        when(priceTypeRepository.findIdsInUseByProducts(any())).thenReturn(Set.of());

        useCase.execute(null, null, 0, 10, "createdAt", "desc");

        verify(priceTypeRepository).findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("createdAt"), eq("desc"));
    }

    @Test
    @DisplayName("should_PassSearchAndActiveFiltersToRepository")
    void should_PassSearchAndActiveFiltersToRepository() {
        when(priceTypeRepository.findAllWithFilters(eq("Mayo"), eq(true), eq(1), eq(5), eq("name"), eq("asc")))
            .thenReturn(pageOf(List.of(priceType1)));
        when(priceTypeRepository.findIdsInUseByProducts(any())).thenReturn(Set.of());

        useCase.execute("Mayo", true, 1, 5, "name", "asc");

        verify(priceTypeRepository).findAllWithFilters(eq("Mayo"), eq(true), eq(1), eq(5), eq("name"), eq("asc"));
    }
}
