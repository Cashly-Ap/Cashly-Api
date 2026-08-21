package com.parsun.parsun_api.customers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEntity;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaCustomerRepository Unit Tests")
class JpaCustomerRepositoryTest {

    @Mock
    private SpringDataCustomerRepository springDataRepository;

    @Captor
    private ArgumentCaptor<CustomerEntity> entityCaptor;

    private JpaCustomerRepository adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaCustomerRepository(springDataRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_CreateNewCustomer_When_NotExisting() {
            Customer customer = createTestCustomer();

            when(springDataRepository.findById(customer.getId().getValue())).thenReturn(Optional.empty());

            CustomerEntity savedEntity = CustomerEntity.fromDomain(customer);
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            Customer result = adapter.save(customer);

            assertNotNull(result);
            assertEquals(customer.getId(), result.getId());
            assertEquals(customer.getLegalName(), result.getLegalName());
            assertEquals(customer.getRif(), result.getRif());
            assertEquals(customer.getAlias(), result.getAlias());

            CustomerEntity captured = entityCaptor.getValue();
            assertEquals(customer.getId().getValue(), captured.getId());
            assertEquals(customer.getCode().getValue(), captured.getCode());
            assertEquals(customer.getLegalName().getValue(), captured.getLegalName());

            verify(springDataRepository).findById(customer.getId().getValue());
            verify(springDataRepository).save(any(CustomerEntity.class));
        }

        @Test
        void should_UpdateExistingCustomer_When_AlreadyExists() {
            Customer customer = createTestCustomer();
            CustomerEntity existingEntity = CustomerEntity.fromDomain(customer);

            when(springDataRepository.findById(customer.getId().getValue())).thenReturn(Optional.of(existingEntity));
            when(springDataRepository.save(entityCaptor.capture())).thenReturn(existingEntity);

            Customer result = adapter.save(customer);

            assertNotNull(result);
            assertEquals(customer.getId(), result.getId());

            CustomerEntity captured = entityCaptor.getValue();
            assertEquals(customer.getLegalName().getValue(), captured.getLegalName());
            assertEquals(customer.getRif().getValue(), captured.getRif());

            verify(springDataRepository).findById(customer.getId().getValue());
            verify(springDataRepository).save(any(CustomerEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            Customer customer = createTestCustomer();
            CustomerEntity entity = CustomerEntity.fromDomain(customer);

            when(springDataRepository.findById(customer.getId().getValue())).thenReturn(Optional.of(entity));

            Optional<Customer> result = adapter.findById(customer.getId());

            assertTrue(result.isPresent());
            assertEquals(customer.getId(), result.get().getId());
            assertEquals(customer.getLegalName(), result.get().getLegalName());
            verify(springDataRepository).findById(customer.getId().getValue());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            CustomerId id = CustomerId.generate();
            when(springDataRepository.findById(id.getValue())).thenReturn(Optional.empty());

            Optional<Customer> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(springDataRepository).findById(id.getValue());
        }
    }

    @Nested
    @DisplayName("FindActivePortalCustomerByRif")
    class FindActivePortalCustomerByRif {

        @Test
        void should_ReturnActivePortalCustomer() {
            Customer customer = createTestCustomer();
            CustomerEntity entity = CustomerEntity.fromDomain(customer);

            when(springDataRepository
                    .findByRifAndActiveTrueAndPortalAccessTrue(
                            customer.getRif().getValue()))
                    .thenReturn(Optional.of(entity));

            Optional<Customer> result = adapter.findActivePortalCustomerByRif(customer.getRif());

            assertTrue(result.isPresent());
            assertEquals(customer.getId(), result.get().getId());
            verify(springDataRepository)
                    .findByRifAndActiveTrueAndPortalAccessTrue(
                            customer.getRif().getValue());
        }
    }

    @Nested
    @DisplayName("Rif uniqueness")
    class RifUniqueness {

        @Test
        void should_CheckWhetherRifExists() {
            Rif rif = new Rif("J-12345678-9");
            when(springDataRepository.existsByRif(rif.getValue())).thenReturn(true);

            assertTrue(adapter.existsByRif(rif));

            verify(springDataRepository).existsByRif(rif.getValue());
        }

        @Test
        void should_ExcludeCurrentCustomer_WhenCheckingRif() {
            Rif rif = new Rif("J-12345678-9");
            CustomerId customerId = CustomerId.generate();
            when(springDataRepository.existsByRifAndIdNot(rif.getValue(), customerId.getValue()))
                    .thenReturn(false);

            assertFalse(adapter.existsByRifAndIdNot(rif, customerId));

            verify(springDataRepository).existsByRifAndIdNot(rif.getValue(), customerId.getValue());
        }
    }

    @Nested
    @DisplayName("FindAll")
    class FindAll {

        @Test
        void should_ReturnAllCustomers() {
            Customer customer = createTestCustomer();
            CustomerEntity entity = CustomerEntity.fromDomain(customer);

            when(springDataRepository.findAll()).thenReturn(List.of(entity));

            List<Customer> result = adapter.findAll();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(customer.getId(), result.get(0).getId());
            verify(springDataRepository).findAll();
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnPagedResults_When_EntitiesFound() {
            Customer customer = createTestCustomer();
            Page<CustomerEntity> entityPage = mock(Page.class);
            Page<Customer> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(customer));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Customer> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, null, null, 0, 20, "legalName", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.content().size());
            assertEquals(customer.getId(), result.content().get(0).getId());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_ReturnEmpty_When_NoEntitiesFound() {
            Page<CustomerEntity> entityPage = mock(Page.class);
            Page<Customer> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of());
            when(mappedPage.getTotalElements()).thenReturn(0L);
            when(mappedPage.getTotalPages()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(true);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Customer> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, null, null, 0, 20, "legalName", "asc");

            assertNotNull(result);
            assertTrue(result.empty());
            assertTrue(result.content().isEmpty());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_FallbackToLegalName_When_InvalidOrderByProvided() {
            Customer customer = createTestCustomer();
            Page<CustomerEntity> entityPage = mock(Page.class);
            Page<Customer> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(customer));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Customer> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, null, null, 0, 20, "invalidField", "asc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class));
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_UseDescendingSort_When_DescDirectionProvided() {
            Customer customer = createTestCustomer();
            Page<CustomerEntity> entityPage = mock(Page.class);
            Page<Customer> mappedPage = mock(Page.class);

            when(entityPage.map(any(Function.class))).thenReturn(mappedPage);
            when(mappedPage.getContent()).thenReturn(List.of(customer));
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.getSize()).thenReturn(20);
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            when(mappedPage.isEmpty()).thenReturn(false);

            when(springDataRepository.findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class)))
                    .thenReturn(entityPage);

            PageResponse<Customer> result = adapter.findAllWithFilters(
                    null, null, null, null, null, null, null, null, null, 0, 20, "legalName", "desc");

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(customer.getId(), result.content().get(0).getId());
            verify(springDataRepository).findAllWithFilters(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("ExistsById")
    class ExistsById {

        @Test
        void should_ReturnTrue_When_CustomerExists() {
            CustomerId id = CustomerId.generate();
            when(springDataRepository.existsById(id.getValue())).thenReturn(true);

            boolean result = adapter.existsById(id);

            assertTrue(result);
            verify(springDataRepository).existsById(id.getValue());
        }

        @Test
        void should_ReturnFalse_When_CustomerDoesNotExist() {
            CustomerId id = CustomerId.generate();
            when(springDataRepository.existsById(id.getValue())).thenReturn(false);

            boolean result = adapter.existsById(id);

            assertFalse(result);
            verify(springDataRepository).existsById(id.getValue());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToSpringDataRepository() {
            CustomerId id = CustomerId.generate();

            adapter.delete(id);

            verify(springDataRepository).deleteById(id.getValue());
        }
    }

    @Nested
    @DisplayName("GetNextCode")
    class GetNextCode {

        @Test
        void should_ReturnMaxCodePlusOne_When_MaxCodeExists() {
            when(springDataRepository.findMaxCode()).thenReturn(Optional.of(42L));

            long next = adapter.getNextCode();

            assertEquals(43L, next);
            verify(springDataRepository).findMaxCode();
        }

        @Test
        void should_ReturnOne_When_NoMaxCode() {
            when(springDataRepository.findMaxCode()).thenReturn(Optional.empty());

            long next = adapter.getNextCode();

            assertEquals(1L, next);
            verify(springDataRepository).findMaxCode();
        }
    }

    private Customer createTestCustomer() {
        return Customer.reconstitute(
                CustomerId.generate(),
                new CustomerCode(1L),
                new LegalName("Test Customer S.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("TestCustomer"),
                new CreditDays(30),
                new CreditLimit(new BigDecimal("50000.00")),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of(),
                false,
                null,
                null,
                null
        );
    }
}
