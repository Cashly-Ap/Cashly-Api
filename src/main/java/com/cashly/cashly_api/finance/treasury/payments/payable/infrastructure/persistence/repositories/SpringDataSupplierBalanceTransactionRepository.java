package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.repositories;

import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierBalanceTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataSupplierBalanceTransactionRepository
        extends JpaRepository<SupplierBalanceTransactionEntity, UUID> {

    Page<SupplierBalanceTransactionEntity> findBySupplierIdOrderByCreatedAtDesc(
            UUID supplierId, Pageable pageable);
}
