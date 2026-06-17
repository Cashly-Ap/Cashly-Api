package com.parsun.parsun_api.receivables.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.domain.entities.CustomerBalance;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEmailEntity;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEntity;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerPhoneEntity;
import com.parsun.parsun_api.receivables.application.dto.response.CustomerDebtListResponse;
import com.parsun.parsun_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.parsun.parsun_api.receivables.application.dto.response.CustomerInvoiceDebtResponse;
import com.parsun.parsun_api.receivables.application.dto.response.PendingPaymentResponse;
import com.parsun.parsun_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceivablesRepositoryAdapter Unit Tests")
class ReceivablesRepositoryAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ReceivablesJpaRepository receivablesJpaRepository;

    @Mock
    private CustomerBalanceRepository customerBalanceRepository;

    @Mock
    private Query nativeQuery;

    @Mock
    private CustomerEntity customerEntity;

    private ReceivablesRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ReceivablesRepositoryAdapter(entityManager, receivablesJpaRepository, customerBalanceRepository);
    }

    @Nested
    @DisplayName("findSummary")
    class FindSummary {

        @BeforeEach
        void setUp() {
            lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        }

        @Test
        @DisplayName("should return summary with all calculated values")
        void shouldReturnSummaryWithAllCalculatedValues() {
            when(receivablesJpaRepository.findTotalDebt()).thenReturn(new BigDecimal("50000.00"));
            when(receivablesJpaRepository.countCustomersWithDebt()).thenReturn(25L);
            when(receivablesJpaRepository.countInvoicesWithDebt()).thenReturn(100L);
            when(receivablesJpaRepository.findTotalPendingValidationAmount()).thenReturn(new BigDecimal("10000.00"));
            doReturn(new BigDecimal("75000.00")).when(nativeQuery).getSingleResult();

            ReceivablesSummaryResponse result = adapter.findSummary();

            assertThat(result.totalDebt()).isEqualByComparingTo(new BigDecimal("50000.00"));
            assertThat(result.totalCustomersWithDebt()).isEqualTo(25L);
            assertThat(result.totalInvoicesWithDebt()).isEqualTo(100L);
            assertThat(result.totalPendingValidationAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(result.totalCustomerBalance()).isEqualByComparingTo(new BigDecimal("75000.00"));
            assertThat(result.calculatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should default to ZERO when jpa queries return null")
        void shouldDefaultToZeroWhenJpaQueriesReturnNull() {
            when(receivablesJpaRepository.findTotalDebt()).thenReturn(null);
            when(receivablesJpaRepository.countCustomersWithDebt()).thenReturn(0L);
            when(receivablesJpaRepository.countInvoicesWithDebt()).thenReturn(0L);
            when(receivablesJpaRepository.findTotalPendingValidationAmount()).thenReturn(null);
            doReturn(null).when(nativeQuery).getSingleResult();

            ReceivablesSummaryResponse result = adapter.findSummary();

            assertThat(result.totalDebt()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalCustomersWithDebt()).isZero();
            assertThat(result.totalInvoicesWithDebt()).isZero();
            assertThat(result.totalPendingValidationAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalCustomerBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("findCustomerDebt")
    class FindCustomerDebt {

        private final UUID customerId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
            lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            lenient().when(nativeQuery.setParameter(anyString(), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(nativeQuery);
        }

        @Test
        @DisplayName("should return full debt summary for existing customer with invoices and balance")
        void shouldReturnFullDebtSummaryForExistingCustomer() {
            CustomerPhoneEntity phone = new CustomerPhoneEntity();
            phone.setPhone("+584141234567");
            CustomerEmailEntity email = new CustomerEmailEntity();
            email.setEmail("test@example.com");

            when(customerEntity.getLegalName()).thenReturn("Test Customer S.A.");
            when(customerEntity.getRif()).thenReturn("J-12345678-9");
            when(customerEntity.getPhones()).thenReturn(List.of(phone));
            when(customerEntity.getEmails()).thenReturn(List.of(email));
            when(customerEntity.getCreditLimit()).thenReturn(new BigDecimal("100000.00"));
            when(entityManager.find(CustomerEntity.class, customerId)).thenReturn(customerEntity);

            doReturn(new BigDecimal("30000.00")).when(nativeQuery).getSingleResult();

            UUID invoiceId = UUID.randomUUID();
            Timestamp invoiceDate = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
            Object[] invoiceRow = new Object[]{
                invoiceId, "FAC-001", "PENDING_PAYMENT", invoiceDate,
                BigDecimal.valueOf(15000), BigDecimal.ZERO, BigDecimal.valueOf(5000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(15000), BigDecimal.valueOf(10000),
                null, null
            };
            doReturn(Collections.singletonList(invoiceRow)).when(nativeQuery).getResultList();

            CustomerBalance balance = CustomerBalance.reconstitute(
                    UUID.randomUUID(), customerId, BigDecimal.valueOf(5000),
                    LocalDateTime.now(), LocalDateTime.now()
            );
            when(customerBalanceRepository.findByCustomerId(customerId))
                    .thenReturn(Optional.of(balance));

            CustomerDebtSummaryResponse result = adapter.findCustomerDebt(customerId);

            assertThat(result.customerId()).isEqualTo(customerId);
            assertThat(result.customerName()).isEqualTo("Test Customer S.A.");
            assertThat(result.customerRif()).isEqualTo("J-12345678-9");
            assertThat(result.customerPhone()).isEqualTo("+584141234567");
            assertThat(result.customerEmail()).isEqualTo("test@example.com");

            assertThat(result.totalDebt()).isEqualByComparingTo(new BigDecimal("30000.00"));
            assertThat(result.creditLimit()).isEqualByComparingTo(new BigDecimal("100000.00"));
            assertThat(result.availableDebt()).isEqualByComparingTo(new BigDecimal("75000.00"));
            assertThat(result.availableCredit()).isEqualByComparingTo(new BigDecimal("75000.00"));
            assertThat(result.customerBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));

            assertThat(result.invoicesWithDebt()).hasSize(1);
            CustomerInvoiceDebtResponse invoice = result.invoicesWithDebt().get(0);
            assertThat(invoice.invoiceId()).isEqualTo(invoiceId);
            assertThat(invoice.invoiceNumber()).isEqualTo("FAC-001");
            assertThat(invoice.status()).isEqualTo("PENDING_PAYMENT");
            assertThat(invoice.subtotal()).isEqualByComparingTo(new BigDecimal("15000"));
            assertThat(invoice.balance()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(invoice.pendingPaymentId()).isNull();
            assertThat(invoice.pendingPaymentStatus()).isNull();
        }

        @Test
        @DisplayName("should handle customer not found gracefully")
        void shouldHandleCustomerNotFound() {
            when(entityManager.find(CustomerEntity.class, customerId)).thenReturn(null);

            doReturn(new BigDecimal("30000.00")).when(nativeQuery).getSingleResult();
            doReturn(Collections.emptyList()).when(nativeQuery).getResultList();

            when(customerBalanceRepository.findByCustomerId(customerId))
                    .thenReturn(Optional.empty());

            CustomerDebtSummaryResponse result = adapter.findCustomerDebt(customerId);

            assertThat(result.customerId()).isEqualTo(customerId);
            assertThat(result.customerName()).isNull();
            assertThat(result.customerRif()).isNull();
            assertThat(result.customerPhone()).isNull();
            assertThat(result.customerEmail()).isNull();
            assertThat(result.creditLimit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalDebt()).isEqualByComparingTo(new BigDecimal("30000.00"));
            assertThat(result.availableDebt()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.customerBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.invoicesWithDebt()).isEmpty();
        }

        @Test
        @DisplayName("should default to ZERO when totalDebt query returns null")
        void shouldDefaultToZeroWhenTotalDebtQueryReturnsNull() {
            CustomerPhoneEntity phone = new CustomerPhoneEntity();
            phone.setPhone("+584141234567");
            CustomerEmailEntity email = new CustomerEmailEntity();
            email.setEmail("test@example.com");

            when(customerEntity.getLegalName()).thenReturn("Test Customer");
            when(customerEntity.getRif()).thenReturn("J-00000000-0");
            when(customerEntity.getPhones()).thenReturn(List.of(phone));
            when(customerEntity.getEmails()).thenReturn(List.of(email));
            when(customerEntity.getCreditLimit()).thenReturn(new BigDecimal("100000.00"));
            when(entityManager.find(CustomerEntity.class, customerId)).thenReturn(customerEntity);

            doReturn(null).when(nativeQuery).getSingleResult();

            UUID invoiceId = UUID.randomUUID();
            Timestamp invoiceDate = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
            Object[] invoiceRow = new Object[]{
                invoiceId, "FAC-001", "PENDING_PAYMENT", invoiceDate,
                BigDecimal.valueOf(15000), BigDecimal.ZERO, BigDecimal.valueOf(5000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(15000), BigDecimal.valueOf(10000),
                null, null
            };
            doReturn(Collections.singletonList(invoiceRow)).when(nativeQuery).getResultList();

            when(customerBalanceRepository.findByCustomerId(customerId))
                    .thenReturn(Optional.empty());

            CustomerDebtSummaryResponse result = adapter.findCustomerDebt(customerId);

            assertThat(result.totalDebt()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.availableDebt()).isEqualByComparingTo(new BigDecimal("100000.00"));
        }

        @Test
        @DisplayName("should handle null invoice date and empty collections")
        void shouldHandleNullInvoiceDateAndEmptyCollections() {
            when(customerEntity.getLegalName()).thenReturn("Edge Case Customer");
            when(customerEntity.getRif()).thenReturn("J-00000000-0");
            when(customerEntity.getPhones()).thenReturn(Collections.emptyList());
            when(customerEntity.getEmails()).thenReturn(Collections.emptyList());
            when(customerEntity.getCreditLimit()).thenReturn(BigDecimal.ZERO);
            when(entityManager.find(CustomerEntity.class, customerId)).thenReturn(customerEntity);

            doReturn(BigDecimal.ZERO).when(nativeQuery).getSingleResult();

            UUID invoiceId = UUID.randomUUID();
            Object[] invoiceRow = new Object[]{
                invoiceId, "FAC-NULL-DATE", "PENDING_PAYMENT", null,
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(5000),
                null, null
            };
            doReturn(Collections.singletonList(invoiceRow)).when(nativeQuery).getResultList();

            when(customerBalanceRepository.findByCustomerId(customerId))
                    .thenReturn(Optional.empty());

            CustomerDebtSummaryResponse result = adapter.findCustomerDebt(customerId);

            assertThat(result.customerId()).isEqualTo(customerId);
            assertThat(result.customerName()).isEqualTo("Edge Case Customer");
            assertThat(result.customerPhone()).isNull();
            assertThat(result.customerEmail()).isNull();
            assertThat(result.totalDebt()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.creditLimit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.customerBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.invoicesWithDebt()).hasSize(1);
            assertThat(result.invoicesWithDebt().get(0).invoiceDate()).isNull();
        }
    }

    @Nested
    @DisplayName("findCustomersWithDebt")
    class FindCustomersWithDebt {

        @BeforeEach
        void setUp() {
            lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            lenient().when(nativeQuery.setParameter(anyString(), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setFirstResult(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setMaxResults(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
        }

        @Test
        @DisplayName("should return paginated customers with debt data")
        void shouldReturnPaginatedCustomersWithDebtData() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer Uno", "J-11111111-1",
                BigDecimal.valueOf(25000), BigDecimal.valueOf(3000), 8L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(3)),
                BigDecimal.valueOf(100000), BigDecimal.valueOf(80000), BigDecimal.valueOf(5000)
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(42L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, null);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(42L);

            CustomerDebtListResponse dto = result.getContent().get(0);
            assertThat(dto.customerId()).isEqualTo(custId);
            assertThat(dto.customerName()).isEqualTo("Customer Uno");
            assertThat(dto.customerRif()).isEqualTo("J-11111111-1");
            assertThat(dto.totalDebt()).isEqualByComparingTo(new BigDecimal("25000"));
            assertThat(dto.pendingPaymentsAmount()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(dto.pendingInvoicesCount()).isEqualTo(8L);
            assertThat(dto.oldestInvoiceDate()).isEqualTo(LocalDate.now().minusMonths(3));
            assertThat(dto.creditLimit()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(dto.availableCredit()).isEqualByComparingTo(new BigDecimal("80000"));
            assertThat(dto.customerBalance()).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("should return empty page when no customers with debt")
        void shouldReturnEmptyPageWhenNoCustomers() {
            doReturn(Collections.emptyList()).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(0L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, null);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("should apply search filter when provided")
        void shouldApplySearchFilterWhenProvided() {
            Object[] row = new Object[]{
                UUID.randomUUID(), "Customer Dos", "J-22222222-2",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), 3L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(1)),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(45000), BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countCustomersWithDebtBySearch("Customer")).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt("Customer", 0, 10, null);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getContent().get(0).customerName()).isEqualTo("Customer Dos");
        }

        @Test
        @DisplayName("should handle null oldest invoice date")
        void shouldHandleNullOldestInvoiceDate() {
            Object[] row = new Object[]{
                UUID.randomUUID(), "Customer Tres", "J-33333333-3",
                BigDecimal.valueOf(5000), BigDecimal.ZERO, 1L,
                null,
                BigDecimal.valueOf(20000), BigDecimal.valueOf(15000), BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, null);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).oldestInvoiceDate()).isNull();
        }

        @Test
        @DisplayName("should apply sort by customer name ascending")
        void shouldApplySortByCustomerName() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer Alpha", "J-AAA-1",
                BigDecimal.valueOf(15000), BigDecimal.valueOf(2000), 5L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(2)),
                BigDecimal.valueOf(80000), BigDecimal.valueOf(70000), BigDecimal.valueOf(10000)
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "customerName,asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).customerName()).isEqualTo("Customer Alpha");
        }

        @Test
        @DisplayName("should apply sort by total debt descending")
        void shouldApplySortByTotalDebtDesc() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer Beta", "J-BBB-2",
                BigDecimal.valueOf(50000), BigDecimal.valueOf(5000), 12L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(6)),
                BigDecimal.valueOf(100000), BigDecimal.valueOf(55000), BigDecimal.valueOf(5000)
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "totalDebt,desc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).totalDebt()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("should apply sort by credit limit ascending")
        void shouldApplySortByCreditLimit() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer Gamma", "J-GGG-3",
                BigDecimal.valueOf(30000), BigDecimal.valueOf(1000), 4L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(1)),
                BigDecimal.valueOf(60000), BigDecimal.valueOf(35000), BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "creditLimit,asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).creditLimit()).isEqualByComparingTo(new BigDecimal("60000"));
        }

        @Test
        @DisplayName("should apply sort by available credit ascending")
        void shouldApplySortByAvailableCredit() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer Delta", "J-DDD-4",
                BigDecimal.valueOf(20000), BigDecimal.valueOf(500), 3L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(2)),
                BigDecimal.valueOf(100000), BigDecimal.valueOf(80000), BigDecimal.valueOf(3000)
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "availableCredit,asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).availableCredit()).isEqualByComparingTo(new BigDecimal("80000"));
        }

        @Test
        @DisplayName("should apply sort by pending invoices count descending")
        void shouldApplySortByPendingInvoicesCount() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer Epsilon", "J-EEE-5",
                BigDecimal.valueOf(35000), BigDecimal.valueOf(2000), 7L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(4)),
                BigDecimal.valueOf(90000), BigDecimal.valueOf(60000), BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "pendingInvoicesCount,desc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).pendingInvoicesCount()).isEqualTo(7L);
        }

        @Test
        @DisplayName("should convert byte array to UUID via toUuid")
        void shouldConvertByteArrayToUuid() {
            UUID custId = UUID.randomUUID();
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.putLong(custId.getMostSignificantBits());
            bb.putLong(custId.getLeastSignificantBits());
            byte[] custIdBytes = bb.array();

            Object[] row = new Object[]{
                custIdBytes, "Customer Byte", "J-BYT-1",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(500), 2L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(1)),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(40000), BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "customerName,asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).customerId()).isEqualTo(custId);
        }

        @Test
        @DisplayName("should default to ascending when sort has no direction")
        void shouldDefaultToAscendingWhenSortHasNoDirection() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer No Dir", "J-NODIR-1",
                BigDecimal.valueOf(5000), BigDecimal.ZERO, 1L,
                null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "customerName");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).customerId()).isEqualTo(custId);
        }

        @Test
        @DisplayName("should default to totalDebt DESC when sort is blank")
        void shouldDefaultToTotalDebtDescWhenSortIsBlank() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Customer Blank Sort", "J-BLANK-1",
                BigDecimal.valueOf(5000), BigDecimal.ZERO, 1L,
                null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(null, 0, 10, "");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).customerId()).isEqualTo(custId);
        }

        @Test
        @DisplayName("should default to totalDebt when sort field is unknown")
        void shouldDefaultToTotalDebtWhenSortFieldIsUnknown() {
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                custId, "Unknown Field", "J-UNKN-1",
                BigDecimal.valueOf(5000), BigDecimal.ZERO, 1L,
                java.sql.Date.valueOf(LocalDate.now().minusMonths(1)),
                null, null, null
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            Page<CustomerDebtListResponse> result = adapter.findCustomersWithDebt(
                    null, 0, 10, "nonexistentField,asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).customerId()).isEqualTo(custId);
            assertThat(result.getContent().get(0).creditLimit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getContent().get(0).availableCredit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getContent().get(0).customerBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when toUuid receives unsupported type")
        void shouldThrowWhenToUuidReceivesUnsupportedType() {
            Object[] row = new Object[]{
                "not-a-uuid-string", "Bad Customer", "J-BAD-1",
                BigDecimal.valueOf(100), BigDecimal.ZERO, 1L,
                null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            };
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();
            when(receivablesJpaRepository.countAllCustomers()).thenReturn(1L);

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.findCustomersWithDebt(null, 0, 10, "customerName,asc")
            );
        }
    }

    @Nested
    @DisplayName("findCustomerInvoices")
    class FindCustomerInvoices {

        private final UUID customerId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
            lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            lenient().when(nativeQuery.setParameter(anyString(), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setFirstResult(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setMaxResults(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
        }

        @Test
        @DisplayName("should return paginated invoices with debt status")
        void shouldReturnPaginatedInvoicesWithDebtStatus() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-100", "PENDING_PAYMENT",
                Timestamp.valueOf(LocalDateTime.now().minusDays(10)),
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.valueOf(1000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(4000),
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, null, "PENDING", 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);

            CustomerInvoiceDebtResponse dto = result.getContent().get(0);
            assertThat(dto.invoiceId()).isEqualTo(invId);
            assertThat(dto.invoiceNumber()).isEqualTo("FAC-100");
            assertThat(dto.status()).isEqualTo("PENDING_PAYMENT");
            assertThat(dto.subtotal()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(dto.balance()).isEqualByComparingTo(new BigDecimal("4000"));
        }

        @Test
        @DisplayName("should filter by returned status")
        void shouldFilterByReturnedStatus() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-200", "FULLY_RETURNED",
                Timestamp.valueOf(LocalDateTime.now().minusDays(5)),
                BigDecimal.valueOf(3000), BigDecimal.valueOf(3000), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, null, "RETURNED", 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo("FULLY_RETURNED");
        }

        @Test
        @DisplayName("should filter by search term")
        void shouldFilterBySearchTerm() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-300", "PAID",
                Timestamp.valueOf(LocalDateTime.now().minusDays(3)),
                BigDecimal.valueOf(2000), BigDecimal.ZERO, BigDecimal.valueOf(2000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(2000), BigDecimal.ZERO,
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, "300", "PAID", 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).invoiceNumber()).isEqualTo("FAC-300");
            assertThat(result.getContent().get(0).status()).isEqualTo("PAID");
            assertThat(result.getContent().get(0).balance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should filter by cancelled status")
        void shouldFilterByCancelledStatus() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-400", "CANCELLED",
                Timestamp.valueOf(LocalDateTime.now().minusDays(2)),
                BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(1000),
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, null, "CANCELLED", 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).invoiceNumber()).isEqualTo("FAC-400");
            assertThat(result.getContent().get(0).status()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("should filter by partial return status")
        void shouldFilterByPartialReturnStatus() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-500", "PENDING_PAYMENT",
                Timestamp.valueOf(LocalDateTime.now().minusDays(7)),
                BigDecimal.valueOf(5000), BigDecimal.valueOf(2000), BigDecimal.valueOf(2000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(3000), BigDecimal.valueOf(1000),
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, null, "PARTIAL_RETURN", 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).invoiceNumber()).isEqualTo("FAC-500");
        }

        @Test
        @DisplayName("should filter by partial payment status")
        void shouldFilterByPartialStatus() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-600", "PARTIALLY_PAID",
                Timestamp.valueOf(LocalDateTime.now().minusDays(14)),
                BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.valueOf(3000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000),
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, null, "PARTIAL", 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).invoiceNumber()).isEqualTo("FAC-600");
            assertThat(result.getContent().get(0).status()).isEqualTo("PARTIALLY_PAID");
            assertThat(result.getContent().get(0).balance()).isEqualByComparingTo(new BigDecimal("7000"));
        }

        @Test
        @DisplayName("should use default balance > 0 filter when unknown status is provided")
        void shouldUseDefaultStatusFilter_When_UnknownStatus() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-700", "SOME_STATUS",
                Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
                BigDecimal.valueOf(2000), BigDecimal.ZERO, BigDecimal.valueOf(500),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(2000), BigDecimal.valueOf(1500),
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, null, "UNKNOWN", 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).invoiceNumber()).isEqualTo("FAC-700");
            assertThat(result.getContent().get(0).balance()).isEqualByComparingTo(new BigDecimal("1500"));
        }

        @Test
        @DisplayName("should use default balance > 0 filter when status is null with search")
        void shouldUseDefaultStatusFilter_When_NullStatus() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-800", "PENDING_PAYMENT",
                Timestamp.valueOf(LocalDateTime.now().minusDays(20)),
                BigDecimal.valueOf(8000), BigDecimal.ZERO, BigDecimal.valueOf(2000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(8000), BigDecimal.valueOf(6000),
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, "800", null, 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).invoiceNumber()).isEqualTo("FAC-800");
            assertThat(result.getContent().get(0).balance()).isEqualByComparingTo(new BigDecimal("6000"));
        }

        @Test
        @DisplayName("should handle null invoice date in rows")
        void shouldHandleNullInvoiceDateInRows() {
            UUID invId = UUID.randomUUID();
            Object[] row = new Object[]{
                invId, "FAC-NULL-DT", "PENDING_PAYMENT", null,
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.valueOf(1000),
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(4000),
                null, null
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<CustomerInvoiceDebtResponse> result = adapter.findCustomerInvoices(
                    customerId, null, null, 0, 10
            );

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).invoiceDate()).isNull();
        }
    }

    @Nested
    @DisplayName("findCustomerPaymentHistory")
    class FindCustomerPaymentHistory {

        private final UUID customerId = UUID.randomUUID();

        @BeforeEach
        void setUp() {
            lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            lenient().when(nativeQuery.setParameter(anyString(), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setFirstResult(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setMaxResults(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
        }

        @Test
        @DisplayName("should return paginated payment history for customer")
        void shouldReturnPaginatedPaymentHistoryForCustomer() {
            UUID paymentId = UUID.randomUUID();
            Object[] row = new Object[]{
                paymentId, "PAG-001", customerId, "Customer Uno",
                BigDecimal.valueOf(2500), "Transferencia", "APPLIED", "MANUAL",
                Date.valueOf(LocalDate.now().minusDays(15)),
                Timestamp.valueOf(LocalDateTime.now().minusDays(15)),
                "FAC-100"
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<PendingPaymentResponse> result = adapter.findCustomerPaymentHistory(customerId, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);

            PendingPaymentResponse dto = result.getContent().get(0);
            assertThat(dto.id()).isEqualTo(paymentId);
            assertThat(dto.paymentNumber()).isEqualTo("PAG-001");
            assertThat(dto.customerId()).isEqualTo(customerId);
            assertThat(dto.customerName()).isEqualTo("Customer Uno");
            assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("2500"));
            assertThat(dto.paymentMethodName()).isEqualTo("Transferencia");
            assertThat(dto.status()).isEqualTo("APPLIED");
            assertThat(dto.source()).isEqualTo("MANUAL");
            assertThat(dto.paymentDate()).isEqualTo(LocalDate.now().minusDays(15));
            assertThat(dto.createdAt())
                    .isCloseTo(LocalDateTime.now().minusDays(15),
                            org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.SECONDS));
            assertThat(dto.invoiceNumber()).isEqualTo("FAC-100");
        }

        @Test
        @DisplayName("should filter by invoice id when provided")
        void shouldFilterByInvoiceIdWhenProvided() {
            UUID paymentId = UUID.randomUUID();
            Object[] row = new Object[]{
                paymentId, "PAG-002", customerId, "Customer Dos",
                BigDecimal.valueOf(5000), "Efectivo", "PENDING_APPROVAL", "MANUAL",
                Date.valueOf(LocalDate.now()),
                Timestamp.valueOf(LocalDateTime.now()),
                "FAC-200"
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            UUID invoiceId = UUID.randomUUID();
            Page<PendingPaymentResponse> result = adapter.findCustomerPaymentHistory(customerId, invoiceId, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).paymentNumber()).isEqualTo("PAG-002");
            assertThat(result.getContent().get(0).status()).isEqualTo("PENDING_APPROVAL");
        }

        @Test
        @DisplayName("should return empty page when no payments found")
        void shouldReturnEmptyPageWhenNoPayments() {
            doReturn(0L).when(nativeQuery).getSingleResult();
            doReturn(Collections.emptyList()).when(nativeQuery).getResultList();

            Page<PendingPaymentResponse> result = adapter.findCustomerPaymentHistory(customerId, null, 0, 10);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("findPaymentHistory")
    class FindPaymentHistory {

        @BeforeEach
        void setUp() {
            lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
            lenient().when(nativeQuery.setParameter(anyString(), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setFirstResult(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
            lenient().when(nativeQuery.setMaxResults(org.mockito.ArgumentMatchers.anyInt()))
                    .thenReturn(nativeQuery);
        }

        @Test
        @DisplayName("should return paginated payment history filtered by search")
        void shouldReturnPaginatedPaymentHistoryFilteredBySearch() {
            UUID paymentId = UUID.randomUUID();
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                paymentId, "PAG-010", custId, "Customer Tres",
                BigDecimal.valueOf(1200), "Zelle", "APPLIED", "PORTAL",
                Date.valueOf(LocalDate.now().minusDays(5)),
                Timestamp.valueOf(LocalDateTime.now().minusDays(5)),
                "FAC-300"
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<PendingPaymentResponse> result = adapter.findPaymentHistory("Customer Tres", 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);

            PendingPaymentResponse dto = result.getContent().get(0);
            assertThat(dto.id()).isEqualTo(paymentId);
            assertThat(dto.paymentNumber()).isEqualTo("PAG-010");
            assertThat(dto.customerId()).isEqualTo(custId);
            assertThat(dto.customerName()).isEqualTo("Customer Tres");
            assertThat(dto.amount()).isEqualByComparingTo(new BigDecimal("1200"));
            assertThat(dto.paymentMethodName()).isEqualTo("Zelle");
            assertThat(dto.status()).isEqualTo("APPLIED");
            assertThat(dto.source()).isEqualTo("PORTAL");
            assertThat(dto.paymentDate()).isEqualTo(LocalDate.now().minusDays(5));
            assertThat(dto.createdAt())
                    .isCloseTo(LocalDateTime.now().minusDays(5),
                            org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.SECONDS));
            assertThat(dto.invoiceNumber()).isEqualTo("FAC-300");
        }

        @Test
        @DisplayName("should return empty page when search matches nothing")
        void shouldReturnEmptyPageWhenNoMatch() {
            doReturn(0L).when(nativeQuery).getSingleResult();
            doReturn(Collections.emptyList()).when(nativeQuery).getResultList();

            Page<PendingPaymentResponse> result = adapter.findPaymentHistory("NONEXISTENT", 0, 10);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("should return all payments when search is null")
        void shouldReturnAllPaymentsWhenSearchIsNull() {
            UUID paymentId = UUID.randomUUID();
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                paymentId, "PAG-ALL", custId, "Customer All",
                BigDecimal.valueOf(999), "Cash", "APPLIED", "MANUAL",
                Date.valueOf(LocalDate.now()), Timestamp.valueOf(LocalDateTime.now()),
                "FAC-ALL"
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<PendingPaymentResponse> result = adapter.findPaymentHistory(null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("should handle null dates in payment rows")
        void shouldHandleNullDatesInPaymentRows() {
            UUID paymentId = UUID.randomUUID();
            UUID custId = UUID.randomUUID();
            Object[] row = new Object[]{
                paymentId, "PAG-NULL-DATE", custId, "Customer Null Date",
                BigDecimal.valueOf(1000), "Cash", "PENDING", "MANUAL",
                null,
                null,
                "FAC-NULL"
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Page<PendingPaymentResponse> result = adapter.findPaymentHistory("Customer Null Date", 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).paymentDate()).isNull();
            assertThat(result.getContent().get(0).createdAt()).isNull();
        }

        @Test
        @DisplayName("should filter payments by merchant via reflection")
        void shouldFilterPaymentsByMerchant() throws Exception {
            UUID merchantId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();
            UUID custId = UUID.randomUUID();

            Object[] row = new Object[]{
                paymentId, "PAG-MERCHANT", custId, "Merchant Customer",
                BigDecimal.valueOf(3000), "Transfer", "APPLIED", "MANUAL",
                Date.valueOf(LocalDate.now()), Timestamp.valueOf(LocalDateTime.now()),
                "FAC-MERCHANT"
            };

            doReturn(1L).when(nativeQuery).getSingleResult();
            doReturn(Collections.singletonList(row)).when(nativeQuery).getResultList();

            Method method = ReceivablesRepositoryAdapter.class.getDeclaredMethod(
                    "findPaymentsWithFilters", UUID.class, String.class, String.class, int.class, int.class, boolean.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            Page<PendingPaymentResponse> result = (Page<PendingPaymentResponse>) method.invoke(
                    adapter, merchantId, "APPLIED", null, 0, 10, true);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(paymentId);
            assertThat(result.getContent().get(0).paymentNumber()).isEqualTo("PAG-MERCHANT");
        }
    }
}
