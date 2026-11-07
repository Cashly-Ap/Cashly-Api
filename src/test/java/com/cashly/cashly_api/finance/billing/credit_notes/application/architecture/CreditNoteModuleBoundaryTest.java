package com.cashly.cashly_api.finance.billing.credit_notes.application.architecture;

import com.cashly.cashly_api.finance.billing.credit_notes.application.create.use_case.CreateCreditNoteUseCase;
import com.cashly.cashly_api.finance.billing.credit_notes.application.delete.use_case.DeleteCreditNoteUseCase;
import com.cashly.cashly_api.finance.billing.credit_notes.application.get.use_case.GetCreditNoteByIdUseCase;
import com.cashly.cashly_api.finance.billing.credit_notes.application.list.use_case.GetCreditNotesUseCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditNoteModuleBoundaryTest {
    @Test
    void application_use_cases_depend_only_on_credit_note_owned_types() {
        Class<?>[] useCases = {CreateCreditNoteUseCase.class,
                DeleteCreditNoteUseCase.class, GetCreditNoteByIdUseCase.class,
                GetCreditNotesUseCase.class};

        for (Class<?> useCase : useCases) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes())
                        .allMatch(type -> (type.getPackageName().startsWith(
                                "com.cashly.cashly_api.finance.billing.credit_notes.") || type.getPackageName().startsWith("com.cashly.cashly_api.platform.notifications.application.ports.in"))),
                        useCase.getSimpleName()
                                + " imports an external module into its constructor");
            }
        }
    }
}
