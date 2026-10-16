package com.cashly.cashly_api.price_types.application.create;

import com.cashly.cashly_api.price_types.application.create.CreatePriceTypeRequest;
import com.cashly.cashly_api.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.cashly.cashly_api.price_types.domain.PriceType;
import com.cashly.cashly_api.price_types.domain.PriceTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePriceTypeUseCase Unit Tests")
class CreatePriceTypeUseCaseUnitTest {

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @InjectMocks
    private CreatePriceTypeUseCase createPriceTypeUseCase;

    @Test
    void should_CreatePriceType_When_ValidRequestProvided() {
        CreatePriceTypeRequest request = new CreatePriceTypeRequest("Mayorista", "Precio para mayoristas");
        UUID savedId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType saved = PriceType.reconstitute(savedId, "Mayorista", "Precio para mayoristas", true, now, now);

        when(priceTypeRepository.existsByName("Mayorista")).thenReturn(false);
        when(priceTypeRepository.save(any(PriceType.class))).thenReturn(saved);

        PriceTypeResponse response = createPriceTypeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(savedId.toString(), response.id());
        assertEquals("Mayorista", response.name());
        assertEquals("Precio para mayoristas", response.description());
        assertTrue(response.active());

        verify(priceTypeRepository).existsByName("Mayorista");
        verify(priceTypeRepository).save(any(PriceType.class));
    }

    @Test
    void should_ThrowException_When_NameAlreadyExists() {
        CreatePriceTypeRequest request = new CreatePriceTypeRequest("Mayorista", null);

        when(priceTypeRepository.existsByName("Mayorista")).thenReturn(true);

        DuplicatePriceTypeNameException exception = assertThrows(
            DuplicatePriceTypeNameException.class,
            () -> createPriceTypeUseCase.execute(request)
        );

        assertEquals("Price type with name already exists: Mayorista", exception.getMessage());
        verify(priceTypeRepository).existsByName("Mayorista");
        verify(priceTypeRepository, never()).save(any());
    }

    @Test
    void should_CreateWithNullDescription_When_DescriptionNotProvided() {
        CreatePriceTypeRequest request = new CreatePriceTypeRequest("Minorista", null);
        LocalDateTime now = LocalDateTime.now();
        PriceType saved = PriceType.reconstitute(UUID.randomUUID(), "Minorista", null, true, now, now);

        when(priceTypeRepository.existsByName("Minorista")).thenReturn(false);
        when(priceTypeRepository.save(any(PriceType.class))).thenReturn(saved);

        PriceTypeResponse response = createPriceTypeUseCase.execute(request);

        assertNull(response.description());
    }

    @Test
    void should_SaveExactlyOnce_When_ValidRequest() {
        CreatePriceTypeRequest request = new CreatePriceTypeRequest("Especial", "desc");
        LocalDateTime now = LocalDateTime.now();
        PriceType saved = PriceType.reconstitute(UUID.randomUUID(), "Especial", "desc", true, now, now);

        when(priceTypeRepository.existsByName("Especial")).thenReturn(false);
        when(priceTypeRepository.save(any(PriceType.class))).thenReturn(saved);

        createPriceTypeUseCase.execute(request);

        verify(priceTypeRepository, times(1)).save(any(PriceType.class));
    }
}
