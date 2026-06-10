package com.parsun.parsun_api.cashregister.application.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyRegisterRequest(
        @NotBlank String verifiedByName
) {}
