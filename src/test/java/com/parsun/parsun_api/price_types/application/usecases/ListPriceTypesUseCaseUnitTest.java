package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPriceTypesUseCase Unit Tests")
class ListPriceTypesUseCaseUnitTest {

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @InjectMocks
    private ListPriceTypesUseCase listPriceTypesUseCase;

    @Test
    void should_ReturnAllPriceTypes_When_MultipleExist() {
        LocalDateTime now = LocalDateTime.now();
        List<PriceType> priceTypes = List.of(
            PriceType.reconstitute(UUID.randomUUID(), "Mayorista", "desc1", true, now, now),
            PriceType.reconstitute(UUID.randomUUID(), "Minorista", "desc2", true, now, now),
            PriceType.reconstitute(UUID.randomUUID(), "Especial", null, false, now, now)
        );

        when(priceTypeRepository.findAll()).thenReturn(priceTypes);

        List<PriceTypeResponse> responses = listPriceTypesUseCase.execute();

        assertEquals(3, responses.size());
        assertEquals("Mayorista", responses.get(0).name());
        assertEquals("Minorista", responses.get(1).name());
        assertEquals("Especial", responses.get(2).name());
        verify(priceTypeRepository).findAll();
    }

    @Test
    void should_ReturnEmptyList_When_NoneExist() {
        when(priceTypeRepository.findAll()).thenReturn(List.of());

        List<PriceTypeResponse> responses = listPriceTypesUseCase.execute();

        assertTrue(responses.isEmpty());
        verify(priceTypeRepository).findAll();
    }

    @Test
    void should_MapAllFields_When_PriceTypeHasAllData() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PriceType priceType = PriceType.reconstitute(id, "Mayorista", "Precio para mayoristas", true, now, now);

        when(priceTypeRepository.findAll()).thenReturn(List.of(priceType));

        List<PriceTypeResponse> responses = listPriceTypesUseCase.execute();

        PriceTypeResponse response = responses.get(0);
        assertEquals(id.toString(), response.id());
        assertEquals("Mayorista", response.name());
        assertEquals("Precio para mayoristas", response.description());
        assertTrue(response.active());
    }
}
