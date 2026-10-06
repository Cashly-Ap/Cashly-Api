package com.cashly.cashly_api.payments.infrastructure.persistence.repositories;

import com.cashly.cashly_api.payments.domain.entities.Payment;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.payments.infrastructure.persistence.entities.PaymentEntity;
import com.cashly.cashly_api.payments.infrastructure.persistence.mappers.PaymentJpaMapper;
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
@DisplayName("PaymentRepositoryAdapter Unit Tests")
class PaymentRepositoryAdapterTest {

    @Mock
    private PaymentJpaRepository jpaRepository;

    @Captor
    private ArgumentCaptor<PaymentEntity> entityCaptor;

    private PaymentRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PaymentRepositoryAdapter(jpaRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            Payment payment = Payment.reconstitute(
                    PaymentId.generate(),
                    PaymentNumber.of(2026, 1),
                    UUID.randomUUID(),
                    new BigDecimal("500.00"),
                    UUID.randomUUID(),
                    PaymentSource.PAYMENT_METHOD,
                    "REF-001",
                    LocalDate.now(),
                    "Test payment",
                    PaymentStatus.APPLIED,
                    BigDecimal.ZERO,
                    UUID.randomUUID(),
                    "John Doe",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            PaymentEntity expectedEntity = PaymentJpaMapper.toEntity(payment);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            Payment saved = adapter.save(payment);

            assertNotNull(saved);
            assertEquals(payment.getId(), saved.getId());
            assertEquals(payment.getPaymentNumber(), saved.getPaymentNumber());
            assertEquals(payment.getAmount(), saved.getAmount());
            assertEquals(payment.getStatus(), saved.getStatus());

            PaymentEntity captured = entityCaptor.getValue();
            assertEquals(payment.getId().value(), captured.getId());
            assertEquals(payment.getPaymentNumber().value(), captured.getPaymentNumber());
            assertEquals(payment.getCustomerId(), captured.getCustomerId());
            assertEquals(payment.getAmount(), captured.getAmount());
            assertEquals(payment.getPaymentMethodId(), captured.getPaymentMethodId());
            assertEquals(payment.getSource().name(), captured.getSource());
            assertEquals(payment.getReferenceNumber(), captured.getReferenceNumber());
            assertEquals(payment.getPaymentDate(), captured.getPaymentDate());
            assertEquals(payment.getNotes(), captured.getNotes());
            assertEquals(payment.getStatus().name(), captured.getStatus());
            assertEquals(payment.getAppliedAmount(), captured.getAppliedAmount());
            assertEquals(payment.getCreatedBy(), captured.getCreatedBy());
            assertEquals(payment.getCreatedByName(), captured.getCreatedByName());
            assertEquals(payment.getCreatedAt(), captured.getCreatedAt());
            assertEquals(payment.getUpdatedAt(), captured.getUpdatedAt());

            verify(jpaRepository).save(any(PaymentEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            PaymentId id = PaymentId.generate();
            PaymentEntity entity = PaymentJpaMapper.toEntity(
                    Payment.reconstitute(
                            id,
                            PaymentNumber.of(2026, 1),
                            UUID.randomUUID(),
                            new BigDecimal("500.00"),
                            UUID.randomUUID(),
                            PaymentSource.PAYMENT_METHOD,
                            "REF-001",
                            LocalDate.now(),
                            "Test payment",
                            PaymentStatus.APPLIED,
                            BigDecimal.ZERO,
                            UUID.randomUUID(),
                            "John Doe",
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );

            when(jpaRepository.findById(id.value())).thenReturn(Optional.of(entity));

            Optional<Payment> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            verify(jpaRepository).findById(id.value());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            PaymentId id = PaymentId.generate();
            when(jpaRepository.findById(id.value())).thenReturn(Optional.empty());

            Optional<Payment> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findById(id.value());
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            PaymentEntity entity = PaymentJpaMapper.toEntity(
                    Payment.reconstitute(
                            PaymentId.generate(),
                            PaymentNumber.of(2026, 1),
                            UUID.randomUUID(),
                            new BigDecimal("500.00"),
                            UUID.randomUUID(),
                            PaymentSource.PAYMENT_METHOD,
                            "REF-001",
                            LocalDate.now(),
                            "Test payment",
                            PaymentStatus.APPLIED,
                            BigDecimal.ZERO,
                            UUID.randomUUID(),
                            "John Doe",
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of(entity));

            List<Payment> result = adapter.findAllWithFilters(null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(jpaRepository).findAllWithFilters(null, null, null, null);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithFilters(any(), any(), any(), any()))
                    .thenReturn(List.of());

            List<Payment> result = adapter.findAllWithFilters(null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findAllWithFilters(null, null, null, null);
        }
    }

    @Nested
    @DisplayName("GetRefundableAmountByCustomerId")
    class GetRefundableAmountByCustomerId {

        @Test
        void should_DelegateToJpaRepository() {
            UUID customerId = UUID.randomUUID();
            BigDecimal expected = new BigDecimal("1500.00");
            when(jpaRepository.getRefundableAmountByCustomerId(customerId)).thenReturn(expected);

            BigDecimal result = adapter.getRefundableAmountByCustomerId(customerId);

            assertEquals(expected, result);
            verify(jpaRepository).getRefundableAmountByCustomerId(customerId);
        }
    }

    @Nested
    @DisplayName("DeleteById")
    class DeleteById {

        @Test
        void should_DelegateToJpaRepository() {
            PaymentId id = PaymentId.generate();

            adapter.deleteById(id);

            verify(jpaRepository).deleteById(id.value());
        }
    }

    @Nested
    @DisplayName("TargetInvoiceAndVoucherRoundTrip")
    class TargetInvoiceAndVoucherRoundTrip {

        @Test
        void should_PersistTargetInvoiceIdAndVoucherPath_When_Saving() {
            UUID targetInvoiceId = UUID.randomUUID();
            String voucherPath = "payments/justificantes/abc.pdf";
            Payment payment = Payment.create(
                    PaymentNumber.of(2026, 1),
                    UUID.randomUUID(),
                    new BigDecimal("500.00"),
                    UUID.randomUUID(),
                    "REF-001",
                    LocalDate.now(),
                    "Test payment",
                    UUID.randomUUID(),
                    "John Doe",
                    PaymentStatus.PENDING,
                    targetInvoiceId,
                    voucherPath
            );

            PaymentEntity expectedEntity = PaymentJpaMapper.toEntity(payment);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            adapter.save(payment);

            PaymentEntity captured = entityCaptor.getValue();
            assertEquals(targetInvoiceId, captured.getTargetInvoiceId());
            assertEquals(voucherPath, captured.getVoucherPath());
        }

        @Test
        void should_ReturnDomainWithTargetInvoiceIdAndVoucherPath_When_FindingById() {
            PaymentId id = PaymentId.generate();
            UUID targetInvoiceId = UUID.randomUUID();
            String voucherPath = "payments/justificantes/abc.pdf";
            Payment payment = Payment.reconstitute(
                    id, PaymentNumber.of(2026, 1), UUID.randomUUID(),
                    new BigDecimal("500.00"), UUID.randomUUID(), PaymentSource.PAYMENT_METHOD,
                    "REF-001", LocalDate.now(), "Test payment",
                    PaymentStatus.APPLIED, BigDecimal.ZERO,
                    UUID.randomUUID(), "John Doe",
                    LocalDateTime.now(), LocalDateTime.now(),
                    null, null, null, null,
                    targetInvoiceId, voucherPath
            );
            PaymentEntity entity = PaymentJpaMapper.toEntity(payment);
            when(jpaRepository.findById(id.value())).thenReturn(Optional.of(entity));

            Optional<Payment> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(targetInvoiceId, result.get().getTargetInvoiceId());
            assertEquals(voucherPath, result.get().getVoucherPath());
        }

        @Test
        void should_ReturnDomainWithNullTargetInvoiceAndVoucher_When_NotSet() {
            PaymentId id = PaymentId.generate();
            Payment payment = Payment.reconstitute(
                    id, PaymentNumber.of(2026, 1), UUID.randomUUID(),
                    new BigDecimal("500.00"), UUID.randomUUID(), PaymentSource.PAYMENT_METHOD,
                    "REF-001", LocalDate.now(), "Test payment",
                    PaymentStatus.APPLIED, BigDecimal.ZERO,
                    UUID.randomUUID(), "John Doe",
                    LocalDateTime.now(), LocalDateTime.now()
            );
            PaymentEntity entity = PaymentJpaMapper.toEntity(payment);
            when(jpaRepository.findById(id.value())).thenReturn(Optional.of(entity));

            Optional<Payment> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertNull(result.get().getTargetInvoiceId());
            assertNull(result.get().getVoucherPath());
        }
    }

    @Nested
    @DisplayName("GetNextSequenceForYear")
    class GetNextSequenceForYear {

        @Test
        void should_ReturnCountPlusOne_When_YearPaymentsExist() {
            int year = LocalDate.now().getYear();

            PaymentEntity entity = PaymentJpaMapper.toEntity(
                    Payment.reconstitute(
                            PaymentId.generate(),
                            PaymentNumber.of(year, 1),
                            UUID.randomUUID(),
                            new BigDecimal("500.00"),
                            UUID.randomUUID(),
                            PaymentSource.PAYMENT_METHOD,
                            "REF-001",
                            LocalDate.now(),
                            "Test payment",
                            PaymentStatus.APPLIED,
                            BigDecimal.ZERO,
                            UUID.randomUUID(),
                            "John Doe",
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );

            when(jpaRepository.findAllWithFilters(
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31),
                    null, null))
                    .thenReturn(List.of(entity));

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(2, next);
            verify(jpaRepository).findAllWithFilters(
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31),
                    null, null);
        }

        @Test
        void should_ReturnOne_When_NoYearPayments() {
            int year = LocalDate.now().getYear();

            when(jpaRepository.findAllWithFilters(
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31),
                    null, null))
                    .thenReturn(List.of());

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(1, next);
            verify(jpaRepository).findAllWithFilters(
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31),
                    null, null);
        }
    }
}
