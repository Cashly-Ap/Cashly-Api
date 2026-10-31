package com.cashly.cashly_api.infrastructure.seed;

public interface SeedStep {
    int version();
    void seed();
}
