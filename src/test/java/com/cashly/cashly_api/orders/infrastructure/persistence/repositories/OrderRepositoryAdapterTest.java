package com.cashly.cashly_api.orders.infrastructure.persistence.repositories;

import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.entities.OrderLineAllocation;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderLineId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.orders.infrastructure.persistence.entities.OrderEntity;
import com.cashly.cashly_api.orders.infrastructure.persistence.entities.OrderNumberSequenceEntity;
import com.cashly.cashly_api.orders.infrastructure.persistence.mappers.OrderJpaMapper;
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

@DisplayName("OrderRepositoryAdapter Unit Tests")
class OrderRepositoryAdapterTest {

    private OrderJpaRepository jpaRepository;
    private OrderNumberSequenceJpaRepository sequenceJpaRepository;
    private ArgumentCaptor<OrderEntity> entityCaptor;
    private OrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(OrderJpaRepository.class);
        sequenceJpaRepository = mock(OrderNumberSequenceJpaRepository.class);
        entityCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        adapter = new OrderRepositoryAdapter(jpaRepository, sequenceJpaRepository);
    }

    private OrderLine createTestLine() {
        return OrderLine.reconstitute(
                OrderLineId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Test Product",
                "PRD-001",
                new BigDecimal("2"),
                new BigDecimal("50.00"),
                List.of(OrderLineAllocation.reconstitute(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("2")))
        );
    }

    private Order createTestOrder() {
        return Order.reconstitute(
                OrderId.of(UUID.randomUUID()),
                OrderNumber.of(2026, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "John Doe",
                OrderStatus.DRAFT,
                "Test notes",
                null,
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
            Order order = createTestOrder();

            OrderEntity expectedEntity = OrderJpaMapper.toEntity(order);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            Order saved = adapter.save(order);

            assertNotNull(saved);
            assertEquals(order.getId(), saved.getId());
            assertEquals(order.getOrderNumber(), saved.getOrderNumber());
            assertEquals(order.getCustomerId(), saved.getCustomerId());
            assertEquals(order.getCustomerName(), saved.getCustomerName());
            assertEquals(order.getCustomerRif(), saved.getCustomerRif());
            assertEquals(order.getStatus(), saved.getStatus());
            assertEquals(order.getSubtotal(), saved.getSubtotal());

            OrderEntity captured = entityCaptor.getValue();
            assertEquals(order.getId().value(), captured.getId());
            assertEquals(order.getOrderNumber().value(), captured.getOrderNumber());
            assertEquals(order.getCustomerId(), captured.getCustomerId());
            assertEquals(order.getCustomerName(), captured.getCustomerName());
            assertEquals(order.getCustomerRif(), captured.getCustomerRif());
            assertEquals(order.getStatus().name(), captured.getStatus());
            assertEquals(order.getSubtotal(), captured.getSubtotal());
            assertEquals(order.getCreatedAt(), captured.getCreatedAt());
            assertEquals(order.getUpdatedAt(), captured.getUpdatedAt());

            verify(jpaRepository).save(any(OrderEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            Order order = createTestOrder();
            OrderEntity entity = OrderJpaMapper.toEntity(order);

            when(jpaRepository.findWithLinesById(order.getId().value())).thenReturn(Optional.of(entity));
            when(jpaRepository.findWithAllocationsForOrder(order.getId().value())).thenReturn(List.of());

            Optional<Order> result = adapter.findById(order.getId());

            assertTrue(result.isPresent());
            assertEquals(order.getId(), result.get().getId());
            assertEquals(order.getOrderNumber(), result.get().getOrderNumber());
            assertEquals(order.getCustomerId(), result.get().getCustomerId());
            assertEquals(order.getStatus(), result.get().getStatus());

            verify(jpaRepository).findWithLinesById(order.getId().value());
            verify(jpaRepository).findWithAllocationsForOrder(order.getId().value());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            OrderId id = OrderId.generate();
            when(jpaRepository.findWithLinesById(id.value())).thenReturn(Optional.empty());

            Optional<Order> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findWithLinesById(id.value());
            verify(jpaRepository, never()).findWithAllocationsForOrder(any());
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            Order order = createTestOrder();
            OrderEntity entity = OrderJpaMapper.toEntity(order);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Order> result = adapter.findAllWithFilters(null, null, null, null, null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(order.getId(), result.getFirst().getId());
            verify(jpaRepository).findAllWithFilters(null, null, null, null, null, null);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            List<Order> result = adapter.findAllWithFilters(null, null, null, null, null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findAllWithFilters(null, null, null, null, null, null);
        }

        @Test
        void should_ApplySorting_When_OrderByIsProvided() {
            Order order1 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Order order2 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            OrderEntity entity1 = OrderJpaMapper.toEntity(order1);
            OrderEntity entity2 = OrderJpaMapper.toEntity(order2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Order> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "customerName", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("A Customer", result.getFirst().getCustomerName());
            assertEquals("B Customer", result.getLast().getCustomerName());
        }

        @Test
        void should_ConvertLocalDateToLocalDateTime_When_DatesAreProvided() {
            Order order = createTestOrder();
            OrderEntity entity = OrderJpaMapper.toEntity(order);

            LocalDate dateFrom = LocalDate.of(2026, 1, 1);
            LocalDate dateTo = LocalDate.of(2026, 12, 31);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Order> result = adapter.findAllWithFilters(dateFrom, dateTo, null, null, null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());

            verify(jpaRepository).findAllWithFilters(
                    dateFrom.atStartOfDay(),
                    dateTo.atTime(23, 59, 59),
                    null, null, null, null
            );
        }

        @Test
        void should_SortByOrderNumber() {
            Order order1 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Order order2 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            OrderEntity entity1 = OrderJpaMapper.toEntity(order1);
            OrderEntity entity2 = OrderJpaMapper.toEntity(order2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Order> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "orderNumber", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(order1.getOrderNumber(), result.getFirst().getOrderNumber());
            assertEquals(order2.getOrderNumber(), result.getLast().getOrderNumber());
        }

        @Test
        void should_SortByStatus() {
            Order order1 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Order order2 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    OrderStatus.CONFIRMED,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            OrderEntity entity1 = OrderJpaMapper.toEntity(order1);
            OrderEntity entity2 = OrderJpaMapper.toEntity(order2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Order> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "status", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(OrderStatus.CONFIRMED, result.getFirst().getStatus());
            assertEquals(OrderStatus.DRAFT, result.getLast().getStatus());
        }

        @Test
        void should_SortBySubtotal() {
            OrderLine line1 = OrderLine.reconstitute(
                    OrderLineId.of(UUID.randomUUID()),
                    UUID.randomUUID(),
                    "Product A",
                    "PRD-001",
                    new BigDecimal("3"),
                    new BigDecimal("100.00"),
                    List.of(OrderLineAllocation.reconstitute(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("3")))
            );

            OrderLine line2 = OrderLine.reconstitute(
                    OrderLineId.of(UUID.randomUUID()),
                    UUID.randomUUID(),
                    "Product B",
                    "PRD-002",
                    new BigDecimal("5"),
                    new BigDecimal("200.00"),
                    List.of(OrderLineAllocation.reconstitute(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("5")))
            );

            Order order1 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(line1),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Order order2 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(line2),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            OrderEntity entity1 = OrderJpaMapper.toEntity(order1);
            OrderEntity entity2 = OrderJpaMapper.toEntity(order2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity2, entity1));

            List<Order> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "subtotal", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.getFirst().getSubtotal().compareTo(result.getLast().getSubtotal()) < 0);
        }

        @Test
        void should_UseDefaultSortByCreatedAt_When_UnknownOrderBy() {
            Order order = createTestOrder();
            OrderEntity entity = OrderJpaMapper.toEntity(order);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Order> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "unknownField", "asc");

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        void should_ApplyDescendingSort() {
            Order order1 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 1),
                    UUID.randomUUID(),
                    "A Customer",
                    "J-11111111-1",
                    UUID.randomUUID(),
                    "John Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Order order2 = Order.reconstitute(
                    OrderId.of(UUID.randomUUID()),
                    OrderNumber.of(2026, 2),
                    UUID.randomUUID(),
                    "B Customer",
                    "J-22222222-2",
                    UUID.randomUUID(),
                    "Jane Doe",
                    OrderStatus.DRAFT,
                    "Notes",
                    null,
                    null,
                    null,
                    null,
                    List.of(createTestLine()),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            OrderEntity entity1 = OrderJpaMapper.toEntity(order1);
            OrderEntity entity2 = OrderJpaMapper.toEntity(order2);

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(entity1, entity2));

            List<Order> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, "customerName", "desc");

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
            OrderId id = OrderId.generate();

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
            OrderNumberSequenceEntity existingSeq = new OrderNumberSequenceEntity();
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
            verify(sequenceJpaRepository).save(any(OrderNumberSequenceEntity.class));
        }
    }
}
