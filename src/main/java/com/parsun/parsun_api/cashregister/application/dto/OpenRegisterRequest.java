package com.parsun.parsun_api.cashregister.application.dto;

import jakarta.validation.constraints.NotBlank;

public record OpenRegisterRequest(
        @NotBlank String openedByName
) {}
