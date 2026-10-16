package com.cashly.cashly_api.customers.profile.application.list;

import com.cashly.cashly_api.customers.profile.application.dto.response.CustomerListResponse;
import com.cashly.cashly_api.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.customers.profile.domain.Customer;
import com.cashly.cashly_api.customers.profile.domain.CustomerRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public ListCustomersUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerListResponse> execute(String search, String rif, String type, Boolean active,
                                                       String state, String municipality,
                                                       String postalCode, Boolean hasActiveWarehouse, int page, int size,
                                                       String orderBy, String orderDir) {
        Boolean effectiveActive = active != null ? active : true;
        PageResponse<Customer> customerPage = customerRepository.findAllWithFilters(
            search, rif, type, effectiveActive, state, municipality, postalCode, hasActiveWarehouse,
            page, size, orderBy, orderDir);

        Set<UUID> idsWithRecords = customerRepository.findIdsWithAssociatedRecords(
            customerPage.content().stream()
                .map(c -> c.getId().getValue())
                .collect(Collectors.toSet())
        );

        List<CustomerListResponse> content = customerPage.content().stream()
            .map(c -> customerMapper.toListResponse(c, !idsWithRecords.contains(c.getId().getValue())))
            .toList();

        return new PageResponse<>(
            content,
            customerPage.totalElements(),
            customerPage.totalPages(),
            customerPage.size(),
            customerPage.number(),
            customerPage.first(),
            customerPage.last(),
            customerPage.empty()
        );
    }
}

