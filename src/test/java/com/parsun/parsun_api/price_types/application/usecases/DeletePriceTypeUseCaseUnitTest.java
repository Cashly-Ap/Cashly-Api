package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.exceptions.PriceTypeInUseException;
import com.parsun.parsun_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePriceTypeUseCase Unit Tests")
class DeletePriceTypeUseCaseUnitTest {

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @InjectMocks
    private DeletePriceTypeUseCase deletePriceTypeUseCase;

    @Test
    void should_DeletePriceType_When_ExistsAndNotInUse() {
        UUID id = UUID.randomUUID();

        when(priceTypeRepository.existsById(id)).thenReturn(true);
        when(priceTypeRepository.isInUseByProducts(id)).thenReturn(false);

        assertDoesNotThrow(() -> deletePriceTypeUseCase.execute(id));

        verify(priceTypeRepository).existsById(id);
        verify(priceTypeRepository).isInUseByProducts(id);
        verify(priceTypeRepository).delete(id);
    }

    @Test
    void should_ThrowPriceTypeNotFoundException_When_IdDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(priceTypeRepository.existsById(id)).thenReturn(false);

        PriceTypeNotFoundException exception = assertThrows(
            PriceTypeNotFoundException.class,
            () -> deletePriceTypeUseCase.execute(id)
        );

        assertEquals("Price type not found with id: " + id, exception.getMessage());
        verify(priceTypeRepository).existsById(id);
        verify(priceTypeRepository, never()).isInUseByProducts(any());
        verify(priceTypeRepository, never()).delete(any());
    }

    @Test
    void should_ThrowPriceTypeInUseException_When_PriceTypeIsUsedByProducts() {
        UUID id = UUID.randomUUID();

        when(priceTypeRepository.existsById(id)).thenReturn(true);
        when(priceTypeRepository.isInUseByProducts(id)).thenReturn(true);

        PriceTypeInUseException exception = assertThrows(
            PriceTypeInUseException.class,
            () -> deletePriceTypeUseCase.execute(id)
        );

        assertEquals("Price type is in use by products and cannot be deleted: " + id, exception.getMessage());
        verify(priceTypeRepository).existsById(id);
        verify(priceTypeRepository).isInUseByProducts(id);
        verify(priceTypeRepository, never()).delete(any());
    }
}
