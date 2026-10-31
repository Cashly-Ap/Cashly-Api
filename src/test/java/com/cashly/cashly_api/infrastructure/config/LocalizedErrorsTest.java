package com.cashly.cashly_api.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.Locale;
import static org.assertj.core.api.Assertions.*;

class LocalizedErrorsTest {
    @AfterEach
    void reset() {
        LocaleContextHolder.resetLocaleContext();
    }
    @Test
    void sameErrorHasStableCodeAndTranslatedMessages() {
        var handler = new GlobalExceptionHandler();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        var english = handler
                .handleIllegalArgument(
                        new IllegalArgumentException("Unsupported language"))
                .getBody();
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        var spanish = handler
                .handleIllegalArgument(
                        new IllegalArgumentException("Unsupported language"))
                .getBody();
        assertThat(english.get("code")).isEqualTo(spanish.get("code"))
                .isEqualTo("INVALID_REQUEST");
        assertThat(english.get("message"))
                .isEqualTo("Language must be Spanish (es) or English (en)");
        assertThat(spanish.get("message"))
                .isEqualTo("El idioma debe ser español (es) o inglés (en)");
    }
    @Test
    void preservesAvailableAndRequestedStockInBothLanguages() {
        var handler = new GlobalExceptionHandler();
        var error = new com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.InsufficientStockException(
                java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 7,
                12);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertThat(
                handler.handleInsufficientStock(error).getBody().get("message"))
                .isEqualTo("Insufficient stock: available 7, requested 12");
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        assertThat(
                handler.handleInsufficientStock(error).getBody().get("message"))
                .isEqualTo("Stock insuficiente: disponible 7, solicitado 12");
    }
    @Test
    void unexpectedTechnicalDetailsAreNotExposedToUsers() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        var body = new GlobalExceptionHandler()
                .handleGenericException(
                        new Exception("Internal SQL connection detail"))
                .getBody();
        assertThat(body.get("message"))
                .isEqualTo("Se produjo un error interno. Inténtalo de nuevo");
        assertThat(body.get("code")).isEqualTo("INTERNAL_SERVER_ERROR");
    }
    @Test
    void businessRulesKeepTheirSpecificMeaningInEachLanguage() {
        var handler = new GlobalExceptionHandler();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertThat(handler
                .handleIllegalState(new IllegalStateException(
                        "Only DRAFT invoices can be confirmed"))
                .getBody().get("message"))
                .isEqualTo("Only DRAFT invoices can be confirmed");
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        assertThat(handler
                .handleIllegalState(new IllegalStateException(
                        "Only DRAFT invoices can be confirmed"))
                .getBody().get("message"))
                .isEqualTo("Solo se pueden confirmar facturas en borrador");
        assertThat(handler
                .handleIllegalArgument(new IllegalArgumentException(
                        "Quantity must be greater than zero"))
                .getBody().get("message"))
                .isEqualTo("La cantidad debe ser mayor que cero");
    }
    @Test
    void amountAndLengthParametersSurviveTranslation() {
        var handler = new GlobalExceptionHandler();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        var returned = new com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.ReturnQuantityExceedsException(
                java.util.UUID.randomUUID(), new java.math.BigDecimal("12"),
                new java.math.BigDecimal("7"));
        assertThat(handler.handleReturnQuantityExceeds(returned).getBody()
                .get("message")).isEqualTo(
                        "Returned quantity (12) exceeds the original quantity (7)");
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        assertThat(handler
                .handleIllegalArgument(new IllegalArgumentException(
                        "User name cannot exceed 100 characters"))
                .getBody().get("message")).isEqualTo(
                        "El nombre de usuario no puede superar los 100 caracteres");
    }
    @Test
    void beanValidationPreservesConstraintAndFieldMeaning() {
        var field = new org.springframework.validation.FieldError("request",
                "firstName", "", false, new String[]{"NotBlank"}, null,
                "must not be blank");
        var translator = new com.cashly.cashly_api.shared.infrastructure.i18n.MessageTranslator();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertThat(LocalizedValidationMessages.translate(field, translator))
                .isEqualTo("The first name field is required");
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        assertThat(LocalizedValidationMessages.translate(field, translator))
                .isEqualTo("El campo nombre es obligatorio");
    }

    @Test
    void fileUploadMessagesAreTranslatedWithoutExposingTechnicalDetails() {
        var handler = new GlobalExceptionHandler();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertThat(handler.handleIllegalArgument(
                new IllegalArgumentException("Invalid PNG or JPEG image"))
                .getBody().get("message"))
                .isEqualTo("The image is invalid or cannot be read");
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        var response = handler.handleUploadTooLarge(
                new org.springframework.web.multipart.MaxUploadSizeExceededException(
                        100));
        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody().get("message"))
                .isEqualTo("El archivo supera el tamaño máximo permitido");
    }

}
