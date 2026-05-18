package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    @DisplayName("should_ReturnList_When_PriceTypesExist")
    void should_ReturnList_When_PriceTypesExist() {
        when(priceTypeRepository.findAll(eq("name"), eq("asc")))
            .thenReturn(List.of(priceType1, priceType2));

        List<PriceTypeResponse> result = useCase.execute("name", "asc");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Mayorista", result.get(0).name());
        assertEquals("Minorista", result.get(1).name());
        verify(priceTypeRepository).findAll(eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_ReturnEmptyList_When_NoPriceTypesFound")
    void should_ReturnEmptyList_When_NoPriceTypesFound() {
        when(priceTypeRepository.findAll(eq("name"), eq("asc")))
            .thenReturn(List.of());

        List<PriceTypeResponse> result = useCase.execute("name", "asc");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(priceTypeRepository).findAll(eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should_PassOrderByToRepository")
    void should_PassOrderByToRepository() {
        when(priceTypeRepository.findAll(eq("createdAt"), eq("desc")))
            .thenReturn(List.of());

        useCase.execute("createdAt", "desc");

        verify(priceTypeRepository).findAll(eq("createdAt"), eq("desc"));
    }
}
