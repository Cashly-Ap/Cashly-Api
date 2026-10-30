package com.cashly.cashly_api.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.i18n.MessageTranslator;
import org.springframework.validation.FieldError;

final class LocalizedValidationMessages {
    private LocalizedValidationMessages() { }
    static String translate(FieldError error, MessageTranslator translator) {
        String field = MessageTranslator.messageSource().getMessage("validation.fields." + error.getField(),
                null, error.getField(), MessageTranslator.currentLocale());
        String code = error.getCode() == null ? "" : error.getCode();
        if (error.getField().equals("language")) return translator.text("validation.unsupported_language");
        Object[] arguments = error.getArguments();
        Object last = arguments == null || arguments.length < 2 ? "" : arguments[arguments.length - 1];
        return switch (code) {
            case "NotNull", "NotBlank", "NotEmpty" -> translator.text("validation.constraint.required", field);
            case "Email" -> translator.text("validation.constraint.email", field);
            case "Size" -> translator.text("validation.constraint.size", field,
                    arguments != null && arguments.length > 2 ? arguments[2] : "0",
                    arguments != null && arguments.length > 1 ? arguments[1] : "");
            case "Min", "DecimalMin" -> translator.text("validation.constraint.minimum", field, last);
            case "Max", "DecimalMax" -> translator.text("validation.constraint.maximum", field, last);
            case "Positive" -> translator.text("validation.constraint.positive", field);
            case "PositiveOrZero" -> translator.text("validation.constraint.nonnegative", field);
            case "Negative" -> translator.text("validation.constraint.negative", field);
            case "NegativeOrZero" -> translator.text("validation.constraint.nonpositive", field);
            case "Past", "PastOrPresent" -> translator.text("validation.constraint.past", field);
            case "Future", "FutureOrPresent" -> translator.text("validation.constraint.future", field);
            default -> {
                String specific = error.getDefaultMessage() == null ? null
                        : BusinessErrorMessages.translate(error.getDefaultMessage(), translator);
                yield specific == null ? translator.text("errors.invalid_field", field) : specific;
            }
        };
    }
}
