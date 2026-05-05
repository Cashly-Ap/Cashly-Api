package com.parsun.parsun_api.orders.application.mappers;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;

import java.util.List;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderDetailResponse toDetailResponse(Order order) {
        List<OrderDetailResponse.OrderLineDetailResponse> lineResponses = order.getLines().stream()
                .map(OrderMapper::toLineDetailResponse)
                .toList();

        return new OrderDetailResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getCustomerRif(),
                order.getCreatedBy(),
                order.getCreatedByName(),
                order.getStatus().name(),
                order.getDeliveryDate(),
                order.getNotes(),
                order.getSubtotal(),
                order.getInvoiceId(),
                order.getInvoiceNumber(),
                order.getCancelledAt(),
                order.getCancelledReason(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                lineResponses
        );
    }

    public static OrderListResponse toListResponse(Order order) {
        return new OrderListResponse(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getStatus().name(),
                order.getDeliveryDate(),
                order.getSubtotal(),
                order.getLines().size(),
                order.getInvoiceId(),
                order.getCreatedAt()
        );
    }

    private static OrderDetailResponse.OrderLineDetailResponse toLineDetailResponse(OrderLine line) {
        return new OrderDetailResponse.OrderLineDetailResponse(
                line.getId().value(),
                line.getProductId(),
                line.getProductName(),
                line.getProductCode(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineTotal()
        );
    }
}
