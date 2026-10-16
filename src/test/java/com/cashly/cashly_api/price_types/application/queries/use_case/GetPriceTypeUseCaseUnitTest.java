package com.cashly.cashly_api.price_types.application.queries.use_case;

import com.cashly.cashly_api.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.price_types.domain.entity.PriceType;
import com.cashly.cashly_api.price_types.domain.repository.PriceTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPriceTypeUseCase Unit Tests")
class GetPriceTypeUseCaseUnitTest {

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @InjectMocks
    private GetPriceTypeUseCase getPriceTypeUseCase;

    @Test
    void should_ReturnPriceType_When_IdExists() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType priceType = PriceType.reconstitute(id, "Mayorista", "Precio mayorista", true, now, now);

        when(priceTypeRepository.findById(id)).thenReturn(Optional.of(priceType));

        PriceTypeResponse response = getPriceTypeUseCase.execute(id);

        assertNotNull(response);
        assertEquals(id.toString(), response.id());
        assertEquals("Mayorista", response.name());
        assertEquals("Precio mayorista", response.description());
        assertTrue(response.active());
        verify(priceTypeRepository).findById(id);
    }

    @Test
    void should_ThrowException_When_IdNotFound() {
        UUID id = UUID.randomUUID();

        when(priceTypeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PriceTypeNotFoundException.class, () -> getPriceTypeUseCase.execute(id));
        verify(priceTypeRepository).findById(id);
    }

    @Test
    void should_ReturnInactivePriceType_When_IdExistsAndInactive() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType priceType = PriceType.reconstitute(id, "Especial", null, false, now, now);

        when(priceTypeRepository.findById(id)).thenReturn(Optional.of(priceType));

        PriceTypeResponse response = getPriceTypeUseCase.execute(id);

        assertFalse(response.active());
        assertNull(response.description());
    }
}
