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
    @PreAuthorize("hasAuthority('ROLES_VIEW_LIST')")
    public List<Role> list(
            @RequestParam(required = false) ManageRolesUseCase.ListAction action) {
        return useCase.list(action);
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('USERS_VIEW_LIST') and (hasAuthority('USERS_CREATE') or hasAuthority('USERS_EDIT'))")
    public List<ManageRolesUseCase.Option> options() {
        return useCase.options();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_VIEW_DETAIL')")
    public Role find(@PathVariable UUID id) {
        return useCase.find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLES_CREATE')")
    public Role create(@RequestBody ManageRolesUseCase.Request request) {
        return useCase.save(null, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_EDIT')")
    public Role update(@PathVariable UUID id,
            @RequestBody ManageRolesUseCase.Request request) {
        return useCase.save(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLES_DELETE')")
    public void delete(@PathVariable UUID id) {
        useCase.delete(id);
    }


}
