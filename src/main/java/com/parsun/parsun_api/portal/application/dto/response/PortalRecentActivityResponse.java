package com.parsun.parsun_api.portal.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortalRecentActivityResponse(
    String type,
    String number,
    LocalDate date,
    String status,
    BigDecimal amount,
    String reference
) {}
