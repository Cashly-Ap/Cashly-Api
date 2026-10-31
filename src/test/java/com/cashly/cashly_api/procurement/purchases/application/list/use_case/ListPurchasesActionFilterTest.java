package com.cashly.cashly_api.procurement.purchases.application.list.use_case;

import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ListPurchasesActionFilterTest {
    private final PurchaseRepository repository = mock(
            PurchaseRepository.class);
    private final ListPurchasesUseCase useCase = new ListPurchasesUseCase(
            repository);

    @Test
    void shouldQueryConfirmedPurchasesForCancellation() {
        useCase.execute(null, null, null, "ALL", null, null, "createdAt",
                "desc", DocumentAction.CANCEL);
        verify(repository).findAllWithFilters(null, null, null, "CONFIRMED",
                null, null, "createdAt", "desc");
    }

    @Test
    void shouldQueryDraftPurchasesForConfirmationAndEditing() {
        for (DocumentAction action : new DocumentAction[]{
                DocumentAction.CONFIRM, DocumentAction.EDIT}) {
            useCase.execute(null, null, null, null, null, null, "createdAt",
                    "desc", action);
        }
        verify(repository, times(2)).findAllWithFilters(null, null, null,
                "DRAFT", null, null, "createdAt", "desc");
    }

    @Test
    void shouldNotQueryAnIncompatibleStatus() {
        assertTrue(useCase.execute(null, null, null, "DRAFT", null, null,
                "createdAt", "desc", DocumentAction.CANCEL).isEmpty());
        verifyNoInteractions(repository);
    }
}
