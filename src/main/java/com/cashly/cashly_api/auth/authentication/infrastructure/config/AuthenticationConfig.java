package com.cashly.cashly_api.auth.authentication.infrastructure.config;

import com.cashly.cashly_api.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.auth.token.infrastructure.adapters.out.LocalAuthenticableSubjectGateway;
import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticationDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cashly.cashly_api.auth.authentication.application.usecases.LoginUserUseCase;
import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.auth.authentication.infrastructure.adapters.out.services.ChainedAuthenticableResolver;
import com.cashly.cashly_api.auth.token.application.usecases.RefreshTokenUseCase;
import com.cashly.cashly_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;

@Configuration
public class AuthenticationConfig {

    @Bean
    public AuthenticableResolver authenticableResolver(UserRepository userRepository) {
        return new ChainedAuthenticableResolver(userRepository);
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(
        AuthenticableResolver authenticableResolver,
        RefreshTokenRepository refreshTokenRepository,
        TokenService tokenService,
        PasswordEncoder passwordEncoder
    ) {
        return new LoginUserUseCase(
            authenticableResolver,
            refreshTokenRepository,
            tokenService,
            passwordEncoder
        );
    }

    @Bean
    public AuthenticationDomainService authenticationDomainService() {
        return new AuthenticationDomainService();
    }

    @Bean
    public AuthenticableSubjectGateway authenticableSubjectGateway(UserRepository userRepository) {
        return new LocalAuthenticableSubjectGateway(userRepository);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                                                  AuthenticableSubjectGateway subjectGateway,
                                                  TokenService tokenService) {
        return new RefreshTokenUseCase(refreshTokenRepository, subjectGateway, tokenService);
    }
}
