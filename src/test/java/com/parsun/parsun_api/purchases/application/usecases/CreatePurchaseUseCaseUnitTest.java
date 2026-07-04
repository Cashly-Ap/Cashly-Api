package com.parsun.parsun_api.purchases.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.purchases.application.dto.request.CreatePurchaseRequest;
import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.parsun.parsun_api.purchases.domain.entities.Purchase;
import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.suppliers.domain.entities.Supplier;
import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.suppliers.domain.valueobjects.*;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
@DisplayName("CreatePurchaseUseCase Unit Tests")
class CreatePurchaseUseCaseUnitTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private CreatePurchaseUseCase createPurchaseUseCase;

    @BeforeEach
    void setUp() {
        createPurchaseUseCase = new CreatePurchaseUseCase(
                purchaseRepository, supplierRepository, productRepository,
                userRepository, warehouseRepository);
    }

    @Test
    void should_CreatePurchase_When_ValidRequestProvided() {
        UUID supplierId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Supplier supplier = buildSupplier(supplierId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreatePurchaseRequest.AllocationRequest allocRequest =
                new CreatePurchaseRequest.AllocationRequest(warehouseId, BigDecimal.TEN);
        CreatePurchaseRequest.LineRequest lineRequest =
                new CreatePurchaseRequest.LineRequest(productId, BigDecimal.TEN, BigDecimal.valueOf(10),
                        List.of(allocRequest));
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                supplierId, "Notas de compra", List.of(lineRequest));

        when(supplierRepository.findById(new SupplierId(supplierId))).thenReturn(Optional.of(supplier));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseDetailResponse response = createPurchaseUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void should_ThrowException_When_SupplierNotFound() {
        UUID supplierId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        CreatePurchaseRequest request = new CreatePurchaseRequest(
                supplierId, null, List.of());

        when(supplierRepository.findById(new SupplierId(supplierId))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createPurchaseUseCase.execute(request, authenticatedUserId)
        );

        assertEquals("Supplier not found: " + supplierId, exception.getMessage());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_UserNotFound() {
        UUID supplierId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Supplier supplier = buildSupplier(supplierId);
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                supplierId, null, List.of());

        when(supplierRepository.findById(new SupplierId(supplierId))).thenReturn(Optional.of(supplier));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createPurchaseUseCase.execute(request, authenticatedUserId)
        );

        assertEquals("User not found: " + authenticatedUserId, exception.getMessage());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID supplierId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Supplier supplier = buildSupplier(supplierId);
        User user = buildUser(authenticatedUserId);

        CreatePurchaseRequest.LineRequest lineRequest =
                new CreatePurchaseRequest.LineRequest(productId, BigDecimal.ONE, BigDecimal.TEN, List.of());
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                supplierId, null, List.of(lineRequest));

        when(supplierRepository.findById(new SupplierId(supplierId))).thenReturn(Optional.of(supplier));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createPurchaseUseCase.execute(request, authenticatedUserId)
        );

        assertEquals("Product not found: " + productId, exception.getMessage());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_SavePurchaseExactlyOnce_When_CreationSucceeds() {
        UUID supplierId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Supplier supplier = buildSupplier(supplierId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreatePurchaseRequest.AllocationRequest allocRequest =
                new CreatePurchaseRequest.AllocationRequest(warehouseId, BigDecimal.TEN);
        CreatePurchaseRequest.LineRequest lineRequest =
                new CreatePurchaseRequest.LineRequest(productId, BigDecimal.TEN, BigDecimal.valueOf(10),
                        List.of(allocRequest));
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                supplierId, null, List.of(lineRequest));

        when(supplierRepository.findById(new SupplierId(supplierId))).thenReturn(Optional.of(supplier));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        createPurchaseUseCase.execute(request, authenticatedUserId);

        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    @Test
    void should_GeneratePurchaseNumber_WithCorrectFormat() {
        UUID supplierId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Supplier supplier = buildSupplier(supplierId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreatePurchaseRequest.AllocationRequest allocRequest =
                new CreatePurchaseRequest.AllocationRequest(warehouseId, BigDecimal.TEN);
        CreatePurchaseRequest.LineRequest lineRequest =
                new CreatePurchaseRequest.LineRequest(productId, BigDecimal.TEN, BigDecimal.valueOf(10),
                        List.of(allocRequest));
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                supplierId, null, List.of(lineRequest));

        when(supplierRepository.findById(new SupplierId(supplierId))).thenReturn(Optional.of(supplier));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseRepository.getNextSequenceForYear(anyInt())).thenReturn(42);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseDetailResponse response = createPurchaseUseCase.execute(request, authenticatedUserId);

        assertNotNull(response.purchaseNumber());
        assertTrue(response.purchaseNumber().startsWith("PUR-"));
        assertTrue(response.purchaseNumber().contains("-00042"));
        verify(purchaseRepository).getNextSequenceForYear(anyInt());
    }

    @Test
    void should_IncludeSupplierSnapshot_When_Created() {
        UUID supplierId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        Supplier supplier = buildSupplier(supplierId);
        User user = buildUser(authenticatedUserId);
        Product product = buildProduct(productId);

        CreatePurchaseRequest.AllocationRequest allocRequest =
                new CreatePurchaseRequest.AllocationRequest(warehouseId, BigDecimal.TEN);
        CreatePurchaseRequest.LineRequest lineRequest =
                new CreatePurchaseRequest.LineRequest(productId, BigDecimal.TEN, BigDecimal.valueOf(10),
                        List.of(allocRequest));
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                supplierId, null, List.of(lineRequest));

        when(supplierRepository.findById(new SupplierId(supplierId))).thenReturn(Optional.of(supplier));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseDetailResponse response = createPurchaseUseCase.execute(request, authenticatedUserId);

        assertEquals(supplierId, response.supplierId());
        assertEquals("Proveedor Test C.A.", response.supplierName());
        assertEquals("J-12345678-9", response.supplierRif());
    }

    private Supplier buildSupplier(UUID id) {
        return Supplier.reconstitute(
                new SupplierId(id),
                SupplierCode.from(1001L),
                new LegalName("Proveedor Test C.A."),
                SupplierType.JURIDICO,
                new Rif("J-12345678-9"),
                new SupplierAlias("Test Prov"),
                "Notas de prueba",
                true,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(5),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private User buildUser(UUID userId) {
        return new User(
                new UserId(userId),
                new Email("user@parsun.com"),
                new UserName("Test User"),
                Password.fromHash("hashed-password")
        );
    }

    private Product buildProduct(UUID productId) {
        return Product.reconstitute(
                productId, "PROD-001", "Product A",
                null, null, false, true, null,
                BigDecimal.valueOf(8),
                List.of(), List.of(), java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
        );
    }
}
