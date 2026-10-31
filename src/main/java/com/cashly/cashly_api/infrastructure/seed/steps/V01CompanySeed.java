package com.cashly.cashly_api.infrastructure.seed.steps;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.company.domain.Company;
import com.cashly.cashly_api.platform.company.domain.CompanyRepository;
import org.springframework.stereotype.Component;

@Component
public class V01CompanySeed implements SeedStep {

    private final CompanyRepository companies;

    public V01CompanySeed(CompanyRepository companies) {
        this.companies = companies;
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public void seed() {
        companies.createIfAbsent(new Company(
                Company.INSTALLATION_ID, "Cashly Demo, C.A.", "Cashly Demo",
                "J-00000000-0", "hello@cashly.local", "+58-212-0000000",
                "https://cashly.local", new Company.Address("Avenida Principal",
                        "Caracas", "Distrito Capital", "Venezuela", "1010"),
                null, null));
    }
}
