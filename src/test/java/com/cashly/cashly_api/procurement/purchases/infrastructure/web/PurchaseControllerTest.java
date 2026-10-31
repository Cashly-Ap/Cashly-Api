package com.cashly.cashly_api.procurement.purchases.infrastructure.web;

import com.cashly.cashly_api.procurement.purchases.application.cancel.request.CancelPurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.create.request.CreatePurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.update.request.UpdatePurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseProductResponse;
import com.cashly.cashly_api.procurement.purchases.application.cancel.use_case.CancelPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.confirm.use_case.ConfirmPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.create.use_case.CreatePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.delete.use_case.DeletePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.products.use_case.GetProductsForPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.get.use_case.GetPurchaseByIdUseCase;
import com.cashly.cashly_api.procurement.purchases.application.list.use_case.ListPurchasesUseCase;
import com.cashly.cashly_api.procurement.purchases.application.update.use_case.UpdatePurchaseUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseController Unit Tests")
class PurchaseControllerTest {

    @Mock
    private CreatePurchaseUseCase createPurchaseUseCase;

    @Mock
    private GetPurchaseByIdUseCase getPurchaseByIdUseCase;

    @Mock
    private ListPurchasesUseCase listPurchasesUseCase;

    @Mock
    private UpdatePurchaseUseCase updatePurchaseUseCase;

    @Mock
    private ConfirmPurchaseUseCase confirmPurchaseUseCase;

    @Mock
    private CancelPurchaseUseCase cancelPurchaseUseCase;

    @Mock
    private DeletePurchaseUseCase deletePurchaseUseCase;

    @Mock
    private GetProductsForPurchaseUseCase getProductsForPurchaseUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PurchaseController purchaseController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("should create purchase and return 201")
    void shouldCreatePurchase() {
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());
        PurchaseDetailResponse response = buildDetailResponse();

        when(createPurchaseUseCase.execute(any(CreatePurchaseRequest.class),
                any(UUID.class))).thenReturn(response);

