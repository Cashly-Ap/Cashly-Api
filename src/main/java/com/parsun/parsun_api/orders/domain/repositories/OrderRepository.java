package com.parsun.parsun_api.orders.domain.repositories;

import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                   UUID customerId, String status,
                                   UUID productId, UUID categoryId,
                                   String orderBy, String orderDir);
    int getNextSequenceForYear(int year);
    void deleteById(OrderId id);
}
