package com.cashly.cashly_api.sales.quotes.infrastructure.persistence.adapters;

import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.mappers.QuoteJpaMapper;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories.QuoteJpaRepository;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteNumberSequenceJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories.QuoteNumberSequenceJpaRepository;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JpaQuoteRepositoryAdapter implements QuoteRepository {

    private final QuoteJpaRepository jpaRepository;
    private final QuoteNumberSequenceJpaRepository sequenceJpaRepository;
    private final QuoteJpaMapper mapper;

    public JpaQuoteRepositoryAdapter(QuoteJpaRepository jpaRepository,
            QuoteNumberSequenceJpaRepository sequenceJpaRepository,
            QuoteJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Quote save(Quote quote) {
        QuoteJpaEntity entity = mapper.toJpa(quote);
        // @MapsId snapshots need their existing key during merge; null identifies a genuinely new snapshot.
        jpaRepository.findByIdWithLines(quote.getId()).ifPresent(existing -> {
            var snapshotIds = existing.getLines().stream()
                    .filter(line -> line.getCategorySnapshot() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            line -> line.getId(),
                            line -> line.getCategorySnapshot().getId()));
            entity.getLines().forEach(line -> {
                if (line.getCategorySnapshot() != null) {
                    line.getCategorySnapshot()
                            .setId(snapshotIds.get(line.getId()));
                }
            });
        });
        QuoteJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quote> findById(UUID id) {
        return jpaRepository.findByIdWithLines(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quote> findEligibleForDeletion(UUID id) {
        return jpaRepository.findEligibleForDeletion(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quote> findAll() {
        return jpaRepository.findAllWithLines().stream().map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Quote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, String status, UUID productId, UUID categoryId,
            java.util.Set<UUID> currentCategoryProductIds, String orderBy,
            String orderDir) {
        Instant from = startOfDay(dateFrom);
        Instant to = endOfDay(dateTo);

        List<QuoteJpaEntity> entities = currentCategoryProductIds.isEmpty()
                ? jpaRepository.findAllWithFilters(from, to, customerId, status,
                        productId, categoryId)
                : jpaRepository.findAllWithFilters(from, to, customerId, status,
                        productId, categoryId, currentCategoryProductIds);

        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<QuoteJpaEntity> comparator = switch (orderBy) {
            case null -> Comparator.comparing(QuoteJpaEntity::getCreatedAt);
            case "quoteNumber" ->
                Comparator.comparing(QuoteJpaEntity::getQuoteNumber);
            case "customerName" ->
                Comparator.comparing(QuoteJpaEntity::getCustomerName);
            case "status" -> Comparator.comparing(QuoteJpaEntity::getStatus);
            case "subtotal" ->
                Comparator.comparing(QuoteJpaEntity::getSubtotal);
            default -> Comparator.comparing(QuoteJpaEntity::getCreatedAt);
        };

        if (desc) {
            comparator = comparator.reversed();
        }

        return entities.stream().sorted(comparator).map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Quote> findPageWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status, UUID productId,
            UUID categoryId, java.util.Set<UUID> currentCategoryProductIds,
            String search, int page, int size, String orderBy,
            String orderDir) {
        Instant from = startOfDay(dateFrom);
        Instant to = endOfDay(dateTo);
        var categoryProductIds = currentCategoryProductIds.isEmpty()
                ? java.util.Set.of(new UUID(0, 0))
                : currentCategoryProductIds;
        Page<QuoteJpaEntity> result = jpaRepository.findPageWithFilters(from,
                to, customerId, status, productId, categoryId,
                categoryProductIds, searchPattern(search),
                PageRequest.of(page, size, sort(orderBy, orderDir)));
        if (result.isEmpty()) {
            return new PageResponse<>(List.of(), result.getTotalElements(),
                    result.getTotalPages(), result.getSize(),
                    result.getNumber(), result.isFirst(), result.isLast(),
                    true);
        }
        List<UUID> orderedIds = result.getContent().stream()
                .map(QuoteJpaEntity::getId).toList();
        Map<UUID, QuoteJpaEntity> entities = jpaRepository
                .findAllWithLinesByIds(orderedIds).stream()
                .collect(Collectors.toMap(QuoteJpaEntity::getId,
                        Function.identity()));
        List<Quote> content = orderedIds.stream().map(entities::get)
                .filter(Objects::nonNull).map(mapper::toDomain).toList();
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
            case "quoteNumber" -> "quoteNumber";
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
    @Transactional(readOnly = true)
    public List<Quote> findAllEligibleForDeletion(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status, UUID productId,
            UUID categoryId, java.util.Set<UUID> currentCategoryProductIds,
            String orderBy, String orderDir) {
        Instant from = startOfDay(dateFrom);
        Instant to = endOfDay(dateTo);
        var entities = jpaRepository.findAllEligibleForDeletion(from, to,
                customerId, status, productId, categoryId,
                currentCategoryProductIds.isEmpty()
                        ? java.util.Set.of(new UUID(0, 0))
                        : currentCategoryProductIds);
        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<QuoteJpaEntity> comparator = switch (orderBy) {
            case null -> Comparator.comparing(QuoteJpaEntity::getCreatedAt);
            case "quoteNumber" -> Comparator.comparing(QuoteJpaEntity::getQuoteNumber);
            case "customerName" -> Comparator.comparing(QuoteJpaEntity::getCustomerName);
            case "status" -> Comparator.comparing(QuoteJpaEntity::getStatus);
            case "subtotal" -> Comparator.comparing(QuoteJpaEntity::getSubtotal);
            default -> Comparator.comparing(QuoteJpaEntity::getCreatedAt);
        };
        if (desc) comparator = comparator.reversed();
        return entities.stream().sorted(comparator).map(mapper::toDomain).toList();
    }

    private static Instant startOfDay(LocalDate date) {
        return date == null
                ? null
                : date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static Instant endOfDay(LocalDate date) {
        return date == null
                ? null
                : date.atTime(23, 59, 59)
                        .atZone(ZoneId.systemDefault()).toInstant();
    }

    @Override
    public boolean existsByQuoteNumber(QuoteNumber quoteNumber) {
        return jpaRepository.existsByQuoteNumber(quoteNumber.value());
    }

    @Override
    @Transactional
    public int getNextSequenceForYear(int year) {
        QuoteNumberSequenceJpaEntity sequence = sequenceJpaRepository
                .findByYearForUpdate(year).orElseGet(() -> {
                    QuoteNumberSequenceJpaEntity newSeq = new QuoteNumberSequenceJpaEntity();
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
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
