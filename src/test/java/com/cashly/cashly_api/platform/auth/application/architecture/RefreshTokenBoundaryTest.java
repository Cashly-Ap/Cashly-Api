package com.cashly.cashly_api.platform.auth.application.architecture;

import com.cashly.cashly_api.platform.auth.token.application.refresh.use_case.RefreshTokenUseCase;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenBoundaryTest {
    @Test
    void should_resolve_subject_through_application_contract() {
        var constructor = RefreshTokenUseCase.class.getConstructors()[0];
        assertEquals(3, constructor.getParameterCount());
        assertEquals("com.cashly.cashly_api.platform.auth.token.application.ports.out.AuthenticableSubjectGateway",
                constructor.getParameterTypes()[1].getName());
    }

    @Test
    void should_keep_authentication_domain_free_of_spring_stereotypes() {
        assertEquals(0, com.cashly.cashly_api.platform.auth.authentication.domain.services.AuthenticationDomainService.class.getAnnotations().length);
    }
}
