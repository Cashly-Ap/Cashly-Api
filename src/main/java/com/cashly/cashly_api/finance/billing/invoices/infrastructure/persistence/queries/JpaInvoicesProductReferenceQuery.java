package com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.queries;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.repositories.SpringDataInvoicesProductReferenceRepository;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.ProductReferenceQuery;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaInvoicesProductReferenceQuery implements ProductReferenceQuery {
    private final SpringDataInvoicesProductReferenceRepository references;
    public JpaInvoicesProductReferenceQuery(SpringDataInvoicesProductReferenceRepository references) {
        this.references = references;
    }
    @Override
    public Set<UUID> findReferencedProductIds(Set<UUID> ids) {
        return ids.isEmpty() ? Set.of() : Set.copyOf(references.findReferencedProductIds(ids));
    }
}