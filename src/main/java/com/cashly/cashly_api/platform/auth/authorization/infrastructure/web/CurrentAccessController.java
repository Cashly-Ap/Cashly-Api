package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;

@RestController
@RequestMapping("/api/auth/access")
public class CurrentAccessController {
    private final AccessService access;
    public CurrentAccessController(AccessService access) {
        this.access = access;
    }
    @GetMapping
    public ResponseEntity<AccessService.CurrentAccess> current() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(access.current());
    }
}
