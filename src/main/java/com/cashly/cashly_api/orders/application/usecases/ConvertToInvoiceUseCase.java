package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.invoices.application.dto.request.CreateInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.usecases.CreateInvoiceUseCase;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.orders.application.dto.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.orders.application.dto.response.ConvertToInvoiceResponse;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConvertToInvoiceUseCase {

    private final OrderRepository orderRepository;
    private final CreateInvoiceUseCase createInvoiceUseCase;
    private final NotificationService notificationService;

    public ConvertToInvoiceUseCase(OrderRepository orderRepository,
                                    CreateInvoiceUseCase createInvoiceUseCase,
                                    NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.createInvoiceUseCase = createInvoiceUseCase;
        this.notificationService = notificationService;
    }

    @Transactional
    public ConvertToInvoiceResponse execute(UUID orderId, ConvertToInvoiceRequest request, UUID authenticatedUserId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        CreateInvoiceRequest createRequest = new CreateInvoiceRequest(
                order.getId().value(),
                null,
                null,
                request.paymentType(),
                null,
                request.useBalance(),
                request.balanceAmount(),
                request.paymentMethodId(),
                request.confirmationPassword(),
                null,
                request.issueDate()
        );

        InvoiceDetailResponse invoice = createInvoiceUseCase.execute(createRequest, authenticatedUserId);

        order.markAsInvoiced(invoice.id(), invoice.invoiceNumber());
        orderRepository.save(order);

        notificationService.notifyCustomerOrderConvertedToInvoice(order.getCustomerId(), order.getId().value(), invoice.invoiceNumber());

        return new ConvertToInvoiceResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getStatus().name(),
                invoice.id(),
                invoice.invoiceNumber()
        );
    }
}
