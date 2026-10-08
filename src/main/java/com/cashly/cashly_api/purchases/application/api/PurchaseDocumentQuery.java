package com.cashly.cashly_api.purchases.application.api;

import java.util.List;
import java.util.UUID;

public interface PurchaseDocumentQuery {
    PurchaseDocumentSnapshot find(UUID id);
    List<PurchaseDocumentRow> findAll(PurchaseDocumentFilter filter);
}
