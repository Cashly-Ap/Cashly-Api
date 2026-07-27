package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.AddressResponse;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerBalance;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.portal.application.dto.response.PortalProfileResponse;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class GetCustomerProfileUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerBalanceRepository customerBalanceRepository;

    public GetCustomerProfileUseCase(CustomerRepository customerRepository,
                                      CustomerBalanceRepository customerBalanceRepository) {
        this.customerRepository = customerRepository;
        this.customerBalanceRepository = customerBalanceRepository;
    }

    @Transactional(readOnly = true)
    public PortalProfileResponse execute(UUID customerId) {
        Customer customer = customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));

        List<String> emails = customer.getEmails().stream()
                .map(e -> e.getEmail().getValue())
                .toList();

        List<String> phones = customer.getPhones().stream()
                .map(p -> p.getPhone().getValue())
                .toList();

        List<AddressResponse> addresses = customer.getAddresses().stream()
                .map(a -> new AddressResponse(
                        a.getId().getValue().toString(),
                        a.getCustomerId().getValue().toString(),
                        a.getAddress(),
                        a.isBilling(),
                        a.isActive(),
                        a.getState(),
                        a.getMunicipality(),
                        a.getPostalCode()))
                .toList();

        BigDecimal currentBalance = customerBalanceRepository.findByCustomerId(customerId)
                .map(CustomerBalance::getTotalBalance)
                .orElse(BigDecimal.ZERO);

        return new PortalProfileResponse(
                customer.getId().getValue(),
                customer.getCode().getValue(),
                customer.getLegalName().getValue(),
                customer.getType().name(),
                customer.getRif().getValue(),
                customer.getAlias() != null ? customer.getAlias().getValue() : null,
                customer.getPaymentType().name(),
                customer.getCreditDays().getValue(),
                customer.getCreditLimit().getValue(),
                currentBalance,
                customer.isActive(),
                customer.isPortalAccess(),
                customer.getPriceTypeId(),
                customer.getNotes(),
                emails,
                phones,
                addresses
        );
    }
}
