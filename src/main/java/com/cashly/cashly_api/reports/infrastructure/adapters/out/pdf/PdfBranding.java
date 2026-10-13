package com.cashly.cashly_api.reports.infrastructure.adapters.out.pdf;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import java.io.IOException;
import java.io.InputStream;

public final class PdfBranding {

    private static final String LOGO_RESOURCE = "/pdf/cashly-logo.png";
    private static final byte[] LOGO_BYTES = loadLogo();

    private PdfBranding() {
    }

    public static Image logo() {
        return new Image(ImageDataFactory.create(LOGO_BYTES))
                .setWidth(125);
    }

    private static byte[] loadLogo() {
        try (InputStream inputStream = PdfBranding.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("PDF logo resource not found: " + LOGO_RESOURCE);
            }
            return inputStream.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load PDF logo resource: " + LOGO_RESOURCE, exception);
        }
    }
}
