package com.cashly.cashly_api.shared.infrastructure.pdf;

import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.itextpdf.io.image.ImageDataFactory;
import com.cashly.cashly_api.platform.company.application.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.function.Supplier;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Component
public final class PdfBranding {

    private final Supplier<byte[]> companyLogo;

    public PdfBranding() { this(() -> null); }

    @Autowired
    public PdfBranding(CompanyService company) {
        this(company::documentLogo);
    }

    public PdfBranding(Supplier<byte[]> companyLogo) {
        this.companyLogo = companyLogo;
    }

    private static final int WHITE_BACKGROUND_THRESHOLD = 250;
    private static final float CONTENT_PADDING_RATIO = 0.02f;
    private static final float LOGO_MAX_WIDTH = 125;
    private static final float LOGO_MAX_HEIGHT = 65;
    public Div logo() {
        byte[] configured = companyLogo.get();
        var branding = new Div().setWidth(LOGO_MAX_WIDTH).setKeepTogether(true);
        if (configured == null) {
            return branding.add(new Paragraph(ReportMessages.text("shared.reports.logo")).setFontSize(20).setBold().setMargin(0));
        }
        return branding.add(new Image(ImageDataFactory.create(trimOuterMargins(configured)))
                .scaleToFit(LOGO_MAX_WIDTH, LOGO_MAX_HEIGHT));
    }

    /** Removes only empty border rows/columns. Every non-background pixel stays inside the result. */
    private static byte[] trimOuterMargins(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) throw new IllegalArgumentException("Invalid logo image");
            boolean whiteBackground = isOpaqueWhite(image.getRGB(0, 0))
                    && isOpaqueWhite(image.getRGB(image.getWidth() - 1, 0))
                    && isOpaqueWhite(image.getRGB(0, image.getHeight() - 1))
                    && isOpaqueWhite(image.getRGB(image.getWidth() - 1, image.getHeight() - 1));
            if (image.getColorModel().hasAlpha()) {
                outer: for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        if (((image.getRGB(x, y) >>> 24) & 255) < 255) {
                            whiteBackground = false;
                            break outer;
                        }
                    }
                }
            }
            int left = image.getWidth(), top = image.getHeight(), right = -1, bottom = -1;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);
                    int alpha = (pixel >>> 24) & 255;
                    int red = (pixel >>> 16) & 255, green = (pixel >>> 8) & 255, blue = pixel & 255;
                    boolean empty = alpha == 0 || (whiteBackground && alpha == 255 && red >= WHITE_BACKGROUND_THRESHOLD && green >= WHITE_BACKGROUND_THRESHOLD && blue >= WHITE_BACKGROUND_THRESHOLD);
                    if (!empty) {
                        left = Math.min(left, x); top = Math.min(top, y);
                        right = Math.max(right, x); bottom = Math.max(bottom, y);
                    }
                }
            }
            if (right < left || (left == 0 && top == 0 && right == image.getWidth() - 1 && bottom == image.getHeight() - 1)) {
                return bytes;
            }
            int padding = Math.max(1, Math.round(Math.max(right - left + 1, bottom - top + 1) * CONTENT_PADDING_RATIO));
            left = Math.max(0, left - padding); top = Math.max(0, top - padding);
            right = Math.min(image.getWidth() - 1, right + padding);
            bottom = Math.min(image.getHeight() - 1, bottom + padding);
            var output = new ByteArrayOutputStream();
            ImageIO.write(image.getSubimage(left, top, right - left + 1, bottom - top + 1), "png", output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot process logo image", exception);
        }
    }

    private static boolean isOpaqueWhite(int pixel) {
        return ((pixel >>> 24) & 255) == 255 && ((pixel >>> 16) & 255) >= WHITE_BACKGROUND_THRESHOLD
                && ((pixel >>> 8) & 255) >= WHITE_BACKGROUND_THRESHOLD && (pixel & 255) >= WHITE_BACKGROUND_THRESHOLD;
    }

}
