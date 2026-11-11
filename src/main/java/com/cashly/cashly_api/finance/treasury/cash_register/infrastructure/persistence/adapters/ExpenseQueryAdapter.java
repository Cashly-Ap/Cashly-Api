package com.cashly.cashly_api.finance.treasury.cash_register.infrastructure.persistence.adapters;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model.ExpenseFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.response.ExpenseResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.ExpenseQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashMovementJpaEntity;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashRegisterJpaEntity;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.repositories.CashMovementJpaRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.repositories.CashRegisterJpaRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class ExpenseQueryAdapter implements ExpenseQueryGateway {

    private static final Set<String> SORTABLE_FIELDS = Set.of("occurredAt",
            "amount", "description", "paymentMethodName");

    private final CashMovementJpaRepository movements;
    private final CashRegisterJpaRepository registers;

    public ExpenseQueryAdapter(CashMovementJpaRepository movements,
            CashRegisterJpaRepository registers) {
        this.movements = movements;
        this.registers = registers;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> findExpenses(ExpenseFilter filter) {
        Pageable pageable = PageRequest.of(filter.page(), filter.size(),
                parseSort(filter.sort()));
        var page = movements.findAll(specification(filter), pageable);

        Map<UUID, CashRegisterJpaEntity> registersById = registers
                .findAllById(page.getContent().stream()
                        .map(CashMovementJpaEntity::getRegisterId).distinct()
                        .toList())
                .stream().collect(Collectors.toMap(CashRegisterJpaEntity::getId,
                        Function.identity()));

        var content = page.getContent().stream()
                .map(movement -> toResponse(movement,
                        registersById.get(movement.getRegisterId())))
                .toList();
        return PageResponseMapper.from(
                new PageImpl<>(content, pageable, page.getTotalElements()));
    }

    private Specification<CashMovementJpaEntity> specification(
            ExpenseFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("type"), "EXPENSE"));
            predicates.add(cb.equal(root.get("direction"), "OUT"));
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.<java.time.LocalDateTime> get("occurredAt"),
                        filter.dateFrom().atStartOfDay()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThan(
                        root.<java.time.LocalDateTime> get("occurredAt"),
                        filter.dateTo().plusDays(1).atStartOfDay()));
            }
            if (filter.registerId() != null) {
                predicates.add(cb.equal(root.get("registerId"),
                        filter.registerId()));
            }
            if (filter.paymentMethodId() != null) {
                predicates.add(cb.equal(root.get("paymentMethodId"),
                        filter.paymentMethodId()));
            }
            if (filter.search() != null && !filter.search().isBlank()) {
                String term = "%"
                        + filter.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("description")), term),
                        cb.like(cb.lower(root.get("paymentMethodName")), term),
                        cb.like(cb.lower(root.get("createdByName")), term)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        String field = SORTABLE_FIELDS.contains(parts[0].trim())
                ? parts[0].trim()
                : "occurredAt";
        Sort.Direction direction = parts.length > 1
                && "asc".equalsIgnoreCase(parts[1].trim())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

    private ExpenseResponse toResponse(CashMovementJpaEntity movement,
            CashRegisterJpaEntity register) {
        return new ExpenseResponse(movement.getId(),
                movement.getRegisterId(),
                register != null ? register.getStatus() : null,
                register != null ? register.getOpenedAt() : null,
                register != null ? register.getClosureDate() : null,
                movement.getAmount(), movement.getDescription(),
                movement.getPaymentMethodId(), movement.getPaymentMethodName(),
                movement.getCreatedBy(), movement.getCreatedByName(),
                movement.getOccurredAt(), movement.isCancelled(),
                movement.getCancellationReason(), movement.getCancelledAt(),
                movement.getCancelledBy(), movement.getCancelledByName());
    }
}
