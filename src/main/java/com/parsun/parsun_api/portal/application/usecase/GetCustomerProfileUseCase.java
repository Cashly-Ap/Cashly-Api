package com.parsun.parsun_api.portal.application.usecase;

import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.portal.application.dto.response.PortalProfileResponse;
import com.parsun.parsun_api.portal.application.mapper.PortalProfileMapper;

import java.math.BigDecimal;
import java.util.UUID;

public class GetCustomerProfileUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerBalanceRepository customerBalanceRepository;

    public GetCustomerProfileUseCase(CustomerRepository customerRepository,
                                      CustomerBalanceRepository customerBalanceRepository) {
        this.customerRepository = customerRepository;
        this.customerBalanceRepository = customerBalanceRepository;
    }

    public PortalProfileResponse execute(String customerId) {
        UUID customerUuid = UUID.fromString(customerId);
        CustomerId customerIdObj = new CustomerId(customerUuid);

        var customer = customerRepository.findById(customerIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        BigDecimal currentBalance = customerBalanceRepository.findByCustomerId(customerUuid)
            .map(b -> b.getTotalBalance())
            .orElse(BigDecimal.ZERO);

        return PortalProfileMapper.toResponse(customer, currentBalance);
    }
}
