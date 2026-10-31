package com.cashly.cashly_api.procurement.purchases.domain.entity;

import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Purchase Domain Entity Unit Tests")
class PurchaseUnitTest {

    private static final PurchaseNumber PURCHASE_NUMBER = PurchaseNumber
            .of(2026, 1);
    private static final UUID SUPPLIER_ID = UUID.randomUUID();
    private static final String SUPPLIER_NAME = "Proveedor Test";
    private static final String SUPPLIER_RIF = "J-12345678-9";
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CREATED_BY_NAME = "Usuario Test";

    private List<PurchaseLine> validLines;

    @BeforeEach
    void setUp() {
        validLines = List.of(PurchaseLine.create(UUID.randomUUID(),
                "Producto A", "PROD-001", new BigDecimal("2"),
                new BigDecimal("10.00"), List.of()));
    }

    // ==================== CREATE ====================

    @Test
    void should_CreatePurchase_When_ValidDataProvided() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME,
                "Notas", validLines);

        assertNotNull(purchase.getId());
        assertEquals(PURCHASE_NUMBER, purchase.getPurchaseNumber());
        assertEquals(SUPPLIER_ID, purchase.getSupplierId());
        assertEquals(SUPPLIER_NAME, purchase.getSupplierName());
        assertEquals(SUPPLIER_RIF, purchase.getSupplierRif());
        assertEquals(CREATED_BY, purchase.getCreatedBy());
        assertEquals(CREATED_BY_NAME, purchase.getCreatedByName());
        assertEquals(PurchaseStatus.DRAFT, purchase.getStatus());
        assertEquals("Notas", purchase.getNotes());
        assertEquals(new BigDecimal("20.00"), purchase.getSubtotal());
        assertNull(purchase.getConfirmedAt());
        assertNull(purchase.getCancelledAt());
        assertNull(purchase.getCancelledReason());
        assertNotNull(purchase.getCreatedAt());
        assertNotNull(purchase.getUpdatedAt());
        assertEquals(1, purchase.getLines().size());
    }

    @Test
    void should_AllowNullNotes_When_Creating() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);

        assertNull(purchase.getNotes());
    }

    @Test
    void should_CalculateSubtotal_When_MultipleLines() {
        List<PurchaseLine> lines = List.of(
                PurchaseLine.create(UUID.randomUUID(), "Producto A", "PROD-001",
                        new BigDecimal("2"), new BigDecimal("10.00"),
                        List.of()),
                PurchaseLine.create(UUID.randomUUID(), "Producto B", "PROD-002",
                        new BigDecimal("3"), new BigDecimal("5.00"),
                        List.of()));

        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                lines);

        assertEquals(new BigDecimal("35.00"), purchase.getSubtotal());
    }

    // ==================== NULL VALIDATION ====================

    @Test
    void should_ThrowException_When_PurchaseNumberIsNull() {
        assertThrows(NullPointerException.class,
                () -> Purchase.create(null, SUPPLIER_ID, SUPPLIER_NAME,
                        SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                        validLines));
    }

    @Test
    void should_ThrowException_When_SupplierIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> Purchase.create(PURCHASE_NUMBER, null, SUPPLIER_NAME,
                        SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                        validLines));
    }

    @Test
    void should_ThrowException_When_SupplierNameIsNull() {
        assertThrows(NullPointerException.class,
                () -> Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID, null,
                        SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                        validLines));
    }

    @Test
    void should_ThrowException_When_SupplierRifIsNull() {
        assertThrows(NullPointerException.class,
                () -> Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                        SUPPLIER_NAME, null, CREATED_BY, CREATED_BY_NAME, null,
                        validLines));
    }

    @Test
    void should_ThrowException_When_CreatedByIsNull() {
        assertThrows(NullPointerException.class,
                () -> Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                        SUPPLIER_NAME, SUPPLIER_RIF, null, CREATED_BY_NAME,
                        null, validLines));
    }

    @Test
    void should_ThrowException_When_CreatedByNameIsNull() {
        assertThrows(NullPointerException.class,
                () -> Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                        SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, null, null,
                        validLines));
    }

    @Test
    void should_ThrowException_When_LinesIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                        SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY,
                        CREATED_BY_NAME, null, null));

        assertEquals("Purchase must have at least one line",
                exception.getMessage());
    }

    @Test
    void should_ThrowException_When_LinesIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                        SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY,
                        CREATED_BY_NAME, null, List.of()));

        assertEquals("Purchase must have at least one line",
                exception.getMessage());
    }

    // ==================== UNMODIFIABLE LINES ====================

    @Test
    void should_ReturnUnmodifiableLines_When_GetLinesCalled() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);

        assertThrows(UnsupportedOperationException.class,
                () -> purchase.getLines().add(validLines.get(0)));
    }

    // ==================== CONFIRM ====================

    @Test
    void should_TransitionToConfirmed_When_ConfirmedFromDraft()
            throws InterruptedException {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);
        LocalDateTime beforeConfirm = purchase.getUpdatedAt();

        Thread.sleep(10);
        purchase.confirm();

        assertEquals(PurchaseStatus.CONFIRMED, purchase.getStatus());
        assertNotNull(purchase.getConfirmedAt());
        assertTrue(purchase.getUpdatedAt().isAfter(beforeConfirm));
    }

    @Test
    void should_ThrowException_When_ConfirmingNonDraftPurchase() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);
        purchase.confirm();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, purchase::confirm);

        assertEquals("Only DRAFT purchases can be confirmed",
                exception.getMessage());
    }

    // ==================== CANCEL ====================

    @Test
    void should_CancelPurchase_When_StatusIsDraft()
            throws InterruptedException {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);
        LocalDateTime beforeCancel = purchase.getUpdatedAt();

        Thread.sleep(10);
        purchase.cancel("Proveedor no confirmó el pedido");

        assertEquals(PurchaseStatus.CANCELLED, purchase.getStatus());
        assertNotNull(purchase.getCancelledAt());
        assertEquals("Proveedor no confirmó el pedido",
                purchase.getCancelledReason());
        assertTrue(purchase.getUpdatedAt().isAfter(beforeCancel));
    }

    @Test
    void should_CancelPurchase_When_StatusIsConfirmed() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);
        purchase.confirm();

        purchase.cancel("Error en el pedido");

        assertEquals(PurchaseStatus.CANCELLED, purchase.getStatus());
        assertNotNull(purchase.getCancelledAt());
        assertEquals("Error en el pedido", purchase.getCancelledReason());
    }

    @Test
    void should_ThrowException_When_CancellingAlreadyCancelledPurchase() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);
        purchase.cancel("Primera cancelación");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> purchase.cancel("Segunda cancelación"));

        assertEquals(
                "Only DRAFT or CONFIRMED purchases can be cancelled, current: CANCELLED",
                exception.getMessage());
    }

    @Test
    void should_ThrowException_When_CancelReasonIsNull() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);

        assertThrows(NullPointerException.class, () -> purchase.cancel(null));
    }

    @Test
    void should_ThrowException_When_CancelReasonIsBlank() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> purchase.cancel("   "));

        assertEquals("Cancellation reason cannot be blank",
                exception.getMessage());
    }

    // ==================== UPDATE ====================

    @Test
    void should_UpdatePurchase_When_StatusIsDraft()
            throws InterruptedException {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME,
                "Notas originales", validLines);
        LocalDateTime beforeUpdate = purchase.getUpdatedAt();

        List<PurchaseLine> updatedLines = List.of(PurchaseLine.create(
                UUID.randomUUID(), "Producto B", "PROD-002",
                new BigDecimal("5"), new BigDecimal("20.00"), List.of()));

        Thread.sleep(10);
        purchase.update("Notas actualizadas", updatedLines);

        assertEquals("Notas actualizadas", purchase.getNotes());
        assertEquals(1, purchase.getLines().size());
        assertEquals(new BigDecimal("100.00"), purchase.getSubtotal());
        assertTrue(purchase.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void should_ThrowException_When_UpdatingNonDraftPurchase() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);
        purchase.confirm();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> purchase.update("nuevas notas", validLines));

        assertEquals("Only DRAFT purchases can be updated",
                exception.getMessage());
    }

    @Test
    void should_ThrowException_When_UpdatingWithEmptyLines() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);

        assertThrows(IllegalArgumentException.class,
                () -> purchase.update("nuevas notas", List.of()));
    }

    // ==================== DELETE ====================

    @Test
    void should_AllowDelete_When_StatusIsDraft() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);

        assertDoesNotThrow(purchase::delete);
    }

    @Test
    void should_ThrowException_When_DeletingNonDraftPurchase() {
        Purchase purchase = Purchase.create(PURCHASE_NUMBER, SUPPLIER_ID,
                SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY, CREATED_BY_NAME, null,
                validLines);
        purchase.confirm();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, purchase::delete);

        assertEquals("Only DRAFT purchases can be deleted",
                exception.getMessage());
    }

    // ==================== RECONSTITUTE ====================

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        PurchaseId id = PurchaseId.generate();
        LocalDateTime now = LocalDateTime.now();

        Purchase purchase = Purchase.reconstitute(id, PURCHASE_NUMBER,
                SUPPLIER_ID, SUPPLIER_NAME, SUPPLIER_RIF, CREATED_BY,
                CREATED_BY_NAME, PurchaseStatus.CONFIRMED, "notas",
                BigDecimal.ZERO, now, null, null, validLines, now, now);

        assertEquals(id, purchase.getId());
        assertEquals(PurchaseStatus.CONFIRMED, purchase.getStatus());
        assertEquals(now, purchase.getConfirmedAt());
        assertEquals(now, purchase.getCreatedAt());
    }
}
