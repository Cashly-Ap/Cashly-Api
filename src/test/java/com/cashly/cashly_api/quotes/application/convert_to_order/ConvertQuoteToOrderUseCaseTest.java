package com.cashly.cashly_api.quotes.application.convert_to_order;

import com.cashly.cashly_api.auth.user.domain.User;
import com.cashly.cashly_api.auth.user.domain.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.orders.domain.Order;
import com.cashly.cashly_api.orders.domain.OrderLine;
import com.cashly.cashly_api.orders.domain.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.quotes.application.convert_to_order.QuoteConvertedToOrderResponse;
import com.cashly.cashly_api.quotes.domain.Quote;
import com.cashly.cashly_api.quotes.domain.QuoteAllocation;
import com.cashly.cashly_api.quotes.domain.QuoteLine;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.quotes.domain.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteStatus;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConvertQuoteToOrderUseCase Unit Tests")
class ConvertQuoteToOrderUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    private ConvertQuoteToOrderUseCase useCase;

    private final UUID quoteId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ConvertQuoteToOrderUseCase(
                quoteRepository,
                new com.cashly.cashly_api.quotes.infrastructure.integration.LocalOrderCreationGateway(
                    new com.cashly.cashly_api.orders.application.create.from_quote.QuoteOrderCreationService(orderRepository,
                        new com.cashly.cashly_api.orders.infrastructure.integration.LocalOrderInventoryGateway(
                            new com.cashly.cashly_api.warehouses.stock.application.deduction.StockDeductionService(warehouseStockRepository)))),
                new com.cashly.cashly_api.quotes.infrastructure.integration.QuoteActorIdentityAdapter(
                    new com.cashly.cashly_api.auth.user.application.queries.RepositoryActorIdentityQuery(userRepository,
                        mock(com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder.class))),
                new com.cashly.cashly_api.quotes.infrastructure.integration.QuoteConversionNotificationAdapter(
                    new com.cashly.cashly_api.notifications.application.quotes.QuoteConversionNotificationService(notificationService)));
    }

    @Test
    @DisplayName("should convert DRAFT quote to CONFIRMED order and deduct stock")
    void shouldConvertDraftQuoteToConfirmedOrder() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Quote quote = buildDraftQuote(quoteId, warehouseId, productId);
        User user = buildUser(authenticatedUserId);
        WarehouseStock stock = new WarehouseStock(null, warehouseId, productId, 100, 0, null, null);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(5);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteConvertedToOrderResponse result = useCase.execute(quoteId, authenticatedUserId);

        assertNotNull(result);
        assertEquals(quoteId, result.quoteId());
        assertEquals("PRE-2026-0001", result.quoteNumber());
        assertNotNull(result.orderId());
        assertNotNull(result.orderNumber());

        
        assertEquals(97, stock.getQuantity());

        
        assertEquals(result.orderId(), quote.getOrderId());
        assertEquals(result.orderNumber(), quote.getOrderNumber());

        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.CONFIRMED, savedOrder.getStatus());
        assertEquals(quote.getCustomerId(), savedOrder.getCustomerId());
        assertEquals(quote.getCustomerName(), savedOrder.getCustomerName());

        
        verify(quoteRepository).save(quote);
    }

    @Test
    @DisplayName("should throw when quote is not in DRAFT status")
    void shouldThrowWhenQuoteNotDraft() {
        QuoteLine line = new QuoteLine(UUID.randomUUID(), "Prod", "P-001", BigDecimal.ONE, BigDecimal.TEN);
        Quote quote = Quote.reconstitute(quoteId, new QuoteNumber("PRE-2026-0001"),
                UUID.randomUUID(), "Customer", "J-123",
                authenticatedUserId, "Creator", QuoteStatus.CANCELLED,
                null, null, BigDecimal.TEN, null, null, null, null,
                null, null, List.of(line), java.time.Instant.now(), java.time.Instant.now());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                useCase.execute(quoteId, authenticatedUserId));

        assertEquals("Only DRAFT quotes can be converted to orders", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductIdForUpdate(any(), any());
    }

    @Test
    @DisplayName("should throw when quote is not found")
    void shouldThrowWhenQuoteNotFound() {
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        assertThrows(QuoteNotFoundException.class, () ->
                useCase.execute(quoteId, authenticatedUserId));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when user is not found")
    void shouldThrowWhenUserNotFound() {
        Quote quote = buildDraftQuote(quoteId, UUID.randomUUID(), UUID.randomUUID());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(quoteId, authenticatedUserId));

        verify(orderRepository, never()).save(any());
    }

    

    @Test
    void shouldDeductTheProductReferenceStoredInTheAllocation() {
        UUID warehouseId = UUID.randomUUID();
        UUID lineProductId = UUID.randomUUID();
        UUID allocationProductId = UUID.randomUUID();
        Quote quote = buildDraftQuote(quoteId, warehouseId, lineProductId);
        QuoteLine line = quote.getLines().getFirst();
        line.removeAllocation(line.getAllocations().getFirst().getId());
        line.addAllocation(new QuoteAllocation(allocationProductId, warehouseId, "Warehouse", BigDecimal.valueOf(3)));
        WarehouseStock stock = new WarehouseStock(null, warehouseId, allocationProductId, 100, 0, null, null);
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(orderRepository.getNextSequenceForYear(anyInt())).thenReturn(5);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, allocationProductId))
                .thenReturn(Optional.of(stock));

        useCase.execute(quoteId, null);

        assertEquals(97, stock.getQuantity());
        verify(userRepository, never()).findById(any());
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductIdForUpdate(warehouseId, lineProductId);
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertEquals(lineProductId, captor.getValue().getLines().getFirst().getProductId());
    }

    private Quote buildDraftQuote(UUID quoteId, UUID warehouseId, UUID productId) {
        QuoteLine line = new QuoteLine(productId, "Product A", "PROD-001", BigDecimal.valueOf(3), new BigDecimal("100.0000"));
        QuoteAllocation allocation = new QuoteAllocation(productId, warehouseId, "Main WH", BigDecimal.valueOf(3));
        line.addAllocation(allocation);

        return Quote.reconstitute(quoteId, new QuoteNumber("PRE-2026-0001"),
                UUID.randomUUID(), "Empresa Test C.A.", "J-12345678-9",
                authenticatedUserId, "Creator", QuoteStatus.DRAFT,
                LocalDate.of(2026, 6, 30), null, new BigDecimal("300.0000"),
                null, null, null, null,
                null, null,
                List.of(line), java.time.Instant.now(), java.time.Instant.now());
    }

    private User buildUser(UUID userId) {
        return new User(
                new UserId(userId),
                new Email("user@cashly.com"),
                new UserName("Test User"),
                Password.fromHash("hashed-password")
        );
    }
}
