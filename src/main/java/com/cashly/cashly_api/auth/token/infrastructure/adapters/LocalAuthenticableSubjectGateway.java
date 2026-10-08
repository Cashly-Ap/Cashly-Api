package com.cashly.cashly_api.auth.token.infrastructure.adapters;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.entities.AuthenticableType;
import com.cashly.cashly_api.auth.authentication.infrastructure.adapters.UserAuthenticableAdapter;
import com.cashly.cashly_api.auth.token.application.contracts.AuthenticableSubjectGateway;
import com.cashly.cashly_api.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
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
