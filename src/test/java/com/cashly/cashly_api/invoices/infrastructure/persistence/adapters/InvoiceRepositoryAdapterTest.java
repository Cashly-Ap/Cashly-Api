package com.cashly.cashly_api.invoices.infrastructure.persistence.adapters;

import com.cashly.cashly_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import com.cashly.cashly_api.invoices.infrastructure.persistence.repositories.InvoiceNumberSequenceJpaRepository;

import com.cashly.cashly_api.invoices.domain.Invoice;
import com.cashly.cashly_api.invoices.domain.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.InvoiceLineAllocation;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceLineId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.invoices.infrastructure.persistence.entities.InvoiceNumberSequenceEntity;
import com.cashly.cashly_api.invoices.infrastructure.persistence.mappers.InvoiceJpaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("InvoiceRepositoryAdapter Unit Tests")
class InvoiceRepositoryAdapterTest {

    private InvoiceJpaRepository jpaRepository;
    private InvoiceNumberSequenceJpaRepository sequenceJpaRepository;
    private ArgumentCaptor<InvoiceEntity> entityCaptor;
    private InvoiceRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(InvoiceJpaRepository.class);
        sequenceJpaRepository = mock(InvoiceNumberSequenceJpaRepository.class);
        entityCaptor = ArgumentCaptor.forClass(InvoiceEntity.class);
        adapter = new InvoiceRepositoryAdapter(jpaRepository, sequenceJpaRepository);
    }

    private InvoiceLine createTestLine() {
        return InvoiceLine.reconstitute(
                InvoiceLineId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Test Product",
                "PRD-001",
                new BigDecimal("2"),
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                List.of(InvoiceLineAllocation.reconstitute(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("2")))
        );
    }

    private Invoice createTestInvoice() {
        return Invoice.reconstitute(
                InvoiceId.of(UUID.randomUUID()),
                InvoiceNumber.of(2026, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "John Doe",
                UUID.randomUUID(),
                "ORD-2026-00001",
                InvoiceStatus.DRAFT,
                PaymentType.CREDIT,
                "Test notes",
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(createTestLine()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            Invoice invoice = createTestInvoice();

            InvoiceEntity expectedEntity = InvoiceJpaMapper.toEntity(invoice);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            Invoice saved = adapter.save(invoice);

            assertNotNull(saved);
            assertEquals(invoice.getId(), saved.getId());
            assertEquals(invoice.getInvoiceNumber(), saved.getInvoiceNumber());
            assertEquals(invoice.getCustomerId(), saved.getCustomerId());
            assertEquals(invoice.getCustomerName(), saved.getCustomerName());
            assertEquals(invoice.getCustomerRif(), saved.getCustomerRif());
            assertEquals(invoice.getStatus(), saved.getStatus());
            assertEquals(invoice.getPaymentType(), saved.getPaymentType());
            assertEquals(invoice.getSubtotal(), saved.getSubtotal());

            InvoiceEntity captured = entityCaptor.getValue();
            assertEquals(invoice.getId().value(), captured.getId());
            assertEquals(invoice.getInvoiceNumber().value(), captured.getInvoiceNumber());
            assertEquals(invoice.getCustomerId(), captured.getCustomerId());
            assertEquals(invoice.getCustomerName(), captured.getCustomerName());
            assertEquals(invoice.getCustomerRif(), captured.getCustomerRif());
            assertEquals(invoice.getStatus().name(), captured.getStatus());
            assertEquals(invoice.getPaymentType().name(), captured.getPaymentType());
            assertEquals(invoice.getSubtotal(), captured.getSubtotal());
            assertEquals(invoice.getCreatedAt(), captured.getCreatedAt());
            assertEquals(invoice.getUpdatedAt(), captured.getUpdatedAt());

            verify(jpaRepository).save(any(InvoiceEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            Invoice invoice = createTestInvoice();
            InvoiceEntity entity = InvoiceJpaMapper.toEntity(invoice);

            when(jpaRepository.findWithLinesById(invoice.getId().value())).thenReturn(Optional.of(entity));

            Optional<Invoice> result = adapter.findById(invoice.getId());

            assertTrue(result.isPresent());
            assertEquals(invoice.getId(), result.get().getId());
            assertEquals(invoice.getInvoiceNumber(), result.get().getInvoiceNumber());
            assertEquals(invoice.getCustomerId(), result.get().getCustomerId());
            assertEquals(invoice.getStatus(), result.get().getStatus());
            verify(jpaRepository).findWithLinesById(invoice.getId().value());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            InvoiceId id = InvoiceId.generate();
            when(jpaRepository.findWithLinesById(id.value())).thenReturn(Optional.empty());

            Optional<Invoice> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findWithLinesById(id.value());
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            Invoice invoice = createTestInvoice();
            InvoiceEntity entity = InvoiceJpaMapper.toEntity(invoice);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Invoice> result = adapter.findAllWithFilters(null, null, null, null, null, null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(invoice.getId(), result.getFirst().getId());
            verify(jpaRepository).findAllWithFilters(null, null, null, null, null, null, null);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            List<Invoice> result = adapter.findAllWithFilters(null, null, null, null, null, null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findAllWithFilters(null, null, null, null, null, null, null);
        }

        @Test
        void should_ApplySorting_When_OrderByIsProvided() {
            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CREDIT,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.DRAFT,
                    PaymentType.CASH,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "customerName", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("A Customer", result.getFirst().getCustomerName());
            assertEquals("B Customer", result.getLast().getCustomerName());
        }

        @Test
        void should_ConvertLocalDateToLocalDateTime_When_DatesAreProvided() {
            Invoice invoice = createTestInvoice();
            InvoiceEntity entity = InvoiceJpaMapper.toEntity(invoice);

            LocalDate dateFrom = LocalDate.of(2026, 1, 1);
            LocalDate dateTo = LocalDate.of(2026, 12, 31);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Invoice> result = adapter.findAllWithFilters(dateFrom, dateTo, null, null, null, null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());

            verify(jpaRepository).findAllWithFilters(
                    dateFrom.atStartOfDay(),
                    dateTo.atTime(23, 59, 59),
                    null, null, null, null, null
            );
        }

        @Test
        void should_SortByInvoiceNumber() {
            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CREDIT,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.PENDING_PAYMENT,
                    PaymentType.CASH,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "invoiceNumber", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(invoice1.getInvoiceNumber(), result.getFirst().getInvoiceNumber());
            assertEquals(invoice2.getInvoiceNumber(), result.getLast().getInvoiceNumber());
        }

        @Test
        void should_SortByStatus() {
            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CREDIT,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.PENDING_PAYMENT,
                    PaymentType.CASH,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "status", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(InvoiceStatus.DRAFT, result.getFirst().getStatus());
            assertEquals(InvoiceStatus.PENDING_PAYMENT, result.getLast().getStatus());
        }

        @Test
        void should_SortBySubtotal() {
            InvoiceLine line1 = InvoiceLine.reconstitute(
                    InvoiceLineId.of(UUID.randomUUID()),
                    UUID.randomUUID(),
                    "Product A",
                    "PRD-001",
                    new BigDecimal("3"),
                    new BigDecimal("100.00"),
                    BigDecimal.ZERO,
                    List.of(InvoiceLineAllocation.reconstitute(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("3")))
            );

            InvoiceLine line2 = InvoiceLine.reconstitute(
                    InvoiceLineId.of(UUID.randomUUID()),
                    UUID.randomUUID(),
                    "Product B",
                    "PRD-002",
                    new BigDecimal("5"),
                    new BigDecimal("200.00"),
                    BigDecimal.ZERO,
                    List.of(InvoiceLineAllocation.reconstitute(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5")))
            );

            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CREDIT,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(line1),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.DRAFT,
                    PaymentType.CASH,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(line2),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "subtotal", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.getFirst().getSubtotal().compareTo(result.getLast().getSubtotal()) < 0);
        }

        @Test
        void should_SortByTotal() {
            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CREDIT,
                    "Notes",
                    new BigDecimal("50.00"),
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.DRAFT,
                    PaymentType.CASH,
                    "Notes",
                    new BigDecimal("10.00"),
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "total", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(invoice1.getInvoiceNumber(), result.getFirst().getInvoiceNumber());
            assertEquals(invoice2.getInvoiceNumber(), result.getLast().getInvoiceNumber());
        }

        @Test
        void should_SortByTotal_When_ReturnedTotalIsNull() {
            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CREDIT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.DRAFT,
                    PaymentType.CASH,
                    "Notes",
                    new BigDecimal("10.00"),
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            entity1.setReturnedTotal(null);

            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "total", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(invoice2.getInvoiceNumber(), result.getFirst().getInvoiceNumber());
            assertEquals(invoice1.getInvoiceNumber(), result.getLast().getInvoiceNumber());
        }

        @Test
        void should_SortByPaymentType() {
            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CASH,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.PENDING_PAYMENT,
                    PaymentType.CREDIT,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "paymentType", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(PaymentType.CASH, result.getFirst().getPaymentType());
            assertEquals(PaymentType.CREDIT, result.getLast().getPaymentType());
        }

        @Test
        void should_UseDefaultSortByCreatedAt_When_UnknownOrderBy() {
            Invoice invoice1 = createTestInvoice();
            Invoice invoice2 = createTestInvoice();

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity1, entity2));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "unknownField", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        void should_ApplyDescendingSort() {
            Invoice invoice1 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00001",
                    InvoiceStatus.DRAFT,
                    PaymentType.CREDIT,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Invoice invoice2 = Invoice.reconstitute(
                    InvoiceId.of(UUID.randomUUID()),
                    InvoiceNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    UUID.randomUUID(),
                    "ORD-2026-00002",
                    InvoiceStatus.DRAFT,
                    PaymentType.CASH,
                    "Notes",
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            InvoiceEntity entity1 = InvoiceJpaMapper.toEntity(invoice1);
            InvoiceEntity entity2 = InvoiceJpaMapper.toEntity(invoice2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity1, entity2));

            List<Invoice> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, "customerName", "desc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("B Customer", result.getFirst().getCustomerName());
            assertEquals("A Customer", result.getLast().getCustomerName());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToJpaRepository() {
            InvoiceId id = InvoiceId.generate();

            adapter.deleteById(id);

            verify(jpaRepository).deleteById(id.value());
        }
    }

    @Nested
    @DisplayName("GetNextSequenceForYear")
    class GetNextSequenceForYear {

        @Test
        void should_ReturnNextValueAndSave_When_ExistingSequenceFound() {
            int year = LocalDate.now().getYear();
            InvoiceNumberSequenceEntity existingSeq = new InvoiceNumberSequenceEntity();
            existingSeq.setYear(year);
            existingSeq.setNextValue(5);

            when(sequenceJpaRepository.findByYearForUpdate(year)).thenReturn(Optional.of(existingSeq));

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(6, next);
            assertEquals(6, existingSeq.getNextValue().intValue());
            verify(sequenceJpaRepository).findByYearForUpdate(year);
            verify(sequenceJpaRepository).save(existingSeq);
        }

        @Test
        void should_CreateNewSequenceAndReturnOne_When_NoExistingSequence() {
            int year = LocalDate.now().getYear();

            when(sequenceJpaRepository.findByYearForUpdate(year)).thenReturn(Optional.empty());

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(1, next);
            verify(sequenceJpaRepository).findByYearForUpdate(year);
            verify(sequenceJpaRepository).save(any(InvoiceNumberSequenceEntity.class));
        }
    }
}
