package com.cashly.cashly_api.auth.authentication.application.mappers;

import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticatedEntityResponse;
import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.auth.authentication.domain.Authenticable;
import com.cashly.cashly_api.auth.token.domain.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationMapper {

    public AuthenticationResponse toAuthenticationResponse(
        String accessToken,
        RefreshToken refreshToken,
        long expiresIn,
        Authenticable authenticable
    ) {
        return new AuthenticationResponse(
            accessToken,
            refreshToken.getToken(),
            "Bearer",
            expiresIn,
            AuthenticatedEntityResponse.from(authenticable)
        );
    }
}
