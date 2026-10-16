package com.cashly.cashly_api.purchases.application.cancel;

import com.cashly.cashly_api.purchases.application.cancel.CancelPurchaseRequest;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.domain.Purchase;
import com.cashly.cashly_api.purchases.domain.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.purchases.domain.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseLineId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.ReferenceType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import com.cashly.cashly_api.purchases.infrastructure.integration.WarehouseInventoryAdapter;
import com.cashly.cashly_api.warehouses.stock.application.purchases.PurchaseInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPurchaseUseCase Unit Tests")
class CancelPurchaseUseCaseUnitTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private CancelPurchaseUseCase cancelPurchaseUseCase;

    @BeforeEach
    void setUp() {
        cancelPurchaseUseCase = new CancelPurchaseUseCase(purchaseRepository,
                new WarehouseInventoryAdapter(new PurchaseInventoryService(warehouseStockRepository, stockMovementRepository)), new com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler(new com.cashly.cashly_api.purchases.infrastructure.integration.LocalPurchaseWarehouseGatewayAdapter(new com.cashly.cashly_api.warehouses.stock.application.queries.RepositoryPurchaseWarehouseQuery(warehouseRepository, mock(WarehouseStockRepository.class)))));
    }

    @Test
    void should_CancelPurchase_When_PurchaseExistsAndIsDraft() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.DRAFT);
        CancelPurchaseRequest request = new CancelPurchaseRequest("Cancelación por parte del proveedor");

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseDetailResponse response = cancelPurchaseUseCase.execute(id, request);

        assertNotNull(response);
        assertEquals("CANCELLED", response.status());
        verify(purchaseRepository).findById(PurchaseId.of(id));
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void should_CancelPurchase_AndRevertStock_When_PurchaseWasConfirmed() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id, PurchaseStatus.CONFIRMED, warehouseId, productId);
        CancelPurchaseRequest request = new CancelPurchaseRequest("Error en la compra");

        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(10);

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        cancelPurchaseUseCase.execute(id, request);

        assertEquals(0, stock.getQuantity());
        verify(warehouseStockRepository).save(argThat(savedStock ->
                savedStock.getQuantity() == 0));
        verify(stockMovementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.CANCEL_ENTRY &&
                movement.getReferenceType() == ReferenceType.PURCHASE &&
                movement.getQuantity() == 10 &&
                movement.getPreviousStock() == 10 && movement.getNewStock() == 0 &&
                movement.getReferenceId().equals(id) &&
                movement.getReferenceNumber().equals(purchase.getPurchaseNumber().getValue()) &&
                movement.getNotes().equals("Compra cancelada")
        ));
    }

    @Test
    void should_NotRevertStock_When_PurchaseWasDraft() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id, PurchaseStatus.DRAFT, warehouseId, productId);
        CancelPurchaseRequest request = new CancelPurchaseRequest("Razón");

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        cancelPurchaseUseCase.execute(id, request);

        verify(purchaseRepository).save(any(Purchase.class));
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductIdForUpdate(any(), any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void should_ThrowPurchaseNotFoundException_When_PurchaseDoesNotExist() {
        UUID id = UUID.randomUUID();
        CancelPurchaseRequest request = new CancelPurchaseRequest("Razón");

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.empty());

        PurchaseNotFoundException exception = assertThrows(
                PurchaseNotFoundException.class,
                () -> cancelPurchaseUseCase.execute(id, request)
        );

        assertEquals("Purchase not found with id: " + id, exception.getMessage());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_PurchaseAlreadyCancelled() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.CANCELLED);
        CancelPurchaseRequest request = new CancelPurchaseRequest("Razón");

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));

        assertThrows(IllegalStateException.class, () -> cancelPurchaseUseCase.execute(id, request));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_SaveExactlyOnce_When_CancellationSucceeds() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.DRAFT);
        CancelPurchaseRequest request = new CancelPurchaseRequest("Razón");

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        cancelPurchaseUseCase.execute(id, request);

        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    @Test
    void shouldRejectCancellationWhenReceivedStockIsMissing() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id, PurchaseStatus.CONFIRMED, warehouseId, productId);
        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,
                () -> cancelPurchaseUseCase.execute(id, new CancelPurchaseRequest("Missing stock")));
        verify(purchaseRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void shouldRejectReversalWhenPurchaseStockHasAlreadyBeenConsumed() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id, PurchaseStatus.CONFIRMED, warehouseId, productId);
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(5);
        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId)).thenReturn(Optional.of(stock));
        assertThrows(IllegalStateException.class,
                () -> cancelPurchaseUseCase.execute(id, new CancelPurchaseRequest("Consumed stock")));
        assertEquals(5, stock.getQuantity());
        verify(purchaseRepository, never()).save(any());
        verify(warehouseStockRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    private Purchase buildPurchase(UUID id, PurchaseStatus status) {
        PurchaseLine line = PurchaseLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Purchase.reconstitute(
                PurchaseId.of(id),
                PurchaseNumber.of(2026, 1),
                UUID.randomUUID(),
                "Supplier Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                status,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Purchase buildPurchaseWithAllocation(UUID id, PurchaseStatus status,
                                                  UUID warehouseId, UUID productId) {
        PurchaseLineAllocation allocation = PurchaseLineAllocation.create(warehouseId, BigDecimal.TEN);
        PurchaseLine line = PurchaseLine.reconstitute(
                PurchaseLineId.of(UUID.randomUUID()),
                productId,
                "Test Product",
                "PROD-001",
                BigDecimal.TEN,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                List.of(allocation)
        );
        return Purchase.reconstitute(
                PurchaseId.of(id),
                PurchaseNumber.of(2026, 1),
                UUID.randomUUID(),
                "Supplier Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                status,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
