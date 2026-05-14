package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.request.CreateInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.usecases.CreateInvoiceUseCase;
import com.parsun.parsun_api.orders.application.dto.response.ConvertToInvoiceResponse;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ConvertToInvoiceUseCase {

    private final OrderRepository orderRepository;
    private final CreateInvoiceUseCase createInvoiceUseCase;

    public ConvertToInvoiceUseCase(OrderRepository orderRepository,
                                    CreateInvoiceUseCase createInvoiceUseCase) {
        this.orderRepository = orderRepository;
        this.createInvoiceUseCase = createInvoiceUseCase;
    }

    @Transactional
    public ConvertToInvoiceResponse execute(UUID orderId, String paymentType, UUID authenticatedUserId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        CreateInvoiceRequest request = new CreateInvoiceRequest(
                order.getId().value(),
                null,
                null,
                paymentType,
                null,
                null
        );

        InvoiceDetailResponse invoice = createInvoiceUseCase.execute(request, authenticatedUserId);

        order.markAsInvoiced(invoice.id(), invoice.invoiceNumber());
        orderRepository.save(order);

        return new ConvertToInvoiceResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getStatus().name(),
                invoice.id(),
                invoice.invoiceNumber()
        );
    }
}
