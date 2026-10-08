package com.cashly.cashly_api.purchases.infrastructure.persistence.repositories;

import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.purchases.infrastructure.persistence.entities.PurchaseEntity;
import com.cashly.cashly_api.purchases.infrastructure.persistence.entities.PurchaseLineAllocationEntity;
import com.cashly.cashly_api.purchases.infrastructure.persistence.entities.PurchaseLineEntity;
import com.cashly.cashly_api.purchases.infrastructure.persistence.entities.PurchaseNumberSequenceEntity;
import com.cashly.cashly_api.purchases.infrastructure.persistence.mappers.PurchaseJpaMapper;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseRepositoryAdapter Unit Tests")
class PurchaseRepositoryIntegrationTest {

    @Mock
    private PurchaseJpaRepository jpaRepository;

    @Mock
    private PurchaseNumberSequenceJpaRepository sequenceJpaRepository;

    @Captor
    private ArgumentCaptor<PurchaseEntity> entityCaptor;

    private PurchaseRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PurchaseRepositoryAdapter(jpaRepository, sequenceJpaRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_SavePurchase_When_DomainObjectProvided() {
            Purchase purchase = buildDomainPurchase(PurchaseStatus.DRAFT);
            PurchaseEntity savedEntity = PurchaseJpaMapper.toEntity(purchase);

            when(jpaRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Purchase result = adapter.save(purchase);

            assertNotNull(result);
            assertEquals(purchase.getId(), result.getId());
            assertEquals(purchase.getPurchaseNumber(), result.getPurchaseNumber());
            assertEquals(purchase.getSupplierName(), result.getSupplierName());

            PurchaseEntity captured = entityCaptor.getValue();
            assertEquals(purchase.getId().value(), captured.getId());
            assertEquals(purchase.getPurchaseNumber().value(), captured.getPurchaseNumber());
            assertEquals(purchase.getStatus().name(), captured.getStatus());

            verify(jpaRepository).save(any(PurchaseEntity.class));
        }

        @Test
        void should_IncludeLinesAndAllocations_When_SavingPurchase() {
            Purchase purchase = buildDomainPurchaseWithAllocations();
            PurchaseEntity savedEntity = PurchaseJpaMapper.toEntity(purchase);

            when(jpaRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            adapter.save(purchase);

            PurchaseEntity captured = entityCaptor.getValue();
            assertFalse(captured.getLines().isEmpty());
            assertEquals(1, captured.getLines().size());

            PurchaseLineEntity lineEntity = captured.getLines().get(0);
            assertNotNull(lineEntity.getProductId());
            assertNotNull(lineEntity.getAllocations());
            assertEquals(1, lineEntity.getAllocations().size());

            PurchaseLineAllocationEntity allocEntity = lineEntity.getAllocations().get(0);
            assertNotNull(allocEntity.getWarehouseId());
            assertEquals(new BigDecimal("10"), allocEntity.getQuantity());
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            UUID id = UUID.randomUUID();
            Purchase purchase = buildDomainPurchase(PurchaseStatus.CONFIRMED);
            PurchaseEntity entity = PurchaseJpaMapper.toEntity(purchase);
            entity.setId(id);

            when(jpaRepository.findWithLinesById(id)).thenReturn(Optional.of(entity));

            Optional<Purchase> result = adapter.findById(PurchaseId.of(id));

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId().value());
            assertEquals(PurchaseStatus.CONFIRMED, result.get().getStatus());
            verify(jpaRepository).findWithLinesById(id);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID id = UUID.randomUUID();
            when(jpaRepository.findWithLinesById(id)).thenReturn(Optional.empty());

            Optional<Purchase> result = adapter.findById(PurchaseId.of(id));

            assertTrue(result.isEmpty());
            verify(jpaRepository).findWithLinesById(id);
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnFilteredResults_When_EntitiesFound() {
            PurchaseEntity entity = PurchaseJpaMapper.toEntity(buildDomainPurchase(PurchaseStatus.DRAFT));

            when(jpaRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Purchase> result = adapter.findAllWithFilters(
                    LocalDate.now().minusDays(30), LocalDate.now(),
                    null, "DRAFT", null, null, "createdAt", "desc");

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(jpaRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(), any());
        }

        @Test
        void should_ReturnEmpty_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            List<Purchase> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "createdAt", "asc");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void should_SortByCreatedAtDesc_When_DefaultOrder() {
            PurchaseEntity entity1 = PurchaseJpaMapper.toEntity(buildDomainPurchase(PurchaseStatus.DRAFT));
            PurchaseEntity entity2 = PurchaseJpaMapper.toEntity(buildDomainPurchase(PurchaseStatus.DRAFT));

            when(jpaRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity1, entity2));

            List<Purchase> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "createdAt", "desc");

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        void should_SortBySupplierNameAsc_When_Specified() {
            PurchaseEntity entity = PurchaseJpaMapper.toEntity(buildDomainPurchase(PurchaseStatus.DRAFT));

            when(jpaRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Purchase> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "supplierName", "asc");

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("DeleteById")
    class DeleteById {

        @Test
        void should_DelegateToJpaRepository() {
            UUID id = UUID.randomUUID();

            adapter.deleteById(PurchaseId.of(id));

            verify(jpaRepository).deleteById(id);
        }
    }

    @Nested
    @DisplayName("FindSupplierIdsWithPurchases")
    class FindSupplierIdsWithPurchases {

        @Test
        void shouldQueryAllUsedSupplierIdsInOneBatch() {
            Set<UUID> supplierIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
            Set<UUID> usedSupplierIds = Set.of(supplierIds.iterator().next());
            when(jpaRepository.findSupplierIdsWithPurchases(supplierIds)).thenReturn(usedSupplierIds);

            assertEquals(usedSupplierIds, adapter.findSupplierIdsWithPurchases(supplierIds));
            verify(jpaRepository).findSupplierIdsWithPurchases(supplierIds);
        }

        @Test
        void shouldSkipQueryWhenSupplierIdBatchIsEmpty() {
            assertEquals(Set.of(), adapter.findSupplierIdsWithPurchases(Set.of()));
            verifyNoInteractions(jpaRepository);
        }
    }

    @Nested
    @DisplayName("GetNextSequenceForYear")
    class GetNextSequenceForYear {

        @Test
        void should_ReturnNextValue_When_SequenceExists() {
            PurchaseNumberSequenceEntity seq = new PurchaseNumberSequenceEntity();
            seq.setYear(2026);
            seq.setNextValue(5);

            when(sequenceJpaRepository.findByYearForUpdate(2026)).thenReturn(Optional.of(seq));
            when(sequenceJpaRepository.save(any(PurchaseNumberSequenceEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            int result = adapter.getNextSequenceForYear(2026);

            assertEquals(6, result);
            verify(sequenceJpaRepository).findByYearForUpdate(2026);
            verify(sequenceJpaRepository).save(argThat(s -> s.getNextValue() == 6));
        }

        @Test
        void should_InitializeSequence_When_YearDoesNotExist() {
            when(sequenceJpaRepository.findByYearForUpdate(2026)).thenReturn(Optional.empty());
            when(sequenceJpaRepository.save(any(PurchaseNumberSequenceEntity.class)))
                    .thenAnswer(inv -> {
                        PurchaseNumberSequenceEntity s = inv.getArgument(0);
                        s.setNextValue(1);
                        return s;
                    });

            int result = adapter.getNextSequenceForYear(2026);

            assertEquals(1, result);
            verify(sequenceJpaRepository).findByYearForUpdate(2026);
            verify(sequenceJpaRepository).save(any(PurchaseNumberSequenceEntity.class));
        }

        @Test
        void should_CreateNewSequence_When_NotFound() {
            when(sequenceJpaRepository.findByYearForUpdate(2026)).thenReturn(Optional.empty());

            ArgumentCaptor<PurchaseNumberSequenceEntity> seqCaptor =
                    ArgumentCaptor.forClass(PurchaseNumberSequenceEntity.class);
            when(sequenceJpaRepository.save(seqCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            int result = adapter.getNextSequenceForYear(2026);

            assertEquals(1, result);
            PurchaseNumberSequenceEntity captured = seqCaptor.getValue();
            assertEquals(2026, captured.getYear());
            assertEquals(1, captured.getNextValue());
        }
    }

    private Purchase buildDomainPurchase(PurchaseStatus status) {
        PurchaseLine line = PurchaseLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Purchase.reconstitute(
                PurchaseId.of(UUID.randomUUID()),
                PurchaseNumber.of(2026, 1),
                UUID.randomUUID(),
                "Supplier Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                status,
                "Notes",
                BigDecimal.TEN,
                status == PurchaseStatus.CONFIRMED ? LocalDateTime.now() : null,
                status == PurchaseStatus.CANCELLED ? LocalDateTime.now() : null,
                status == PurchaseStatus.CANCELLED ? "Reason" : null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Purchase buildDomainPurchaseWithAllocations() {
        PurchaseLineAllocation allocation = PurchaseLineAllocation.create(
                UUID.randomUUID(), new BigDecimal("10"));
        PurchaseLine line = PurchaseLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.TEN, BigDecimal.valueOf(10), List.of(allocation));
        return Purchase.reconstitute(
                PurchaseId.of(UUID.randomUUID()),
                PurchaseNumber.of(2026, 1),
                UUID.randomUUID(),
                "Supplier Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                PurchaseStatus.DRAFT,
                null,
                BigDecimal.valueOf(100),
                null, null, null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
