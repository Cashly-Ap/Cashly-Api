package com.cashly.cashly_api.customers.domain.services;

import com.cashly.cashly_api.customers.domain.valueobjects.CustomerCode;

public interface CustomerCodeGenerator {
    CustomerCode generateNext();
}
