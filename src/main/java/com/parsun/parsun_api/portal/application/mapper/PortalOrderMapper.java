package com.parsun.parsun_api.portal.application.mapper;

import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.portal.application.dto.response.PortalOrderDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalOrderItemResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalOrderListResponse;

import java.util.List;

public final class PortalOrderMapper {

    private PortalOrderMapper() {}

    public static PortalOrderListResponse toListResponse(Order order) {
        return new PortalOrderListResponse(
            order.getId().value(),
            order.getOrderNumber().value(),
            order.getCreatedAt(),
            order.getStatus().name(),
            order.getSubtotal(),
            order.getLines().size(),
            order.getCreatedAt()
        );
    }

    public static PortalOrderDetailResponse toDetailResponse(Order order) {
        List<PortalOrderItemResponse> items = order.getLines().stream()
            .map(PortalOrderMapper::toItemResponse)
            .toList();

        return new PortalOrderDetailResponse(
            order.getId().value(),
            order.getOrderNumber().value(),
            order.getCreatedAt(),
            order.getStatus().name(),
            order.getSubtotal(),
            order.getSubtotal(),
            order.getNotes(),
            order.getCreatedByName(),
            order.getCreatedAt(),
            order.getInvoiceNumber(),
            items
        );
    }

    private static PortalOrderItemResponse toItemResponse(OrderLine line) {
        return new PortalOrderItemResponse(
            line.getProductId(),
            line.getProductCode(),
            line.getProductName(),
            line.getQuantity(),
            line.getUnitPrice(),
            line.getLineTotal()
        );
    }
}
