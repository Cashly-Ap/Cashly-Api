package com.parsun.parsun_api.portal.application.mapper;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerAddress;
import com.parsun.parsun_api.portal.application.dto.response.PortalAddressResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalProfileResponse;

import java.math.BigDecimal;
import java.util.List;

public final class PortalProfileMapper {

    private PortalProfileMapper() {}

    public static PortalProfileResponse toResponse(Customer customer, BigDecimal currentBalance) {
        List<String> emails = customer.getEmails().stream()
            .map(e -> e.getEmail().getValue())
            .toList();

        List<String> phones = customer.getPhones().stream()
            .map(p -> p.getPhone().getValue())
            .toList();

        List<PortalAddressResponse> addresses = customer.getAddresses().stream()
            .map(PortalProfileMapper::toAddressResponse)
            .toList();

        return new PortalProfileResponse(
            customer.getLegalName().getValue(),
            customer.getRif().getValue(),
            customer.getType().name(),
            customer.getAlias().getValue(),
            customer.getCreditDays().getValue(),
            customer.getCreditLimit().getValue(),
            customer.getPaymentType().name(),
            currentBalance,
            emails,
            phones,
            addresses
        );
    }

    private static PortalAddressResponse toAddressResponse(CustomerAddress address) {
        return new PortalAddressResponse(
            address.getId().getValue(),
            address.getAddress(),
            address.getState(),
            address.getMunicipality(),
            address.getPostalCode(),
            address.isBilling()
        );
    }
}
