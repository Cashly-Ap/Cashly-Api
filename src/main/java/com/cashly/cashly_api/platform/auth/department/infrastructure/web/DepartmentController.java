package com.cashly.cashly_api.platform.auth.department.infrastructure.web;

import com.cashly.cashly_api.platform.auth.department.application.use_case.ManageDepartmentsUseCase;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final ManageDepartmentsUseCase useCase;

    public DepartmentController(ManageDepartmentsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENTS:READ')")
    public List<Department> list(
            @RequestParam(required = false) ManageDepartmentsUseCase.ListAction action) {
        return useCase.list(action);
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('USERS:READ') and (hasAuthority('USERS:CREATE') or hasAuthority('USERS:UPDATE'))")
    public List<ManageDepartmentsUseCase.Option> options() {
        return useCase.options();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENTS:READ')")
    public Department find(@PathVariable UUID id) {
        return useCase.find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DEPARTMENTS:CREATE')")
    public Department create(
            @RequestBody ManageDepartmentsUseCase.Request request) {
        return useCase.save(null, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENTS:UPDATE')")
    public Department update(@PathVariable UUID id,
            @RequestBody ManageDepartmentsUseCase.Request request) {
        return useCase.save(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('DEPARTMENTS:DELETE')")
    public void delete(@PathVariable UUID id) {
        useCase.delete(id);
    }
}
