package com.cashly.cashly_api.platform.auth.user.application.update.request;

import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public record UpdateOwnProfileRequest(String userName, String firstName,
        String lastName, UserProfile.Address address, String phone,
        String language) {
    public UpdateOwnProfileRequest(String userName, String firstName,
            String lastName, UserProfile.Address address, String phone) {
        this(userName, firstName, lastName, address, phone, null);
    }
    public UpdateOwnProfileRequest {
        if (language != null)
            com.cashly.cashly_api.platform.auth.preferences.domain.SupportedLanguage
                    .require(language);
        userName = new UserName(userName).getValue();
        var profile = new UserProfile(firstName, lastName, address, phone);
        firstName = profile.firstName();
        lastName = profile.lastName();
        phone = profile.phone();
    }

    @JsonAnySetter
    public void rejectUnknownField(String name, Object value) {
        throw new IllegalArgumentException(
                "Unsupported own profile field: " + name);
    }

    public UserProfile profile() {
        return new UserProfile(firstName, lastName, address, phone);
    }
}
