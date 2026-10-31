package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.ADMIN_USER;
import static com.cashly.cashly_api.infrastructure.seed.SeedIds.OPERATOR_USER;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.auth.mfa.infrastructure.JdbcMfaStore;
import org.springframework.stereotype.Component;

@Component
public class V28MfaSeed implements SeedStep {
    private final JdbcMfaStore mfaStore;
    public V28MfaSeed(JdbcMfaStore mfaStore) { this.mfaStore = mfaStore; }
    @Override public int version() { return 28; }
    @Override public void seed() {
        mfaStore.lock(ADMIN_USER.toString());
        mfaStore.lock(OPERATOR_USER.toString());
    }
}