        CreatePurchaseRequest request = new CreatePurchaseRequest(
                UUID.randomUUID(), "Notes",
                List.of(new CreatePurchaseRequest.LineRequest(UUID.randomUUID(),
                        BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreatePurchaseRequest.AllocationRequest(
                                UUID.randomUUID(), BigDecimal.ONE)))));

        ResponseEntity<PurchaseDetailResponse> result = purchaseController
                .create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(response.id(), result.getBody().id());
        verify(createPurchaseUseCase).execute(any(CreatePurchaseRequest.class),
                any(UUID.class));
    }

    @Test
    @DisplayName("should get all purchases with filters and return 200")
    void shouldGetAllPurchases() {
        PurchaseListResponse purchase = new PurchaseListResponse(
                UUID.randomUUID(), "PUR-2026-00001", UUID.randomUUID(),
                "Supplier", "DRAFT", BigDecimal.TEN, 1, LocalDateTime.now());

        when(listPurchasesUseCase.execute(any(), any(), any(), any(), any(),
                any(), anyString(), anyString())).thenReturn(List.of(purchase));

        ResponseEntity<List<PurchaseListResponse>> result = purchaseController
                .getAll(null, null, null, null, null, null, "createdAt",
                        "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("PUR-2026-00001",
                result.getBody().get(0).purchaseNumber());
        verify(listPurchasesUseCase).execute(null, null, null, null, null, null,
                "createdAt", "desc");
    }

    @Test
    @DisplayName("should return empty list when no purchases exist")
    void shouldReturnEmptyListWhenNoPurchases() {
        when(listPurchasesUseCase.execute(any(), any(), any(), any(), any(),
                any(), anyString(), anyString())).thenReturn(List.of());

        ResponseEntity<List<PurchaseListResponse>> result = purchaseController
                .getAll(null, null, null, null, null, null, "createdAt",
                        "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("should get purchase by id and return 200")
    void shouldGetPurchaseById() {
        UUID id = UUID.randomUUID();
        PurchaseDetailResponse response = buildDetailResponse(id, "DRAFT");

        when(getPurchaseByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<PurchaseDetailResponse> result = purchaseController
                .getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getPurchaseByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should update purchase and return 200")
    void shouldUpdatePurchase() {
        UUID id = UUID.randomUUID();
        PurchaseDetailResponse response = buildDetailResponse();

        when(updatePurchaseUseCase.execute(eq(id),
                any(UpdatePurchaseRequest.class))).thenReturn(response);

        UpdatePurchaseRequest request = new UpdatePurchaseRequest(
                "Updated notes",
                List.of(new UpdatePurchaseRequest.LineRequest(UUID.randomUUID(),
                        BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new UpdatePurchaseRequest.AllocationRequest(
                                UUID.randomUUID(), BigDecimal.ONE)))));

        ResponseEntity<PurchaseDetailResponse> result = purchaseController
                .update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(updatePurchaseUseCase).execute(eq(id),
                any(UpdatePurchaseRequest.class));
    }

    @Test
    @DisplayName("should confirm purchase and return 200")
    void shouldConfirmPurchase() {
        UUID id = UUID.randomUUID();
        PurchaseDetailResponse response = buildDetailResponse(id, "CONFIRMED");

        when(confirmPurchaseUseCase.execute(id)).thenReturn(response);

        ResponseEntity<PurchaseDetailResponse> result = purchaseController
                .confirm(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("CONFIRMED", result.getBody().status());
        verify(confirmPurchaseUseCase).execute(id);
    }

    @Test
    @DisplayName("should cancel purchase and return 200")
    void shouldCancelPurchase() {
        UUID id = UUID.randomUUID();
        PurchaseDetailResponse response = buildDetailResponse(id, "CANCELLED");

        when(cancelPurchaseUseCase.execute(eq(id),
                any(CancelPurchaseRequest.class))).thenReturn(response);

        CancelPurchaseRequest request = new CancelPurchaseRequest(
                "Cancellation reason");

        ResponseEntity<PurchaseDetailResponse> result = purchaseController
                .cancel(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("CANCELLED", result.getBody().status());
        verify(cancelPurchaseUseCase).execute(eq(id),
                any(CancelPurchaseRequest.class));
    }

    @Test
    @DisplayName("should delete purchase and return 204")
    void shouldDeletePurchase() {
        UUID id = UUID.randomUUID();
        doNothing().when(deletePurchaseUseCase).execute(id);

        ResponseEntity<Void> result = purchaseController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deletePurchaseUseCase).execute(id);
    }

    @Test
    @DisplayName("should get products for purchase and return 200")
    void shouldGetProductsForPurchase() {
        PageResponse<PurchaseProductResponse> pageResponse = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(getProductsForPurchaseUseCase.execute(any(), any(), anyInt(),
                anyInt())).thenReturn(pageResponse);

        ResponseEntity<?> result = purchaseController.getProducts(null, null, 0,
                10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(getProductsForPurchaseUseCase).execute(null, null, 0, 10);
    }

    private PurchaseDetailResponse buildDetailResponse() {
        return buildDetailResponse(UUID.randomUUID(), "DRAFT");
    }

    private PurchaseDetailResponse buildDetailResponse(UUID id, String status) {
        return new PurchaseDetailResponse(id, "PUR-2026-00001",
                UUID.randomUUID(), "Supplier Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", status, null, BigDecimal.TEN,
                null, null, null, LocalDateTime.now(), LocalDateTime.now(),
                List.of());
    }
    @Test
    void shouldRejectUnknownActionFilter() throws Exception {
        org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(purchaseController).build()
                .perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/purchases")
                                .param("action", "UNKNOWN"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isBadRequest());
    }

    @Test
    void shouldPassActionFilterToListQuery() throws Exception {
        org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(purchaseController).build()
                .perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/purchases").param("action", "EDIT"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isOk());
        verify(listPurchasesUseCase).execute(null, null, null, null, null, null,
                "createdAt", "desc",
                com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction.EDIT);
    }

}
