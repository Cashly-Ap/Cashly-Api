package com.cashly.cashly_api.partners.customers.profile.application.ports.in;



import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CustomerProfileSnapshot(
    UUID id,
    Long code,
    String legalName,
    String type,
    String rif,
    String alias,
    String paymentType,
    int creditDays,
    BigDecimal creditLimit,
    BigDecimal currentBalance,
    boolean active,
    UUID priceTypeId,
    String notes,
    List<String> emails,
    List<String> phones,
    List<Address> addresses
) { public record Address(String id, String customerId, String address, boolean billing, boolean active, String state, String municipality, String postalCode) {} }

