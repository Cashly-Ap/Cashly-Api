package com.cashly.cashly_api.shared.infrastructure.pdf;

import java.io.IOException;
import java.io.InputStream;

public final class DefaultCompanyLogo {

    private static final String RESOURCE = "/pdf/cashly-logo.png";

    private DefaultCompanyLogo() {
    }

    public static byte[] bytes() {
        try (InputStream input = DefaultCompanyLogo.class
                .getResourceAsStream(RESOURCE)) {
            return input == null ? null : input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cannot load default Cashly logo", exception);
        }
    }
}
