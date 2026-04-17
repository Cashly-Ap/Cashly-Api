package com.parsun.parsun_api.customers.infrastructure.web;

import com.parsun.parsun_api.customers.application.dto.request.CreateCustomerRequest;
import com.parsun.parsun_api.customers.application.dto.request.UpdateCustomerRequest;
import com.parsun.parsun_api.customers.application.dto.response.CustomerCreateResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerDetailResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerListResponse;
import com.parsun.parsun_api.customers.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<CustomerCreateResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerCreateResponse response = registerCustomerUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<List<CustomerListResponse>> getAll() {
        List<CustomerListResponse> customers = listCustomersUseCase.execute();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<CustomerDetailResponse> getById(@PathVariable String id) {
        CustomerDetailResponse customer = getCustomerUseCase.execute(id);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR')")
    public ResponseEntity<CustomerDetailResponse> update(@PathVariable String id,
                                                          @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerDetailResponse customer = updateCustomerUseCase.execute(id, request);
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteCustomerUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
