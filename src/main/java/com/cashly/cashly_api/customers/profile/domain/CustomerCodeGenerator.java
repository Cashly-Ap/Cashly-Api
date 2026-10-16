package com.cashly.cashly_api.customers.profile.domain;

import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerCode;

public interface CustomerCodeGenerator {
    CustomerCode generateNext();
}
