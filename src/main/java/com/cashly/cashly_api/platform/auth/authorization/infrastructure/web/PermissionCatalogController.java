package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cashly.cashly_api.platform.auth.authorization.domain.services.PermissionCatalog;

@RestController
@RequestMapping("/api/permissions")
public class PermissionCatalogController {

    @GetMapping("/catalog")
    @PreAuthorize("hasAuthority('ROLES:READ')")
    public List<PermissionCatalog.AreaCatalog> catalog() {
        return PermissionCatalog.areas();
    }
}
