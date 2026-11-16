package com.cashly.cashly_api.partners.customers.profile.application.queries.services;

import com.cashly.cashly_api.partners.customers.profile.application.ports.in.CustomerProfileQuery;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.balance.domain.entity.CustomerBalance;
import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.partners.customers.profile.application.ports.in.CustomerProfileSnapshot;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class RepositoryCustomerProfileQuery implements CustomerProfileQuery {

    private final CustomerRepository customerRepository;
    private final CustomerBalanceRepository customerBalanceRepository;

    public RepositoryCustomerProfileQuery(CustomerRepository customerRepository,
            CustomerBalanceRepository customerBalanceRepository) {
        this.customerRepository = customerRepository;
        this.customerBalanceRepository = customerBalanceRepository;
    }

    @Transactional(readOnly = true)
    public CustomerProfileSnapshot find(UUID customerId) {
        Customer customer = customerRepository
                .findById(new CustomerId(customerId))
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found: " + customerId));

        List<String> emails = customer.getEmails().stream()
                .map(e -> e.getEmail().getValue()).toList();

        List<String> phones = customer.getPhones().stream()
                .map(p -> p.getPhone().getValue()).toList();

        List<CustomerProfileSnapshot.Address> addresses = customer
                .getAddresses().stream()
                .map(a -> new CustomerProfileSnapshot.Address(
                        a.getId().getValue().toString(),
                        a.getCustomerId().getValue().toString(), a.getAddress(),
                        a.isBilling(), a.isActive(), a.getState(),
                        a.getMunicipality(), a.getPostalCode()))
                .toList();

        BigDecimal currentBalance = customerBalanceRepository
                .findByCustomerId(customerId)
                .map(CustomerBalance::getTotalBalance).orElse(BigDecimal.ZERO);

        return new CustomerProfileSnapshot(customer.getId().getValue(),
                customer.getCode().getValue(),
                customer.getLegalName().getValue(), customer.getType().name(),
                customer.getRif().getValue(),
                customer.getAlias() != null
                        ? customer.getAlias().getValue()
                        : null,
                customer.getPaymentType().name(),
                customer.getCreditDays().getValue(),
                customer.getCreditLimit().getValue(), currentBalance,
                customer.isActive(), customer.getPriceTypeId(),
                customer.getNotes(), emails, phones, addresses);
    }
}
