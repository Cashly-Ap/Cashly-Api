package com.cashly.cashly_api.partners.customers.profile.infrastructure.web;

import com.cashly.cashly_api.partners.customers.profile.application.register.request.CreateCustomerRequest;
import com.cashly.cashly_api.partners.customers.profile.application.update.request.UpdateCustomerRequest;
import com.cashly.cashly_api.partners.customers.profile.application.register.response.CustomerCreateResponse;
import com.cashly.cashly_api.partners.customers.profile.application.dto.response.CustomerDetailResponse;
import com.cashly.cashly_api.partners.customers.profile.application.dto.response.CustomerListResponse;
import com.cashly.cashly_api.partners.customers.profile.application.delete.use_case.DeleteCustomerUseCase;
import com.cashly.cashly_api.partners.customers.profile.application.get.use_case.GetCustomerUseCase;
import com.cashly.cashly_api.partners.customers.profile.application.list.use_case.ListCustomersUseCase;
import com.cashly.cashly_api.partners.customers.profile.application.register.use_case.RegisterCustomerUseCase;
import com.cashly.cashly_api.partners.customers.profile.application.update.use_case.UpdateCustomerUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Unit Tests")
class CustomerControllerTest {

    @Mock
    private RegisterCustomerUseCase registerCustomerUseCase;

    @Mock
    private GetCustomerUseCase getCustomerUseCase;

    @Mock
    private ListCustomersUseCase listCustomersUseCase;

    @Mock
    private UpdateCustomerUseCase updateCustomerUseCase;

    @Mock
    private DeleteCustomerUseCase deleteCustomerUseCase;

    @InjectMocks
    private CustomerController customerController;

    @Test
    @DisplayName("should create customer and return 201")
    void should_create_customer_and_return_201() {
        String customerId = UUID.randomUUID().toString();
        UUID grantedBy = UUID.randomUUID();
        CustomerCreateResponse response = new CustomerCreateResponse(customerId,
                1001L, "Test Corp", "BUSINESS", "J-12345678-9", "Test", 30,
                java.math.BigDecimal.ZERO, true, null, LocalDateTime.now(),
                List.of(), List.of(), List.of(), null, null);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(grantedBy.toString());

        when(registerCustomerUseCase.execute(any(CreateCustomerRequest.class),
                any(UUID.class))).thenReturn(response);

        CreateCustomerRequest request = new CreateCustomerRequest("Test Corp",
                "BUSINESS", "J-12345678-9", "Test", 30,
                java.math.BigDecimal.ZERO, true, List.of(), List.of(),
                List.of(), List.of(UUID.randomUUID()), null, null);

        ResponseEntity<CustomerCreateResponse> result = customerController
                .create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(customerId, result.getBody().id());
        verify(registerCustomerUseCase)
                .execute(any(CreateCustomerRequest.class), eq(grantedBy));
    }

    @Test
    @DisplayName("should get all customers and return 200")
    void should_get_all_customers_and_return_200() {
        CustomerListResponse customer = new CustomerListResponse(
                UUID.randomUUID().toString(), 1001L, "Test", "Test Corp",
                "J-12345678-9", "BUSINESS", true, null, false);

        PageResponse<CustomerListResponse> pageResponse = new PageResponse<>(
                List.of(customer), 1L, 1, 10, 0, true, true, false);

        when(listCustomersUseCase.execute(any(), any(), any(), any(), any(),
                any(), any(), any(), anyInt(), anyInt(), anyString(),
                anyString(), any())).thenReturn(pageResponse);

        ResponseEntity<PageResponse<CustomerListResponse>> result = customerController
                .getAll(null, null, null, null, null, null, null, 0, 10,
                        "legalName", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().content().size());
        verify(listCustomersUseCase).execute(null, null, null, null, null, null,
                null, null, 0, 10, "legalName", "asc", null);
    }

    @Test
    @DisplayName("should get empty customers list")
    void should_get_empty_customers_list() {
        PageResponse<CustomerListResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(listCustomersUseCase.execute(any(), any(), any(), any(), any(),
                any(), any(), any(), anyInt(), anyInt(), anyString(),
                anyString(), any())).thenReturn(emptyPage);

        ResponseEntity<PageResponse<CustomerListResponse>> result = customerController
                .getAll(null, null, null, null, null, null, null, 0, 10,
                        "legalName", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().content().isEmpty());
        verify(listCustomersUseCase).execute(null, null, null, null, null, null,
                null, null, 0, 10, "legalName", "asc", null);
    }

    @Test
    @DisplayName("should get customer by id and return 200")
    void should_get_customer_by_id_and_return_200() {
        String id = UUID.randomUUID().toString();
        CustomerDetailResponse response = new CustomerDetailResponse(id, 1001L,
                "Test Corp", "BUSINESS", "J-12345678-9", "Test", 30,
                java.math.BigDecimal.ZERO, true, null, LocalDateTime.now(),
                List.of(), List.of(), List.of(), null, null, true);

        when(getCustomerUseCase.execute(id)).thenReturn(response);

        ResponseEntity<CustomerDetailResponse> result = customerController
                .getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getCustomerUseCase).execute(id);
    }

    @Test
    @DisplayName("should update customer and return 200")
    void should_update_customer_and_return_200() {
        String id = UUID.randomUUID().toString();
        CustomerDetailResponse response = new CustomerDetailResponse(id, 1001L,
                "Updated Corp", "BUSINESS", "J-12345678-9", "Updated", 30,
                java.math.BigDecimal.ZERO, true, null, LocalDateTime.now(),
                List.of(), List.of(), List.of(), null, null, true);

        when(updateCustomerUseCase.execute(eq(id),
                any(UpdateCustomerRequest.class))).thenReturn(response);

        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Updated Corp", "BUSINESS", "J-12345678-9", "Updated", 30,
                java.math.BigDecimal.ZERO, true, List.of(), List.of(),
                List.of(), null, null);

        ResponseEntity<CustomerDetailResponse> result = customerController
                .update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(updateCustomerUseCase).execute(eq(id),
                any(UpdateCustomerRequest.class));
    }

    @Test
    @DisplayName("should delete customer and return 204")
    void should_delete_customer_and_return_204() {
        String id = UUID.randomUUID().toString();
        doNothing().when(deleteCustomerUseCase).execute(id);

        ResponseEntity<Void> result = customerController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteCustomerUseCase).execute(id);
    }

}
