package com.parsun.parsun_api.customers.application.dto.response;

public record CustomerListResponse(
    String id,
    Long code,
    String alias,
    String legalName,
    String rif,
    String type,
    boolean active
) {}
