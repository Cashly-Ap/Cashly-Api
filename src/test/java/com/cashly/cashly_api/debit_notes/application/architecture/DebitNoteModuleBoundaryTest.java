package com.cashly.cashly_api.debit_notes.application.architecture;

import com.cashly.cashly_api.debit_notes.application.create.use_case.CreateDebitNoteUseCase;
import com.cashly.cashly_api.debit_notes.application.delete.use_case.DeleteDebitNoteUseCase;
import com.cashly.cashly_api.debit_notes.application.get.use_case.GetDebitNoteByIdUseCase;
import com.cashly.cashly_api.debit_notes.application.list.use_case.GetDebitNotesUseCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DebitNoteModuleBoundaryTest {
    @Test
    void application_use_cases_depend_only_on_debit_note_owned_types() {
        Class<?>[] useCases = {CreateDebitNoteUseCase.class, DeleteDebitNoteUseCase.class,
                GetDebitNoteByIdUseCase.class, GetDebitNotesUseCase.class};

        for (Class<?> useCase : useCases) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type ->
                                type.getPackageName().startsWith("com.cashly.cashly_api.debit_notes.")),
                        useCase.getSimpleName() + " imports an external module into its constructor");
            }
        }
    }
}
