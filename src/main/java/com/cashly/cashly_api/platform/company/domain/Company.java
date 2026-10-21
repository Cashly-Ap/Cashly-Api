package com.cashly.cashly_api.platform.company.domain;
import java.util.UUID;
public record Company(UUID id, String legalName, String tradeName, String taxId, String email,
                      String phone, String website, Address address, String logoPath, String pdfLogoPath) {
    public static final UUID INSTALLATION_ID = UUID.fromString("92000000-0000-0000-0000-000000000001");
    public record Address(String street, String city, String region, String country, String postalCode) {}
}
