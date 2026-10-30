package com.cashly.cashly_api.platform.auth.preferences.infrastructure.web;

import com.cashly.cashly_api.platform.auth.preferences.application.UserPreferencesService;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferences;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/preferences")
public class UserPreferencesController {
    private final UserPreferencesService service;
    public UserPreferencesController(UserPreferencesService service) { this.service = service; }
    public record UpdateRequest(
            @NotNull @Pattern(regexp = "system|light|dark") String themeMode,
            @NotNull @Pattern(regexp = "minimal|informative") String colorMode,
            @NotNull Boolean shortcutsEnabled, @NotNull Boolean showShortcutHints, @Pattern(regexp = "es|en") String language) {
        UserPreferences toDomain() { return new UserPreferences(themeMode, colorMode, shortcutsEnabled, showShortcutHints); }
    }
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPreferences> current() {
        return noStore(service.current());
    }
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPreferences> update(@Valid @RequestBody UpdateRequest request) {
        return noStore(service.update(request.toDomain(), request.language()));
    }
    private ResponseEntity<UserPreferences> noStore(UserPreferences preferences) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(preferences);
    }
}
