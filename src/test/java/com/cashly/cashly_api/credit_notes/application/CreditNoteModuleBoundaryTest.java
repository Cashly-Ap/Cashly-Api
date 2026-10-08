package com.cashly.cashly_api.credit_notes.application;

import com.cashly.cashly_api.credit_notes.application.usecases.CreateCreditNoteUseCase;
import com.cashly.cashly_api.credit_notes.application.usecases.DeleteCreditNoteUseCase;
import com.cashly.cashly_api.credit_notes.application.usecases.GetCreditNoteByIdUseCase;
import com.cashly.cashly_api.credit_notes.application.usecases.GetCreditNotesUseCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditNoteModuleBoundaryTest {
    @Test
    void application_use_cases_depend_only_on_credit_note_owned_types() {
        Class<?>[] useCases = {CreateCreditNoteUseCase.class, DeleteCreditNoteUseCase.class,
                GetCreditNoteByIdUseCase.class, GetCreditNotesUseCase.class};

        for (Class<?> useCase : useCases) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type ->
                                type.getPackageName().startsWith("com.cashly.cashly_api.credit_notes.")),
                        useCase.getSimpleName() + " imports an external module into its constructor");
            }
        }
    }
}
