package com.cashly.cashly_api.platform.auth.token.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.integration.UserAuthenticableAdapter;
import com.cashly.cashly_api.platform.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;

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
