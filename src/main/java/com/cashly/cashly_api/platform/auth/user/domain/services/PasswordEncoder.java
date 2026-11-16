package com.cashly.cashly_api.platform.auth.user.domain.services;

public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
