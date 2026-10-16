package com.cashly.cashly_api.auth.token.infrastructure.integration;

import com.cashly.cashly_api.auth.authentication.domain.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.AuthenticableType;
import com.cashly.cashly_api.auth.authentication.infrastructure.integration.UserAuthenticableAdapter;
import com.cashly.cashly_api.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.auth.user.domain.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;

public class LocalAuthenticableSubjectGateway implements AuthenticableSubjectGateway {
    private final UserRepository userRepository;
    public LocalAuthenticableSubjectGateway(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Authenticable resolve(String subjectId, AuthenticableType subjectType) {
        return new UserAuthenticableAdapter(userRepository.findById(UserId.from(subjectId))
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }
}
