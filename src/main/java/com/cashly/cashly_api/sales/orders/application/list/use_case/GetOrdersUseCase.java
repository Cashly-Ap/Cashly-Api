package com.cashly.cashly_api.sales.orders.application.list.use_case;

import com.cashly.cashly_api.sales.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.sales.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;

import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.dto.PageResponses;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetOrdersUseCase {

    private final OrderRepository orderRepository;
    private final OrderProductCatalogGateway products;

    public GetOrdersUseCase(OrderRepository orderRepository,
            OrderProductCatalogGateway products) {
        this.orderRepository = orderRepository;
        this.products = products;
    }

    public List<OrderListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, String status, UUID productId, UUID categoryId,
            String orderBy, String orderDir) {
        var currentCategoryProductIds = categoryId == null
                ? java.util.Set.<UUID>of()
                : products.findProductIdsByCurrentCategory(categoryId);
        return orderRepository
                .findAllWithFilters(dateFrom, dateTo, customerId, status,
                        productId, categoryId, currentCategoryProductIds,
                        orderBy, orderDir)
                .stream().map(OrderMapper::toListResponse).toList();
    }
    public List<OrderListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, String status, UUID productId, UUID categoryId,
            String orderBy, String orderDir, DocumentAction action) {
        if (action == null)
            return execute(dateFrom, dateTo, customerId, status, productId,
                    categoryId, orderBy, orderDir);
        String eligibleStatus = switch (action) {
            case EDIT, CONFIRM -> "DRAFT";
            case CANCEL, CONVERT_TO_INVOICE -> "CONFIRMED";
            case DELETE -> "DRAFT";
            default -> throw new IllegalArgumentException(
                    "Unsupported order action: " + action);
        };
        if (status != null && !status.isBlank() && !"ALL".equals(status)
                && !eligibleStatus.equals(status)) {
            return List.of();
        }
        return execute(dateFrom, dateTo, customerId, eligibleStatus, productId,
                categoryId, orderBy, orderDir);
    }

    public PageResponse<OrderListResponse> executePage(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status, UUID productId,
            UUID categoryId, String search, int page, int size, String orderBy,
            String orderDir, DocumentAction action) {
        if (action != null) {
            List<OrderListResponse> all = execute(dateFrom, dateTo, customerId,
                    status, productId, categoryId, orderBy, orderDir, action)
                    .stream().filter(order -> matchesSearch(search,
                            order.orderNumber(), order.customerName()))
                    .toList();
            return PageResponses.page(all, page, size);
        }
        var currentCategoryProductIds = categoryId == null
                ? java.util.Set.<UUID>of()
                : products.findProductIdsByCurrentCategory(categoryId);
        PageResponse<Order> orders = orderRepository.findPageWithFilters(
                dateFrom, dateTo, customerId, status, productId, categoryId,
                currentCategoryProductIds, search, page, size, orderBy,
                orderDir);
        List<OrderListResponse> content = orders.content().stream()
                .map(OrderMapper::toListResponse).toList();
        return new PageResponse<>(content, orders.totalElements(),
                orders.totalPages(), orders.size(), orders.number(),
                orders.first(), orders.last(), content.isEmpty());
    }

    private static boolean matchesSearch(String search, String... values) {
        if (search == null || search.isBlank())
            return true;
        String term = search.trim().toLowerCase();
        return java.util.Arrays.stream(values)
                .filter(java.util.Objects::nonNull)
                .anyMatch(value -> value.toLowerCase().contains(term));
    }

}
