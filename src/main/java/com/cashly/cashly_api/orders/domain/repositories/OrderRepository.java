package com.cashly.cashly_api.orders.domain.repositories;

import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                   UUID customerId, String status,
                                   UUID productId, UUID categoryId, java.util.Set<UUID> currentCategoryProductIds,
                                   String orderBy, String orderDir);
    default List<Order> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                           UUID customerId, String status, UUID productId, UUID categoryId,
                                           String orderBy, String orderDir) {
        return findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId,
                java.util.Set.of(), orderBy, orderDir);
    }
    int getNextSequenceForYear(int year);
    void deleteById(OrderId id);
}
