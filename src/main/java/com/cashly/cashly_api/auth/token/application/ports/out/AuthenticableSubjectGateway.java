package com.cashly.cashly_api.auth.token.application.ports.out;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.entities.AuthenticableType;

public interface AuthenticableSubjectGateway {
    Authenticable resolve(String subjectId, AuthenticableType subjectType);
}
