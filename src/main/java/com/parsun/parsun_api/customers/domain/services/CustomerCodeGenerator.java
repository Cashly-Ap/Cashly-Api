package com.parsun.parsun_api.customers.domain.services;

import com.parsun.parsun_api.customers.domain.valueobjects.CustomerCode;

public interface CustomerCodeGenerator {
    CustomerCode generateNext();
}
