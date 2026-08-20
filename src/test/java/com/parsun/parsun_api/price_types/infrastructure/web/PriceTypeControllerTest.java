package com.parsun.parsun_api.price_types.infrastructure.web;

import com.parsun.parsun_api.price_types.application.dto.request.CreatePriceTypeRequest;
import com.parsun.parsun_api.price_types.application.dto.request.UpdatePriceTypeRequest;
import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.application.usecases.CreatePriceTypeUseCase;
import com.parsun.parsun_api.price_types.application.usecases.DeletePriceTypeUseCase;
import com.parsun.parsun_api.price_types.application.usecases.GetPriceTypeUseCase;
import com.parsun.parsun_api.price_types.application.usecases.ListPriceTypesUseCase;
import com.parsun.parsun_api.price_types.application.usecases.UpdatePriceTypeUseCase;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriceTypeController Unit Tests")
class PriceTypeControllerTest {

    @Mock
    private CreatePriceTypeUseCase createPriceTypeUseCase;

    @Mock
    private UpdatePriceTypeUseCase updatePriceTypeUseCase;

    @Mock
    private DeletePriceTypeUseCase deletePriceTypeUseCase;

    @Mock
    private GetPriceTypeUseCase getPriceTypeUseCase;

    @Mock
    private ListPriceTypesUseCase listPriceTypesUseCase;

    @InjectMocks
    private PriceTypeController priceTypeController;

    @Test
    @DisplayName("should create price type and return 201")
    void should_create_price_type_and_return_201() {
        String id = UUID.randomUUID().toString();
        PriceTypeResponse response = new PriceTypeResponse(
                id, "Retail", "Retail price", true, false,
                LocalDateTime.now(), LocalDateTime.now());

        when(createPriceTypeUseCase.execute(any(CreatePriceTypeRequest.class))).thenReturn(response);

        CreatePriceTypeRequest request = new CreatePriceTypeRequest("Retail", "Retail price");

        ResponseEntity<PriceTypeResponse> result = priceTypeController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(id, result.getBody().id());
        assertEquals("Retail", result.getBody().name());
        verify(createPriceTypeUseCase).execute(any(CreatePriceTypeRequest.class));
    }

    @Test
    @DisplayName("should update price type and return updated response")
    void should_update_price_type_and_return_updated_response() {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();
        PriceTypeResponse response = new PriceTypeResponse(
                id, "Wholesale", "Wholesale price", true, false,
                LocalDateTime.now(), LocalDateTime.now());

        when(updatePriceTypeUseCase.execute(eq(uuid), any(UpdatePriceTypeRequest.class))).thenReturn(response);

        UpdatePriceTypeRequest request = new UpdatePriceTypeRequest("Wholesale", "Wholesale price", true);

        ResponseEntity<PriceTypeResponse> result = priceTypeController.update(uuid, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(id, result.getBody().id());
        assertEquals("Wholesale", result.getBody().name());
        verify(updatePriceTypeUseCase).execute(eq(uuid), any(UpdatePriceTypeRequest.class));
    }

    @Test
    @DisplayName("should delete price type")
    void should_delete_price_type() {
        UUID id = UUID.randomUUID();
        doNothing().when(deletePriceTypeUseCase).execute(id);

        ResponseEntity<Void> result = priceTypeController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(deletePriceTypeUseCase).execute(id);
    }

    @Test
    @DisplayName("should get price type by id")
    void should_get_price_type_by_id() {
        String id = UUID.randomUUID().toString();
        PriceTypeResponse response = new PriceTypeResponse(
                id, "Retail", "Retail price", true, false,
                LocalDateTime.now(), LocalDateTime.now());

        when(getPriceTypeUseCase.execute(UUID.fromString(id))).thenReturn(response);

        ResponseEntity<PriceTypeResponse> result = priceTypeController.getById(UUID.fromString(id));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(id, result.getBody().id());
        verify(getPriceTypeUseCase).execute(UUID.fromString(id));
    }

    @Test
    @DisplayName("should list all price types")
    void should_list_all_price_types() {
        PriceTypeResponse pt1 = new PriceTypeResponse(
                UUID.randomUUID().toString(), "Retail", null, true, false,
                LocalDateTime.now(), LocalDateTime.now());
        PriceTypeResponse pt2 = new PriceTypeResponse(
                UUID.randomUUID().toString(), "Wholesale", null, true, false,
                LocalDateTime.now(), LocalDateTime.now());
        PageResponse<PriceTypeResponse> page = new PageResponse<>(
                List.of(pt1, pt2), 2, 1, 10, 0, true, true, false);

        when(listPriceTypesUseCase.execute(isNull(), isNull(), eq(0), eq(10), eq("createdAt"), eq("desc")))
                .thenReturn(page);

        ResponseEntity<PageResponse<PriceTypeResponse>> result =
                priceTypeController.listAll(null, null, 0, 10, "createdAt", "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().content().size());
        verify(listPriceTypesUseCase).execute(isNull(), isNull(), eq(0), eq(10), eq("createdAt"), eq("desc"));
    }

    @Test
    @DisplayName("should return empty list when no price types")
    void should_return_empty_list_when_no_price_types() {
        PageResponse<PriceTypeResponse> page = new PageResponse<>(
                List.of(), 0, 0, 10, 0, true, true, true);

        when(listPriceTypesUseCase.execute(isNull(), isNull(), eq(0), eq(10), eq("createdAt"), eq("desc")))
                .thenReturn(page);

        ResponseEntity<PageResponse<PriceTypeResponse>> result =
                priceTypeController.listAll(null, null, 0, 10, "createdAt", "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().content().isEmpty());
        verify(listPriceTypesUseCase).execute(isNull(), isNull(), eq(0), eq(10), eq("createdAt"), eq("desc"));
    }
}
