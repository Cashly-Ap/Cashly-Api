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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;

    public CustomerController(RegisterCustomerUseCase registerCustomerUseCase,
            GetCustomerUseCase getCustomerUseCase,
            ListCustomersUseCase listCustomersUseCase,
            UpdateCustomerUseCase updateCustomerUseCase,
            DeleteCustomerUseCase deleteCustomerUseCase) {
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.getCustomerUseCase = getCustomerUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.deleteCustomerUseCase = deleteCustomerUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTACTS_CUSTOMERS:CREATE')")
    public ResponseEntity<CustomerCreateResponse> create(
            @Valid @RequestBody CreateCustomerRequest request,
            Authentication authentication) {
        UUID grantedBy = UUID.fromString(authentication.getName());
        CustomerCreateResponse response = registerCustomerUseCase
                .execute(request, grantedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTACTS_CUSTOMERS:READ')")
    public ResponseEntity<PageResponse<CustomerListResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rif,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String municipality,
            @RequestParam(required = false) String postalCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "legalName") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        PageResponse<CustomerListResponse> customers = listCustomersUseCase
                .execute(search, rif, type, active, state, municipality,
                        postalCode, null, page, size, orderBy, orderDir);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/picker")
    @PreAuthorize("hasAuthority('CONTACTS_CUSTOMERS:READ')")
    public ResponseEntity<PageResponse<CustomerListResponse>> getForPicker(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CustomerListResponse> customers = listCustomersUseCase
                .execute(search, null, type, true, null, null, null, true, page,
                        size, "legalName", "asc");
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACTS_CUSTOMERS:READ')")
    public ResponseEntity<CustomerDetailResponse> getById(
            @PathVariable String id) {
        CustomerDetailResponse customer = getCustomerUseCase.execute(id);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACTS_CUSTOMERS:UPDATE')")
    public ResponseEntity<CustomerDetailResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerDetailResponse customer = updateCustomerUseCase.execute(id,
                request);
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACTS_CUSTOMERS:DELETE')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteCustomerUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

}
