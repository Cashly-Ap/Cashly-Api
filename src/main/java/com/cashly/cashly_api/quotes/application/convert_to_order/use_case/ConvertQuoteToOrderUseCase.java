package com.cashly.cashly_api.quotes.application.convert_to_order.use_case;

import com.cashly.cashly_api.quotes.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.quotes.application.ports.out.OrderCreationGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteConversionNotificationGateway;
import com.cashly.cashly_api.quotes.application.convert_to_order.response.QuoteConvertedToOrderResponse;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteStatus;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

public class ConvertQuoteToOrderUseCase {
    private final QuoteRepository quotes;
    private final OrderCreationGateway orders;
    private final ActorIdentityGateway actors;
    private final QuoteConversionNotificationGateway notifications;

    public ConvertQuoteToOrderUseCase(QuoteRepository quotes, OrderCreationGateway orders,
                                     ActorIdentityGateway actors, QuoteConversionNotificationGateway notifications) {
        this.quotes = quotes;
        this.orders = orders;
        this.actors = actors;
        this.notifications = notifications;
    }

    @Transactional
    public QuoteConvertedToOrderResponse execute(UUID quoteId, UUID authenticatedUserId) {
        var quote = quotes.findById(quoteId).orElseThrow(() -> new QuoteNotFoundException(quoteId));
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT quotes can be converted to orders");
        }
        var actor = authenticatedUserId == null ? null : actors.requireActor(authenticatedUserId);
        var lines = quote.getLines().stream().map(line -> new OrderCreationGateway.Line(line.getProductId(),
                line.getProductName(), line.getProductCode(), line.getCategoryId(), line.getQuantity(), line.getUnitPrice(),
                line.getAllocations().stream().map(allocation -> new OrderCreationGateway.Allocation(
                        allocation.getWarehouseId(), allocation.getProductId(), allocation.getAllocatedQuantity())).toList())).toList();
        var order = orders.create(new OrderCreationGateway.Command(quote.getCustomerId(), quote.getCustomerName(),
                quote.getCustomerRif(), actor == null ? null : actor.id(), actor == null ? null : actor.name(), lines));
        quote.markConvertedToOrder(order.id(), order.number());
        quotes.save(quote);
        notifications.convertedToOrder(quote.getCustomerId(), quote.getId(), order.number());
        return new QuoteConvertedToOrderResponse(quote.getId(), quote.getQuoteNumber().value(), order.id(), order.number());
    }
}
