package com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.adapters;


import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnId;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.ReturnLine;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.ReturnLineAllocation;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnNumber;
import com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnType;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnLineAllocationEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnLineEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnNumberSequenceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaReturnRepository Unit Tests")
class JpaReturnRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<ReturnEntity> typedQuery;

    @Captor
    private ArgumentCaptor<ReturnEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<ReturnNumberSequenceEntity> sequenceCaptor;

    private JpaReturnRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaReturnRepository(entityManager);
    }

    private Return createTestReturn() {
        ReturnId id = ReturnId.of(UUID.randomUUID());
        ReturnNumber returnNumber = ReturnNumber.of(2026, 1);
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        ReturnType returnType = ReturnType.AUTOMATIC;
        String reason = "Product damaged during shipping";
        BigDecimal total = new BigDecimal("250.00");

        UUID lineInvoiceLineId = UUID.randomUUID();
        UUID lineProductId = UUID.randomUUID();

        List<ReturnLine> lines = List.of(
                ReturnLine.create(
                        lineInvoiceLineId,
                        lineProductId,
                        new BigDecimal("2"),
                        new BigDecimal("125.00"),
                        new BigDecimal("250.00"),
                        List.of(
                                ReturnLineAllocation.reconstitute(
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        new BigDecimal("2")
                                )
                        )
                )
        );

        UUID createdBy = UUID.randomUUID();
        String createdByName = "John Doe";
        LocalDateTime now = LocalDateTime.now();

        return Return.reconstitute(
                id, returnNumber, invoiceId, customerId,
                returnType, reason, total, lines,
                List.of(),
                createdBy, createdByName, now, now
        );
    }

    private ReturnEntity createTestEntity(Return domain) {
        ReturnEntity entity = new ReturnEntity();
        entity.setId(domain.getId().value());
        entity.setReturnNumber(domain.getReturnNumber().value());
        entity.setInvoiceId(domain.getInvoiceId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setReturnType(domain.getReturnType().name());
        entity.setReason(domain.getReason());
        entity.setTotal(domain.getTotal());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedByName(domain.getCreatedByName());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        List<ReturnLineEntity> lineEntities = domain.getLines().stream()
                .map(line -> {
                    ReturnLineEntity le = new ReturnLineEntity();
                    le.setId(UUID.randomUUID());
                    le.setReturnEntity(entity);
                    le.setInvoiceLineId(line.getInvoiceLineId());
                    le.setProductId(line.getProductId());
                    le.setQuantity(line.getQuantity());
                    le.setUnitPrice(line.getUnitPrice());
                    le.setLineTotal(line.getSubtotal());

                    List<ReturnLineAllocationEntity> allocEntities = line.getAllocations().stream()
                            .map(alloc -> {
                                ReturnLineAllocationEntity ae = new ReturnLineAllocationEntity();
                                ae.setId(alloc.getId());
                                ae.setReturnLine(le);
                                ae.setWarehouseId(alloc.getWarehouseId());
                                ae.setAllocated(alloc.getAllocated());
                                return ae;
                            })
                            .toList();
                    le.getAllocations().addAll(allocEntities);
                    return le;
                })
                .toList();

        entity.getLines().addAll(lineEntities);
        return entity;
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        @DisplayName("persists new return when entity not found in database")
        void save_Create_When_IdIsNull() {
            Return return_ = createTestReturn();

            doNothing().when(entityManager).persist(entityCaptor.capture());

            Return saved = adapter.save(return_);

            assertNotNull(saved);
            assertEquals(return_.getId(), saved.getId());
            assertEquals(return_.getReturnNumber(), saved.getReturnNumber());
            assertEquals(return_.getInvoiceId(), saved.getInvoiceId());
            assertEquals(return_.getCustomerId(), saved.getCustomerId());
            assertEquals(return_.getReturnType(), saved.getReturnType());
            assertEquals(return_.getReason(), saved.getReason());
            assertEquals(return_.getTotal(), saved.getTotal());
            assertEquals(return_.getLines().size(), saved.getLines().size());

            ReturnEntity captured = entityCaptor.getValue();
            assertEquals(return_.getId().value(), captured.getId());
            assertEquals(return_.getReturnNumber().value(), captured.getReturnNumber());
            assertEquals(return_.getInvoiceId(), captured.getInvoiceId());
            assertEquals(return_.getCustomerId(), captured.getCustomerId());
            assertEquals(return_.getReturnType().name(), captured.getReturnType());
            assertEquals(return_.getReason(), captured.getReason());
            assertEquals(return_.getTotal(), captured.getTotal());
            assertEquals(return_.getCreatedBy(), captured.getCreatedBy());
            assertEquals(return_.getCreatedByName(), captured.getCreatedByName());
            assertEquals(return_.getCreatedAt(), captured.getCreatedAt());
            assertEquals(return_.getUpdatedAt(), captured.getUpdatedAt());
            assertEquals(return_.getLines().size(), captured.getLines().size());

            verify(entityManager).persist(any(ReturnEntity.class));
            verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("persists new return when entity has id but is not found in DB")
        void save_Create_When_EntityNotFound() {
            Return return_ = createTestReturn();

            when(entityManager.find(ReturnEntity.class, return_.getId().value()))
                    .thenReturn(null);
            doNothing().when(entityManager).persist(entityCaptor.capture());

            Return saved = adapter.save(return_);

            assertNotNull(saved);
            assertEquals(return_.getId(), saved.getId());

            ReturnEntity captured = entityCaptor.getValue();
            assertEquals(return_.getId().value(), captured.getId());
            assertNotNull(captured.getLines());
            assertEquals(1, captured.getLines().size());

            ReturnLineEntity lineEntity = captured.getLines().getFirst();
            assertEquals(return_.getLines().getFirst().getInvoiceLineId(), lineEntity.getInvoiceLineId());
            assertEquals(return_.getLines().getFirst().getProductId(), lineEntity.getProductId());
            assertEquals(return_.getLines().getFirst().getQuantity(), lineEntity.getQuantity());
            assertEquals(return_.getLines().getFirst().getUnitPrice(), lineEntity.getUnitPrice());
            assertEquals(return_.getLines().getFirst().getSubtotal(), lineEntity.getLineTotal());
            assertNotNull(lineEntity.getAllocations());
            assertEquals(1, lineEntity.getAllocations().size());

            verify(entityManager).persist(any(ReturnEntity.class));
            verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("merges existing return when entity is found in database")
        void save_Update_When_EntityExists() {
            Return return_ = createTestReturn();
            ReturnEntity existingEntity = createTestEntity(return_);

            when(entityManager.find(ReturnEntity.class, return_.getId().value()))
                    .thenReturn(existingEntity);
            when(entityManager.merge(any(ReturnEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Return saved = adapter.save(return_);

            assertNotNull(saved);
            assertEquals(return_.getId(), saved.getId());
            assertEquals(return_.getReturnNumber(), saved.getReturnNumber());
            assertEquals(return_.getInvoiceId(), saved.getInvoiceId());
            assertEquals(return_.getTotal(), saved.getTotal());
            assertEquals(return_.getLines().size(), saved.getLines().size());

            verify(entityManager, never()).persist(any());
            verify(entityManager).merge(any(ReturnEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        @DisplayName("returns domain object when entity is found")
        void findById_Found() {
            Return domain = createTestReturn();
            ReturnEntity entity = createTestEntity(domain);

            when(entityManager.find(ReturnEntity.class, domain.getId().value()))
                    .thenReturn(entity);

            Optional<Return> result = adapter.findById(domain.getId());

            assertTrue(result.isPresent());
            assertEquals(domain.getId(), result.get().getId());
            assertEquals(domain.getReturnNumber(), result.get().getReturnNumber());
            assertEquals(domain.getInvoiceId(), result.get().getInvoiceId());
            assertEquals(domain.getCustomerId(), result.get().getCustomerId());
            assertEquals(domain.getReturnType(), result.get().getReturnType());
            assertEquals(domain.getReason(), result.get().getReason());
            assertEquals(domain.getTotal(), result.get().getTotal());
            assertEquals(domain.getCreatedBy(), result.get().getCreatedBy());
            assertEquals(domain.getCreatedByName(), result.get().getCreatedByName());
            assertEquals(domain.getLines().size(), result.get().getLines().size());

            verify(entityManager).find(ReturnEntity.class, domain.getId().value());
        }

        @Test
        @DisplayName("returns empty when entity is not found")
        void findById_NotFound() {
            ReturnId id = ReturnId.of(UUID.randomUUID());

            when(entityManager.find(ReturnEntity.class, id.value()))
                    .thenReturn(null);

            Optional<Return> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(entityManager).find(ReturnEntity.class, id.value());
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        @DisplayName("returns domain objects when all filters are provided")
        void findAllWithFilters_WithAllFilters() {
            Return domain = createTestReturn();
            ReturnEntity entity = createTestEntity(domain);

            when(entityManager.createQuery(anyString(), eq(ReturnEntity.class)))
                    .thenReturn(typedQuery);
            when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of(entity));

            LocalDate dateFrom = LocalDate.of(2026, 1, 1);
            LocalDate dateTo = LocalDate.of(2026, 12, 31);
            UUID customerId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();

            List<Return> results = adapter.findAllWithFilters(dateFrom, dateTo, customerId, invoiceId);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(domain.getId(), results.getFirst().getId());
            assertEquals(domain.getReturnNumber(), results.getFirst().getReturnNumber());

            verify(entityManager).createQuery(anyString(), eq(ReturnEntity.class));
            verify(typedQuery, times(4)).setParameter(anyString(), any());
            verify(typedQuery).getResultList();
        }

        @Test
        @DisplayName("returns domain objects when only some filters are provided")
        void findAllWithFilters_WithSomeFilters() {
            Return domain = createTestReturn();
            ReturnEntity entity = createTestEntity(domain);

            when(entityManager.createQuery(anyString(), eq(ReturnEntity.class)))
                    .thenReturn(typedQuery);
            when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of(entity));

            LocalDate dateFrom = LocalDate.of(2026, 1, 1);

            List<Return> results = adapter.findAllWithFilters(dateFrom, null, null, null);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(domain.getId(), results.getFirst().getId());

            verify(entityManager).createQuery(anyString(), eq(ReturnEntity.class));
            verify(typedQuery, times(1)).setParameter(anyString(), any());
            verify(typedQuery).getResultList();
        }

        @Test
        @DisplayName("returns empty list when no entities match filters")
        void findAllWithFilters_Empty() {
            when(entityManager.createQuery(anyString(), eq(ReturnEntity.class)))
                    .thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of());

            List<Return> results = adapter.findAllWithFilters(null, null, null, null);

            assertNotNull(results);
            assertTrue(results.isEmpty());

            verify(entityManager).createQuery(anyString(), eq(ReturnEntity.class));
            verify(typedQuery, never()).setParameter(anyString(), any());
            verify(typedQuery).getResultList();
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        @DisplayName("removes entity when found")
        void delete_Found() {
            Return domain = createTestReturn();
            ReturnEntity entity = createTestEntity(domain);

            when(entityManager.find(ReturnEntity.class, domain.getId().value()))
                    .thenReturn(entity);

            adapter.delete(domain.getId());

            verify(entityManager).find(ReturnEntity.class, domain.getId().value());
            verify(entityManager).remove(entity);
        }

        @Test
        @DisplayName("does nothing when entity is not found")
        void delete_NotFound() {
            ReturnId id = ReturnId.of(UUID.randomUUID());

            when(entityManager.find(ReturnEntity.class, id.value()))
                    .thenReturn(null);

            adapter.delete(id);

            verify(entityManager).find(ReturnEntity.class, id.value());
            verify(entityManager, never()).remove(any());
        }
    }

    @Nested
    @DisplayName("GetNextSequenceForYear")
    class GetNextSequenceForYear {

        @Test
        @DisplayName("increments and returns next value when sequence exists")
        void getNextSequenceForYear_ExistingSequence() {
            int year = 2026;
            ReturnNumberSequenceEntity existingSeq = new ReturnNumberSequenceEntity();
            existingSeq.setYear(year);
            existingSeq.setNextValue(5);

            when(entityManager.find(
                    eq(ReturnNumberSequenceEntity.class), eq(year),
                    eq(LockModeType.PESSIMISTIC_WRITE)))
                    .thenReturn(existingSeq);
            when(entityManager.merge(any(ReturnNumberSequenceEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(6, next);
            assertEquals(6, existingSeq.getNextValue().intValue());

            verify(entityManager).find(
                    eq(ReturnNumberSequenceEntity.class), eq(year),
                    eq(LockModeType.PESSIMISTIC_WRITE));
            verify(entityManager).merge(existingSeq);
        }

        @Test
        @DisplayName("creates new sequence and returns 1 when no sequence exists")
        void getNextSequenceForYear_NewSequence() {
            int year = 2026;

            when(entityManager.find(
                    eq(ReturnNumberSequenceEntity.class), eq(year),
                    eq(LockModeType.PESSIMISTIC_WRITE)))
                    .thenReturn(null);
            when(entityManager.merge(sequenceCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(1, next);

            ReturnNumberSequenceEntity captured = sequenceCaptor.getValue();
            assertEquals(year, captured.getYear().intValue());
            assertEquals(1, captured.getNextValue().intValue());

            verify(entityManager).find(
                    eq(ReturnNumberSequenceEntity.class), eq(year),
                    eq(LockModeType.PESSIMISTIC_WRITE));
            verify(entityManager).merge(any(ReturnNumberSequenceEntity.class));
        }
    }
}
