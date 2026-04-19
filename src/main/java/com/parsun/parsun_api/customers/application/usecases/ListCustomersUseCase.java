package com.parsun.parsun_api.customers.application.usecases;

import com.parsun.parsun_api.customers.application.dto.response.CustomerListResponse;
import com.parsun.parsun_api.customers.application.mappers.CustomerMapper;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public ListCustomersUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public List<CustomerListResponse> execute(String search, String rif, String type, Boolean active,
                                               Boolean portalAccess, String state, String municipality, String postalCode) {
        boolean hasFilters = search != null || rif != null || type != null || active != null
            || portalAccess != null || state != null || municipality != null || postalCode != null;

        List<Customer> customers = hasFilters
            ? customerRepository.findAllWithFilters(search, rif, type, active, portalAccess, state, municipality, postalCode)
            : customerRepository.findAll();

        return customers.stream()
            .map(customerMapper::toListResponse)
            .toList();
    }
}
