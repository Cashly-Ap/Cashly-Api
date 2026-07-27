package com.parsun.parsun_api.auth.authentication.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.parsun.parsun_api.auth.authentication.application.usecases.LoginUserUseCase;
import com.parsun.parsun_api.auth.authentication.domain.services.AuthenticableResolver;
import com.parsun.parsun_api.auth.authentication.infrastructure.services.ChainedAuthenticableResolver;
import com.parsun.parsun_api.auth.token.application.usecases.RefreshTokenUseCase;
import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;

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
    public RefreshTokenUseCase refreshTokenUseCase(
        RefreshTokenRepository refreshTokenRepository,
        UserRepository userRepository,
        TokenService tokenService,
        CustomerRepository customerRepository
    ) {
        return new RefreshTokenUseCase(
            refreshTokenRepository,
            userRepository,
            tokenService,
            customerRepository
        );
    }
}
