package com.parsun.parsun_api.orders.infrastructure.persistence.mappers;

import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderLineId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.orders.infrastructure.persistence.entities.OrderEntity;
import com.parsun.parsun_api.orders.infrastructure.persistence.entities.OrderLineEntity;

import java.util.List;

public class OrderJpaMapper {

    private OrderJpaMapper() {}

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().value());
        entity.setOrderNumber(order.getOrderNumber().value());
        entity.setCustomerId(order.getCustomerId());
        entity.setCustomerName(order.getCustomerName());
        entity.setCustomerRif(order.getCustomerRif());
        entity.setCreatedBy(order.getCreatedBy());
        entity.setCreatedByName(order.getCreatedByName());
        entity.setStatus(order.getStatus().name());
        entity.setDeliveryDate(order.getDeliveryDate());
        entity.setNotes(order.getNotes());
        entity.setSubtotal(order.getSubtotal());
        entity.setInvoiceId(order.getInvoiceId());
        entity.setInvoiceNumber(order.getInvoiceNumber());
        entity.setCancelledAt(order.getCancelledAt());
        entity.setCancelledReason(order.getCancelledReason());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<OrderLineEntity> lineEntities = order.getLines().stream()
                .map(line -> toLineEntity(line, entity))
                .toList();
        entity.getLines().clear();
        entity.getLines().addAll(lineEntities);

        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        List<OrderLine> lines = entity.getLines().stream()
                .map(OrderJpaMapper::toLineDomain)
                .toList();

        return Order.reconstitute(
                OrderId.of(entity.getId()),
                new OrderNumber(entity.getOrderNumber()),
                entity.getCustomerId(),
                entity.getCustomerName(),
                entity.getCustomerRif(),
                entity.getCreatedBy(),
                entity.getCreatedByName(),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getDeliveryDate(),
                entity.getNotes(),
                entity.getInvoiceId(),
                entity.getInvoiceNumber(),
                entity.getCancelledAt(),
                entity.getCancelledReason(),
                lines,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static OrderLineEntity toLineEntity(OrderLine line, OrderEntity orderEntity) {
        OrderLineEntity entity = new OrderLineEntity();
        entity.setId(line.getId().value());
        entity.setOrder(orderEntity);
        entity.setProductId(line.getProductId());
        entity.setProductName(line.getProductName());
        entity.setProductCode(line.getProductCode());
        entity.setQuantity(line.getQuantity());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getLineTotal());
        return entity;
    }

    private static OrderLine toLineDomain(OrderLineEntity entity) {
        return OrderLine.reconstitute(
                OrderLineId.of(entity.getId()),
                entity.getProductId(),
                entity.getProductName(),
                entity.getProductCode(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }
}
