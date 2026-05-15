package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteConvertedToOrderResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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

    private ConvertQuoteToOrderUseCase useCase;

    private final UUID quoteId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ConvertQuoteToOrderUseCase(
                quoteRepository, orderRepository, warehouseStockRepository, userRepository);
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
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        QuoteConvertedToOrderResponse result = useCase.execute(quoteId, authenticatedUserId);

        assertNotNull(result);
        assertEquals(quoteId, result.quoteId());
        assertEquals("2026-0001", result.quoteNumber());
        assertNotNull(result.orderId());
        assertNotNull(result.orderNumber());

        // Verify stock was deducted (100 - 3 = 97)
        assertEquals(97, stock.getQuantity());

        // Verify quote was marked as converted
        assertEquals(result.orderId(), quote.getOrderId());
        assertEquals(result.orderNumber(), quote.getOrderNumber());

        // Verify order was saved in CONFIRMED status
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.CONFIRMED, savedOrder.getStatus());
        assertEquals(quote.getCustomerId(), savedOrder.getCustomerId());
        assertEquals(quote.getCustomerName(), savedOrder.getCustomerName());

        // Verify quote was saved
        verify(quoteRepository).save(quote);
    }

    @Test
    @DisplayName("should throw when quote is not in DRAFT status")
    void shouldThrowWhenQuoteNotDraft() {
        QuoteLine line = new QuoteLine(UUID.randomUUID(), "Prod", "P-001", BigDecimal.ONE, BigDecimal.TEN);
        Quote quote = Quote.reconstitute(quoteId, new QuoteNumber("2026-0001"),
                UUID.randomUUID(), "Customer", "J-123",
                authenticatedUserId, "Creator", QuoteStatus.CANCELLED,
                null, null, BigDecimal.TEN, null, null, null, null,
                null, null, List.of(line), java.time.Instant.now(), java.time.Instant.now());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                useCase.execute(quoteId, authenticatedUserId));

        assertEquals("Only DRAFT quotes can be converted to orders", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductId(any(), any());
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

    // --- Helpers ---

    private Quote buildDraftQuote(UUID quoteId, UUID warehouseId, UUID productId) {
        QuoteLine line = new QuoteLine(productId, "Product A", "PROD-001", BigDecimal.valueOf(3), new BigDecimal("100.0000"));
        QuoteAllocation allocation = new QuoteAllocation(productId, warehouseId, "Main WH", BigDecimal.valueOf(3));
        line.addAllocation(allocation);

        return Quote.reconstitute(quoteId, new QuoteNumber("2026-0001"),
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
                new Email("user@parsun.com"),
                new UserName("Test User"),
                Password.fromHash("hashed-password")
        );
    }
}
