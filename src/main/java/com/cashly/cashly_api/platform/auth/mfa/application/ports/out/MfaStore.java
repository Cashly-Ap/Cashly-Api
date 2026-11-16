package com.cashly.cashly_api.platform.auth.mfa.application.ports.out;

import com.cashly.cashly_api.platform.auth.mfa.domain.entity.MfaAccount;
public interface MfaStore {
    MfaAccount lock(String subjectId);
    MfaAccount lockByChallenge(String hash);
    void save(MfaAccount account);
}
