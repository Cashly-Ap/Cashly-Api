package com.cashly.cashly_api.platform.company.domain;
public interface CompanyRepository {
    Company find();
    Company lock();
    void save(Company company);
}
