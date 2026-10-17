package com.cashly.cashly_api.sales.orders.application.convert_to_invoice.use_case;

import com.cashly.cashly_api.sales.orders.application.ports.out.InvoiceCreationGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderNotificationGateway;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.response.ConvertToInvoiceResponse;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConvertToInvoiceUseCase {

    private final OrderRepository orderRepository;
    private final InvoiceCreationGateway invoiceCreationGateway;
    private final OrderNotificationGateway notifications;

    public ConvertToInvoiceUseCase(OrderRepository orderRepository,
                                    InvoiceCreationGateway invoiceCreationGateway,
                                    OrderNotificationGateway notifications) {
        this.orderRepository = orderRepository;
        this.invoiceCreationGateway = invoiceCreationGateway;
        this.notifications = notifications;
    }

    @Transactional
    public ConvertToInvoiceResponse execute(UUID orderId, ConvertToInvoiceRequest request, UUID authenticatedUserId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED orders can be invoiced");
        }

        InvoiceCreationGateway.Command createRequest = new InvoiceCreationGateway.Command(
                order.getId().value(),
                request.paymentType(),
                request.useBalance(),
                request.balanceAmount(),
                request.paymentMethodId(),
                request.confirmationPassword(),
                request.issueDate()
        );

        InvoiceCreationGateway.CreatedInvoice invoice = invoiceCreationGateway.create(createRequest, authenticatedUserId);

        order.markAsInvoiced(invoice.id(), invoice.invoiceNumber());
        orderRepository.save(order);

        notifications.notifyCustomerOrderConvertedToInvoice(order.getCustomerId(), order.getId().value(), invoice.invoiceNumber());

        return new ConvertToInvoiceResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getStatus().name(),
                invoice.id(),
                invoice.invoiceNumber()
        );
    }
}
