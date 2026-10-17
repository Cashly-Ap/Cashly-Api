package com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.repositories;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Set;
import java.util.UUID;

public interface SpringDataInvoicesProductReferenceRepository extends Repository<InvoiceLineEntity, UUID> {
    @Query(value = "SELECT DISTINCT BIN_TO_UUID(product_id) FROM invoice_lines WHERE product_id IN :ids", nativeQuery = true)
    Set<UUID> findReferencedProductIds(@Param("ids") Set<UUID> ids);
}