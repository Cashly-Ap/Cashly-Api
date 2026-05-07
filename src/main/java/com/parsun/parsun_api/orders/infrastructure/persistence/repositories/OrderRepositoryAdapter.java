package com.parsun.parsun_api.orders.infrastructure.persistence.repositories;

import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.orders.infrastructure.persistence.entities.OrderEntity;
import com.parsun.parsun_api.orders.infrastructure.persistence.entities.OrderNumberSequenceEntity;
import com.parsun.parsun_api.orders.infrastructure.persistence.mappers.OrderJpaMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public List<Order> findAllWithFilters(UUID customerId, OrderStatus status,
                                          LocalDate fromDate, LocalDate toDate,
                                          UUID createdBy) {
        String statusStr = status != null ? status.name() : null;
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : null;
        return jpaRepository.findAllWithFilters(customerId, statusStr, from, to, createdBy)
                .stream()
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
