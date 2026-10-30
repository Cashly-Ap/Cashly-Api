package com.cashly.cashly_api.platform.auth.user.application.update.request;

import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.UUID;

public record UpdateUserRequest(String userName,
        String firstName, String lastName, UserProfile.Address address, String phone,
        UUID roleId, UUID departmentId, Boolean active, Boolean emailVerified, String language) {
    public UpdateUserRequest(String userName, String firstName, String lastName, UserProfile.Address address,
            String phone, UUID roleId, UUID departmentId, Boolean active, Boolean emailVerified) {
        this(userName, firstName, lastName, address, phone, roleId, departmentId, active, emailVerified, null);
    }
    public UpdateUserRequest {
        if (language != null) com.cashly.cashly_api.platform.auth.preferences.domain.SupportedLanguage.require(language);
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("User name is required");
        }
        var profile = new UserProfile(firstName, lastName, address, phone);
        firstName = profile.firstName();
        lastName = profile.lastName();
        phone = profile.phone();
        if (roleId == null || departmentId == null) {
            throw new IllegalArgumentException("Role and department are required");
        }
    }

    @JsonAnySetter
    public void rejectUnknownField(String name, Object value) {
        throw new IllegalArgumentException("Unsupported user field: " + name);
    }

    public UserProfile profile() {
        return new UserProfile(firstName, lastName, address, phone);
    }
}
