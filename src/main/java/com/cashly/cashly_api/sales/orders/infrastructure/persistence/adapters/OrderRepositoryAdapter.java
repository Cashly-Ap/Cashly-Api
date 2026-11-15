package com.cashly.cashly_api.sales.orders.infrastructure.persistence.adapters;

import com.cashly.cashly_api.sales.orders.infrastructure.persistence.repositories.OrderJpaRepository;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.repositories.OrderNumberSequenceJpaRepository;

import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderNumberSequenceEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.mappers.OrderJpaMapper;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @org.springframework.transaction.annotation.Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderJpaMapper.toEntity(order);
        // @MapsId snapshots need their existing key during merge; null identifies a genuinely new snapshot.
        jpaRepository.findWithLinesById(order.getId().value())
                .ifPresent(existing -> {
                    var snapshotIds = existing.getLines().stream()
                            .filter(line -> line.getCategorySnapshot() != null)
                            .collect(java.util.stream.Collectors
                                    .toMap(line -> line.getId(), line -> line
                                            .getCategorySnapshot().getId()));
                    entity.getLines().forEach(line -> {
                        if (line.getCategorySnapshot() != null) {
                            line.getCategorySnapshot()
                                    .setId(snapshotIds.get(line.getId()));
                        }
                    });
                });
        OrderEntity saved = jpaRepository.save(entity);
        return OrderJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        Optional<OrderEntity> found = jpaRepository
                .findWithLinesById(id.value());
        found.ifPresent(
                order -> jpaRepository.findWithAllocationsForOrder(id.value()));
        return found.map(OrderJpaMapper::toDomain);
    }

    @Override
    public List<Order> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, String status, UUID productId, UUID categoryId,
            java.util.Set<UUID> currentCategoryProductIds, String orderBy,
            String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<OrderEntity> entities = currentCategoryProductIds.isEmpty()
                ? jpaRepository.findAllWithFilters(from, to, customerId, status,
                        productId, categoryId)
                : jpaRepository.findAllWithFilters(from, to, customerId, status,
                        productId, categoryId, currentCategoryProductIds);

        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<OrderEntity> comparator = switch (orderBy) {
            case null -> Comparator.comparing(OrderEntity::getCreatedAt);
            case "orderNumber" ->
                Comparator.comparing(OrderEntity::getOrderNumber);
            case "customerName" ->
                Comparator.comparing(OrderEntity::getCustomerName);
            case "status" -> Comparator.comparing(OrderEntity::getStatus);
            case "subtotal" -> Comparator.comparing(OrderEntity::getSubtotal);
            default -> Comparator.comparing(OrderEntity::getCreatedAt);
        };

        if (desc) {
            comparator = comparator.reversed();
        }

        return entities.stream().sorted(comparator)
                .map(OrderJpaMapper::toDomain).toList();
    }

    @Override
    public PageResponse<Order> findPageWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status, UUID productId,
            UUID categoryId, java.util.Set<UUID> currentCategoryProductIds,
            String search, int page, int size, String orderBy,
            String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        var categoryProductIds = currentCategoryProductIds.isEmpty()
                ? java.util.Set.of(new UUID(0, 0))
                : currentCategoryProductIds;
        Page<OrderEntity> result = jpaRepository.findPageWithFilters(from, to,
                customerId, status, productId, categoryId, categoryProductIds,
                searchPattern(search),
                PageRequest.of(page, size, sort(orderBy, orderDir)));
        if (result.isEmpty()) {
            return new PageResponse<>(List.of(), result.getTotalElements(),
                    result.getTotalPages(), result.getSize(),
                    result.getNumber(), result.isFirst(), result.isLast(),
                    true);
        }
        List<UUID> orderedIds = result.getContent().stream()
                .map(OrderEntity::getId).toList();
        Map<UUID, OrderEntity> entities = jpaRepository
                .findAllWithLinesByIds(orderedIds).stream()
                .collect(Collectors.toMap(OrderEntity::getId,
                        Function.identity()));
        List<Order> content = orderedIds.stream().map(entities::get)
                .filter(Objects::nonNull).map(OrderJpaMapper::toDomain).toList();
        return new PageResponse<>(content, result.getTotalElements(),
                result.getTotalPages(), result.getSize(), result.getNumber(),
                result.isFirst(), result.isLast(), content.isEmpty());
    }

    private static String searchPattern(String search) {
        if (search == null || search.isBlank()) {
            return "";
        }
        return "%" + search.trim().toLowerCase() + "%";
    }

    private static Sort sort(String orderBy, String orderDir) {
        String property = switch (orderBy == null ? "" : orderBy) {
            case "orderNumber" -> "orderNumber";
            case "customerName" -> "customerName";
            case "status" -> "status";
            case "subtotal" -> "subtotal";
            default -> "createdAt";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(orderDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    @Override
    public int getNextSequenceForYear(int year) {
        OrderNumberSequenceEntity sequence = sequenceJpaRepository
                .findByYearForUpdate(year).orElseGet(() -> {
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
