package com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.use_case;

import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.request.TransferStockRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferStockUseCase Tests")
class TransferStockUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseStockRepository warehouseStockRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductRepository productRepository;

    private TransferStockUseCase useCase;

    private final UUID sourceId = UUID.randomUUID();
    private final UUID destId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new TransferStockUseCase(warehouseRepository,
                warehouseStockRepository, stockMovementRepository,
                new LocalStockCatalogGatewayAdapter(
                        new RepositoryStockCatalogQuery(productRepository)), org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
    }

    @Test
    @DisplayName("deducts from source, adds to destination, creates TRANSFER_OUT and TRANSFER_IN movements")
    void shouldTransferStockBetweenWarehouses() {
        Warehouse source = buildActiveWarehouse(sourceId);
        Warehouse dest = buildActiveWarehouse(destId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");

        WarehouseStock sourceStock = WarehouseStock.create(sourceId, productId);
        sourceStock.addStock(100);
        WarehouseStock destStock = WarehouseStock.create(destId, productId);

        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 20, "Transfer");

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.of(source));
        when(warehouseRepository.findById(destId))
                .thenReturn(Optional.of(dest));
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(warehouseStockRepository.existsByWarehouseIdAndProductId(sourceId,
                productId)).thenReturn(true);
        when(warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(any(),
                        eq(productId)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0).equals(sourceId)
                                ? sourceStock
                                : destStock);
        when(warehouseStockRepository.save(any(WarehouseStock.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(inv -> {
                    StockMovement m = inv.getArgument(0);
                    return new StockMovement(UUID.randomUUID(),
                            m.getWarehouseId(), m.getProductId(),
                            m.getMovementType(), m.getQuantity(),
                            m.getPreviousStock(), m.getNewStock(),
                            m.getOriginMovementId(), m.getTransferBatchId(),
                            m.getReferenceType(), m.getReferenceId(),
                            m.getReferenceNumber(), m.getNotes(),
                            m.getCreatedAt());
                });

        List<StockMovementResponse> response = useCase.execute(sourceId,
                request);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(MovementType.TRANSFER_OUT, response.get(0).movementType());
        assertEquals(MovementType.TRANSFER_IN, response.get(1).movementType());
        assertEquals(20, response.get(0).quantity());
        assertEquals(20, response.get(1).quantity());
        assertEquals(80, response.get(0).newStock());
        assertEquals(20, response.get(1).newStock());
        verify(stockMovementRepository, times(2))
                .save(any(StockMovement.class));
        verify(warehouseStockRepository)
                .existsByWarehouseIdAndProductId(sourceId, productId);
        UUID firstLock = sourceId.compareTo(destId) < 0 ? sourceId : destId;
        UUID secondLock = sourceId.compareTo(destId) < 0 ? destId : sourceId;
        InOrder lockOrder = inOrder(warehouseStockRepository);
        lockOrder.verify(warehouseStockRepository)
                .findOrCreateByWarehouseIdAndProductIdForUpdate(firstLock,
                        productId);
        lockOrder.verify(warehouseStockRepository)
                .findOrCreateByWarehouseIdAndProductIdForUpdate(secondLock,
                        productId);
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when source not found")
    void shouldThrowWhenSourceNotFound() {
        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 10, null);

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(sourceId, request));
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when destination not found")
    void shouldThrowWhenDestinationNotFound() {
        Warehouse source = buildActiveWarehouse(sourceId);
        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 10, null);

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.of(source));
        when(warehouseRepository.findById(destId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(sourceId, request));
    }

    @Test
    @DisplayName("throws WarehouseNotActiveException when source inactive")
    void shouldThrowWhenSourceNotActive() {
        Warehouse source = buildInactiveWarehouse(sourceId);
        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 10, null);

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.of(source));

        assertThrows(WarehouseNotActiveException.class,
                () -> useCase.execute(sourceId, request));
    }

    @Test
    @DisplayName("throws WarehouseNotActiveException when destination inactive")
    void shouldThrowWhenDestinationNotActive() {
        Warehouse source = buildActiveWarehouse(sourceId);
        Warehouse dest = buildInactiveWarehouse(destId);
        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 10, null);

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.of(source));
        when(warehouseRepository.findById(destId))
                .thenReturn(Optional.of(dest));

        assertThrows(WarehouseNotActiveException.class,
                () -> useCase.execute(sourceId, request));
    }

    @Test
    @DisplayName("throws ProductNotFoundException when product not found")
    void shouldThrowWhenProductNotFound() {
        Warehouse source = buildActiveWarehouse(sourceId);
        Warehouse dest = buildActiveWarehouse(destId);
        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 10, null);

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.of(source));
        when(warehouseRepository.findById(destId))
                .thenReturn(Optional.of(dest));
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> useCase.execute(sourceId, request));
    }

    @Test
    @DisplayName("throws ProductNotInWarehouseException when product not in source")
    void shouldThrowWhenProductNotInSource() {
        Warehouse source = buildActiveWarehouse(sourceId);
        Warehouse dest = buildActiveWarehouse(destId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 10, null);

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.of(source));
        when(warehouseRepository.findById(destId))
                .thenReturn(Optional.of(dest));
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(warehouseStockRepository.existsByWarehouseIdAndProductId(sourceId,
                productId)).thenReturn(false);

        assertThrows(ProductNotInWarehouseException.class,
                () -> useCase.execute(sourceId, request));
    }

    @Test
    @DisplayName("throws InsufficientStockException when not enough stock in source")
    void shouldThrowWhenInsufficientStock() {
        Warehouse source = buildActiveWarehouse(sourceId);
        Warehouse dest = buildActiveWarehouse(destId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");

        WarehouseStock sourceStock = WarehouseStock.create(sourceId, productId);
        sourceStock.addStock(5);

        TransferStockRequest request = new TransferStockRequest(productId,
                destId, 20, null);

        when(warehouseRepository.findById(sourceId))
                .thenReturn(Optional.of(source));
        when(warehouseRepository.findById(destId))
                .thenReturn(Optional.of(dest));
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(warehouseStockRepository.existsByWarehouseIdAndProductId(sourceId,
                productId)).thenReturn(true);
        when(warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(any(),
                        eq(productId)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0).equals(sourceId)
                                ? sourceStock
                                : WarehouseStock.create(destId, productId));

        assertThrows(InsufficientStockException.class,
                () -> useCase.execute(sourceId, request));
    }

    private Warehouse buildActiveWarehouse(UUID id) {
        return Warehouse.reconstitute(id, "WH-001", "Main", "Addr", "State",
                "Mun", "10001", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Warehouse buildInactiveWarehouse(UUID id) {
        return Warehouse.reconstitute(id, "WH-001", "Main", "Addr", "State",
                "Mun", "10001", false, LocalDateTime.now(),
                LocalDateTime.now());
    }

    private Product buildProduct(UUID id, String code, String name) {
        return Product.reconstitute(id, code, name, "Desc", null, false, true,
                null, BigDecimal.ZERO, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
