package com.parsun.parsun_api.auth.user.domain.services;

public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
