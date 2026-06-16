package com.parsun.parsun_api.payments.infrastructure.persistence.repositories;

import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.infrastructure.persistence.entities.PaymentAllocationEntity;
import com.parsun.parsun_api.payments.infrastructure.persistence.mappers.PaymentAllocationJpaMapper;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentAllocationRepositoryAdapter Unit Tests")
class PaymentAllocationRepositoryAdapterTest {

    @Mock
    private PaymentAllocationJpaRepository jpaRepository;

    @Captor
    private ArgumentCaptor<PaymentAllocationEntity> entityCaptor;

    private PaymentAllocationRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PaymentAllocationRepositoryAdapter(jpaRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            PaymentAllocation allocation = PaymentAllocation.reconstitute(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("500.00"),
                    LocalDateTime.now()
            );

            PaymentAllocationEntity expectedEntity = PaymentAllocationJpaMapper.toEntity(allocation);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            PaymentAllocation saved = adapter.save(allocation);

            assertNotNull(saved);
            assertEquals(allocation.getId(), saved.getId());
            assertEquals(allocation.getPaymentId(), saved.getPaymentId());
            assertEquals(allocation.getInvoiceId(), saved.getInvoiceId());
            assertEquals(allocation.getAllocatedAmount(), saved.getAllocatedAmount());
            assertEquals(allocation.getCreatedAt(), saved.getCreatedAt());

            PaymentAllocationEntity captured = entityCaptor.getValue();
            assertEquals(allocation.getId(), captured.getId());
            assertEquals(allocation.getPaymentId(), captured.getPaymentId());
            assertEquals(allocation.getInvoiceId(), captured.getInvoiceId());
            assertEquals(allocation.getAllocatedAmount(), captured.getAllocatedAmount());
            assertEquals(allocation.getCreatedAt(), captured.getCreatedAt());

            verify(jpaRepository).save(any(PaymentAllocationEntity.class));
        }

        @Test
        void should_ReturnUpdatedObject_When_SavingExistingAllocation() {
            UUID allocationId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();

            PaymentAllocation original = PaymentAllocation.reconstitute(
                    allocationId, paymentId, invoiceId,
                    new BigDecimal("500.00"), LocalDateTime.now().minusDays(1)
            );

            PaymentAllocation updated = PaymentAllocation.reconstitute(
                    allocationId, paymentId, invoiceId,
                    new BigDecimal("750.00"), LocalDateTime.now()
            );

            PaymentAllocationEntity expectedEntity = PaymentAllocationJpaMapper.toEntity(updated);
            when(jpaRepository.save(any(PaymentAllocationEntity.class))).thenReturn(expectedEntity);

            PaymentAllocation saved = adapter.save(updated);

            assertNotNull(saved);
            assertEquals(allocationId, saved.getId());
            assertEquals(new BigDecimal("750.00"), saved.getAllocatedAmount());

            verify(jpaRepository).save(any(PaymentAllocationEntity.class));
        }
    }

    @Nested
    @DisplayName("FindByPaymentId")
    class FindByPaymentId {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            UUID paymentId = UUID.randomUUID();
            PaymentAllocationEntity entity = PaymentAllocationJpaMapper.toEntity(
                    PaymentAllocation.reconstitute(
                            UUID.randomUUID(), paymentId, UUID.randomUUID(),
                            new BigDecimal("500.00"), LocalDateTime.now()
                    )
            );

            when(jpaRepository.findByPaymentId(paymentId)).thenReturn(List.of(entity));

            List<PaymentAllocation> result = adapter.findByPaymentId(paymentId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(paymentId, result.get(0).getPaymentId());
            verify(jpaRepository).findByPaymentId(paymentId);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            UUID paymentId = UUID.randomUUID();
            when(jpaRepository.findByPaymentId(paymentId)).thenReturn(List.of());

            List<PaymentAllocation> result = adapter.findByPaymentId(paymentId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findByPaymentId(paymentId);
        }
    }

    @Nested
    @DisplayName("FindByInvoiceId")
    class FindByInvoiceId {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            UUID invoiceId = UUID.randomUUID();
            PaymentAllocationEntity entity = PaymentAllocationJpaMapper.toEntity(
                    PaymentAllocation.reconstitute(
                            UUID.randomUUID(), UUID.randomUUID(), invoiceId,
                            new BigDecimal("500.00"), LocalDateTime.now()
                    )
            );

            when(jpaRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(entity));

            List<PaymentAllocation> result = adapter.findByInvoiceId(invoiceId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(invoiceId, result.get(0).getInvoiceId());
            verify(jpaRepository).findByInvoiceId(invoiceId);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            UUID invoiceId = UUID.randomUUID();
            when(jpaRepository.findByInvoiceId(invoiceId)).thenReturn(List.of());

            List<PaymentAllocation> result = adapter.findByInvoiceId(invoiceId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findByInvoiceId(invoiceId);
        }
    }

    @Nested
    @DisplayName("DeleteByPaymentId")
    class DeleteByPaymentId {

        @Test
        void should_DelegateToJpaRepository() {
            UUID paymentId = UUID.randomUUID();

            adapter.deleteByPaymentId(paymentId);

            verify(jpaRepository).deleteByPaymentId(paymentId);
        }
    }

    @Nested
    @DisplayName("GetAllocatedAmountByInvoiceId")
    class GetAllocatedAmountByInvoiceId {

        @Test
        void should_ReturnSumOfAllocatedAmounts() {
            UUID invoiceId = UUID.randomUUID();
            BigDecimal expectedSum = new BigDecimal("1250.00");
            when(jpaRepository.sumAllocatedAmountByInvoiceId(invoiceId)).thenReturn(expectedSum);

            BigDecimal result = adapter.getAllocatedAmountByInvoiceId(invoiceId);

            assertEquals(expectedSum, result);
            verify(jpaRepository).sumAllocatedAmountByInvoiceId(invoiceId);
        }

        @Test
        void should_ReturnZero_When_NoAllocations() {
            UUID invoiceId = UUID.randomUUID();
            when(jpaRepository.sumAllocatedAmountByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);

            BigDecimal result = adapter.getAllocatedAmountByInvoiceId(invoiceId);

            assertEquals(BigDecimal.ZERO, result);
            verify(jpaRepository).sumAllocatedAmountByInvoiceId(invoiceId);
        }
    }
}
