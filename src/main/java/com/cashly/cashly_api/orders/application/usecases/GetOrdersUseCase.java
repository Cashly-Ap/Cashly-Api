package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetOrdersUseCase {

    private final OrderRepository orderRepository;
    private final OrderProductCatalogGateway products;

    public GetOrdersUseCase(OrderRepository orderRepository, OrderProductCatalogGateway products) {
        this.orderRepository = orderRepository;
        this.products = products;
    }

    public List<OrderListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                           UUID customerId, String status,
                                           UUID productId, UUID categoryId,
                                           String orderBy, String orderDir) {
        var currentCategoryProductIds = categoryId == null ? java.util.Set.<UUID>of()
                : products.findProductIdsByCurrentCategory(categoryId);
        return orderRepository.findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId,
                        currentCategoryProductIds, orderBy, orderDir)
                .stream()
                .map(OrderMapper::toListResponse)
                .toList();
    }
}
