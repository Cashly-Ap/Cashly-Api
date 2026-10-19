package com.cashly.cashly_api.platform.auth.role.infrastructure.web;

import com.cashly.cashly_api.platform.auth.role.application.use_case.ManageRolesUseCase;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final ManageRolesUseCase useCase;

    public RoleController(ManageRolesUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES:READ')")
    public List<Role> list() {
        return useCase.list();
    }

    @GetMapping("/options")
    @PreAuthorize("isAuthenticated()")
    public List<ManageRolesUseCase.Option> options() {
        return useCase.options();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES:READ')")
    public Role find(@PathVariable UUID id) {
        return useCase.find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLES:CREATE')")
    public Role create(@RequestBody ManageRolesUseCase.Request request) {
        return useCase.save(null, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES:UPDATE')")
    public Role update(@PathVariable UUID id, @RequestBody ManageRolesUseCase.Request request) {
        return useCase.save(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLES:DELETE')")
    public void delete(@PathVariable UUID id) {
        useCase.delete(id);
    }

    public record RegistryEntry(
            com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule module,
            java.util.Set<com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction> actions) {}

    @GetMapping("/registry")
    @PreAuthorize("hasAuthority('ROLES:READ')")
    public List<RegistryEntry> registry() {
        return com.cashly.cashly_api.platform.auth.authorization.domain.services.PermissionRegistry.modules()
                .entrySet().stream().map(entry -> new RegistryEntry(entry.getKey(), entry.getValue())).toList();
    }
}
