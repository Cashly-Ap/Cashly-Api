package com.parsun.parsun_api.finances.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReturnListResponse(
        UUID id,
        String returnNumber,
        String invoiceNumber,
        String customerName,
        String returnType,
        String reason,
        LocalDateTime createdAt
) {}
