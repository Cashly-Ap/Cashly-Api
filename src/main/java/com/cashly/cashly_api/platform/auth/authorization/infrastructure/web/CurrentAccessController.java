package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;

@RestController
@RequestMapping("/api/auth/access")
public class CurrentAccessController  {
    private final AccessService access;
    public CurrentAccessController(AccessService access)  {
        this.access=access;
    }
    @GetMapping public AccessService.CurrentAccess current()  {
        return access.current();
    }
}
