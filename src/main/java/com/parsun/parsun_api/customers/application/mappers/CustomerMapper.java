package com.parsun.parsun_api.customers.application.mappers;

import com.parsun.parsun_api.customers.application.dto.response.*;
import com.parsun.parsun_api.customers.domain.entities.Customer;

import java.util.List;

public class CustomerMapper {

    public CustomerListResponse toListResponse(Customer customer, boolean deletable) {
        return new CustomerListResponse(
            customer.getId().toString(),
            customer.getCode().getValue(),
            customer.getAlias().getValue(),
            customer.getLegalName().getValue(),
            customer.getRif().getValue(),
            customer.getType().name(),
            customer.isActive(),
            customer.getPaymentType(),
            customer.isPortalAccess(),
            deletable
        );
    }

    public CustomerDetailResponse toDetailResponse(Customer customer) {
        return new CustomerDetailResponse(
            customer.getId().toString(),
            customer.getCode().getValue(),
            customer.getLegalName().getValue(),
            customer.getType().name(),
            customer.getRif().getValue(),
            customer.getAlias().getValue(),
            customer.getCreditDays().getValue(),
            customer.getCreditLimit().getValue(),
            customer.isActive(),
            customer.getPaymentType(),
            customer.getCreatedAt(),
            toEmailResponses(customer),
            toPhoneResponses(customer),
            toAddressResponses(customer),
            customer.isPortalAccess(),
            customer.getNotes(),
            customer.getPriceTypeId()
        );
    }

    public CustomerCreateResponse toCreateResponse(Customer customer) {
        return new CustomerCreateResponse(
            customer.getId().toString(),
            customer.getCode().getValue(),
            customer.getLegalName().getValue(),
            customer.getType().name(),
            customer.getRif().getValue(),
            customer.getAlias().getValue(),
            customer.getCreditDays().getValue(),
            customer.getCreditLimit().getValue(),
            customer.isActive(),
            customer.getPaymentType(),
            customer.getCreatedAt(),
            toEmailResponses(customer),
            toPhoneResponses(customer),
            toAddressResponses(customer),
            customer.isPortalAccess(),
            customer.getNotes(),
            customer.getPriceTypeId()
        );
    }

    private List<EmailResponse> toEmailResponses(Customer customer) {
        return customer.getEmails().stream()
            .map(e -> new EmailResponse(
                e.getId().toString(),
                e.getCustomerId().toString(),
                e.getEmail().getValue()
            ))
            .toList();
    }

    private List<PhoneResponse> toPhoneResponses(Customer customer) {
        return customer.getPhones().stream()
            .map(p -> new PhoneResponse(
                p.getId().toString(),
                p.getCustomerId().toString(),
                p.getPhone().getValue()
            ))
            .toList();
    }

    private List<AddressResponse> toAddressResponses(Customer customer) {
        return customer.getAddresses().stream()
            .map(a -> new AddressResponse(
                a.getId().toString(),
                a.getCustomerId().toString(),
                a.getAddress(),
                a.isBilling(),
                a.isActive(),
                a.getState(),
                a.getMunicipality(),
                a.getPostalCode()
            ))
            .toList();
    }
}
