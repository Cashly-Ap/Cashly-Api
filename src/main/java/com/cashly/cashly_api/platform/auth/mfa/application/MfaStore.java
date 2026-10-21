package com.cashly.cashly_api.platform.auth.mfa.application;

import com.cashly.cashly_api.platform.auth.mfa.domain.MfaAccount;
public interface MfaStore {
    MfaAccount lock(String subjectId);
    MfaAccount lockByChallenge(String hash);
    void save(MfaAccount account);
}
