package com.cashly.cashly_api.price_types.application.update;

import com.cashly.cashly_api.price_types.application.update.UpdatePriceTypeRequest;
import com.cashly.cashly_api.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.price_types.domain.PriceType;
import com.cashly.cashly_api.price_types.domain.PriceTypeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePriceTypeUseCase Unit Tests")
class UpdatePriceTypeUseCaseUnitTest {

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @InjectMocks
    private UpdatePriceTypeUseCase updatePriceTypeUseCase;

    @Test
    void should_UpdatePriceType_When_ValidRequestProvided() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType existing = PriceType.reconstitute(id, "Mayorista", "desc original", true, now, now);
        PriceType updated = PriceType.reconstitute(id, "Minorista", "desc nueva", false, now, now.plusSeconds(1));
        UpdatePriceTypeRequest request = new UpdatePriceTypeRequest("Minorista", "desc nueva", false);

        when(priceTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(priceTypeRepository.existsByNameAndIdNot("Minorista", id)).thenReturn(false);
        when(priceTypeRepository.save(any(PriceType.class))).thenReturn(updated);

        PriceTypeResponse response = updatePriceTypeUseCase.execute(id, request);

        assertNotNull(response);
        assertEquals(id.toString(), response.id());
        assertEquals("Minorista", response.name());
        assertEquals("desc nueva", response.description());
        assertFalse(response.active());

        verify(priceTypeRepository).findById(id);
        verify(priceTypeRepository).existsByNameAndIdNot("Minorista", id);
        verify(priceTypeRepository).save(any(PriceType.class));
    }

    @Test
    void should_ThrowPriceTypeNotFoundException_When_IdDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdatePriceTypeRequest request = new UpdatePriceTypeRequest("Mayorista", null, true);

        when(priceTypeRepository.findById(id)).thenReturn(Optional.empty());

        PriceTypeNotFoundException exception = assertThrows(
            PriceTypeNotFoundException.class,
            () -> updatePriceTypeUseCase.execute(id, request)
        );

        assertEquals("Price type not found with id: " + id, exception.getMessage());
        verify(priceTypeRepository).findById(id);
        verify(priceTypeRepository, never()).existsByNameAndIdNot(any(), any());
        verify(priceTypeRepository, never()).save(any());
    }

    @Test
    void should_ThrowDuplicatePriceTypeNameException_When_NameExistsForAnotherPriceType() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType existing = PriceType.reconstitute(id, "Mayorista", null, true, now, now);
        UpdatePriceTypeRequest request = new UpdatePriceTypeRequest("Minorista", null, true);

        when(priceTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(priceTypeRepository.existsByNameAndIdNot("Minorista", id)).thenReturn(true);

        DuplicatePriceTypeNameException exception = assertThrows(
            DuplicatePriceTypeNameException.class,
            () -> updatePriceTypeUseCase.execute(id, request)
        );

        assertEquals("Price type with name already exists: Minorista", exception.getMessage());
        verify(priceTypeRepository).findById(id);
        verify(priceTypeRepository).existsByNameAndIdNot("Minorista", id);
        verify(priceTypeRepository, never()).save(any());
    }

    @Test
    void should_AllowSameName_When_UpdatingSelf() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType existing = PriceType.reconstitute(id, "Mayorista", null, true, now, now);
        PriceType saved = PriceType.reconstitute(id, "Mayorista", "nueva desc", true, now, now.plusSeconds(1));
        UpdatePriceTypeRequest request = new UpdatePriceTypeRequest("Mayorista", "nueva desc", true);

        when(priceTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(priceTypeRepository.existsByNameAndIdNot("Mayorista", id)).thenReturn(false);
        when(priceTypeRepository.save(any(PriceType.class))).thenReturn(saved);

        assertDoesNotThrow(() -> updatePriceTypeUseCase.execute(id, request));
        verify(priceTypeRepository).save(any(PriceType.class));
    }
}
