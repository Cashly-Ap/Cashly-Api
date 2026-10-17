package com.cashly.cashly_api.partners.customers.profile.domain.services;

import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerCode;

public interface CustomerCodeGenerator {
    CustomerCode generateNext();
}
