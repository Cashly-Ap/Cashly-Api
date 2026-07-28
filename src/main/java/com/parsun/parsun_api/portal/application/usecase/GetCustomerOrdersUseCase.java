package com.parsun.parsun_api.portal.application.usecase;

import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.portal.application.dto.response.PortalOrderDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalOrderListResponse;
import com.parsun.parsun_api.portal.application.mapper.PortalOrderMapper;

import java.util.List;
import java.util.UUID;

public class GetCustomerOrdersUseCase {

    private final OrderRepository orderRepository;

    public GetCustomerOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<PortalOrderListResponse> execute(String customerId) {
        UUID customerUuid = UUID.fromString(customerId);
        return orderRepository.findAllWithFilters(null, null, customerUuid, null, null, null, "createdAt", "desc")
            .stream()
            .map(PortalOrderMapper::toListResponse)
            .toList();
    }

    public PortalOrderDetailResponse execute(String customerId, String orderId) {
        UUID customerUuid = UUID.fromString(customerId);
        OrderId orderIdObj = OrderId.of(UUID.fromString(orderId));

        var order = orderRepository.findById(orderIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getCustomerId().equals(customerUuid)) {
            throw new IllegalArgumentException("Order not found");
        }

        return PortalOrderMapper.toDetailResponse(order);
    }
}
