package com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.adapters;

import com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.repositories.OrderJpaRepository;
import com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.repositories.OrderNumberSequenceJpaRepository;

import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.entities.OrderEntity;
import com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.entities.OrderNumberSequenceEntity;
import com.cashly.cashly_api.orders.infrastructure.adapters.out.persistence.mappers.OrderJpaMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderNumberSequenceJpaRepository sequenceJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository,
                                    OrderNumberSequenceJpaRepository sequenceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderJpaMapper.toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);
        return OrderJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        Optional<OrderEntity> found = jpaRepository.findWithLinesById(id.value());
        found.ifPresent(order -> jpaRepository.findWithAllocationsForOrder(id.value()));
        return found.map(OrderJpaMapper::toDomain);
    }

    @Override
    public List<Order> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                           UUID customerId, String status,
                                           UUID productId, UUID categoryId, java.util.Set<UUID> currentCategoryProductIds,
                                           String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<OrderEntity> entities = currentCategoryProductIds.isEmpty()
                ? jpaRepository.findAllWithFilters(from, to, customerId, status, productId, categoryId)
                : jpaRepository.findAllWithFilters(from, to, customerId, status, productId, categoryId,
                        currentCategoryProductIds);

        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<OrderEntity> comparator = switch (orderBy) {
            case null -> Comparator.comparing(OrderEntity::getCreatedAt);
            case "orderNumber" -> Comparator.comparing(OrderEntity::getOrderNumber);
            case "customerName" -> Comparator.comparing(OrderEntity::getCustomerName);
            case "status" -> Comparator.comparing(OrderEntity::getStatus);
            case "subtotal" -> Comparator.comparing(OrderEntity::getSubtotal);
            default -> Comparator.comparing(OrderEntity::getCreatedAt);
        };

        if (desc) {
            comparator = comparator.reversed();
        }

        return entities.stream()
                .sorted(comparator)
                .map(OrderJpaMapper::toDomain)
                .toList();
    }

    @Override
    public int getNextSequenceForYear(int year) {
        OrderNumberSequenceEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    OrderNumberSequenceEntity newSeq = new OrderNumberSequenceEntity();
                    newSeq.setYear(year);
                    newSeq.setNextValue(0);
                    return newSeq;
                });

        int next = sequence.getNextValue() + 1;
        sequence.setNextValue(next);
        sequenceJpaRepository.save(sequence);
        return next;
    }

    @Override
    public void deleteById(OrderId id) {
        jpaRepository.deleteById(id.value());
    }
}
