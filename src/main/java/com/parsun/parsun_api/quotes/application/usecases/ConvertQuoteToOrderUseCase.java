package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.entities.OrderLineAllocation;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteConvertedToOrderResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConvertQuoteToOrderUseCase {

    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final UserRepository userRepository;

    public ConvertQuoteToOrderUseCase(QuoteRepository quoteRepository,
                                       OrderRepository orderRepository,
                                       WarehouseStockRepository warehouseStockRepository,
                                       UserRepository userRepository) {
        this.quoteRepository = quoteRepository;
        this.orderRepository = orderRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuoteConvertedToOrderResponse execute(UUID quoteId, UUID authenticatedUserId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));

        if (quote.getStatus() != com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT quotes can be converted to orders");
        }

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        int year = LocalDate.now().getYear();
        int sequence = orderRepository.getNextSequenceForYear(year);
        OrderNumber orderNumber = OrderNumber.of(year, sequence);

        List<OrderLine> orderLines = quote.getLines().stream()
                .map(this::toOrderLine)
                .toList();

        Order order = Order.reconstitute(
                OrderId.generate(), orderNumber,
                quote.getCustomerId(), quote.getCustomerName(), quote.getCustomerRif(),
                authenticatedUserId, user.getUserName().getValue(),
                OrderStatus.CONFIRMED, null,
                null, null, null, null,
                orderLines, LocalDateTime.now(), LocalDateTime.now()
        );

        
        for (QuoteLine quoteLine : quote.getLines()) {
            for (QuoteAllocation allocation : quoteLine.getAllocations()) {
                WarehouseStock stock = warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), allocation.getProductId())
                        .orElseThrow(() -> new IllegalStateException(
                                "No stock found for product " + allocation.getProductId() +
                                " in warehouse " + allocation.getWarehouseId()));

                int amount = allocation.getAllocatedQuantity().intValue();
                stock.removeStock(amount);
                warehouseStockRepository.save(stock);
            }
        }

        Order savedOrder = orderRepository.save(order);

        quote.markConvertedToOrder(savedOrder.getId().value(), savedOrder.getOrderNumber().value());
        quoteRepository.save(quote);

        return new QuoteConvertedToOrderResponse(
                quote.getId(),
                quote.getQuoteNumber().value(),
                savedOrder.getId().value(),
                savedOrder.getOrderNumber().value()
        );
    }

    private OrderLine toOrderLine(QuoteLine quoteLine) {
        List<OrderLineAllocation> allocations = quoteLine.getAllocations().stream()
                .map(a -> OrderLineAllocation.create(a.getWarehouseId(), a.getAllocatedQuantity()))
                .toList();

        return OrderLine.create(
                quoteLine.getProductId(),
                quoteLine.getProductName(),
                quoteLine.getProductCode(),
                quoteLine.getQuantity(),
                quoteLine.getUnitPrice(),
                allocations
        );
    }
}
