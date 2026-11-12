package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.mappers;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPayment;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.entity.SupplierPaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentId;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentSource;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentStatus;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierPaymentAllocationEntity;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierPaymentEntity;

public class SupplierPaymentJpaMapper {

    private SupplierPaymentJpaMapper() {
    }

    public static SupplierPaymentEntity toEntity(SupplierPayment payment) {
        SupplierPaymentEntity entity = new SupplierPaymentEntity();
        entity.setId(payment.getId().value());
        entity.setPaymentNumber(payment.getPaymentNumber().value());
        entity.setSupplierId(payment.getSupplierId());
        entity.setAmount(payment.getAmount());
        entity.setPaymentMethodId(payment.getPaymentMethodId());
        entity.setSource(payment.getSource().name());
        entity.setReferenceNumber(payment.getReferenceNumber());
        entity.setPaymentDate(payment.getPaymentDate());
        entity.setNotes(payment.getNotes());
        entity.setStatus(payment.getStatus().name());
        entity.setAppliedAmount(payment.getAppliedAmount());
        entity.setCreatedBy(payment.getCreatedBy());
        entity.setCreatedByName(payment.getCreatedByName());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        entity.setCancelledReason(payment.getCancelledReason());
        entity.setVerifiedBy(payment.getVerifiedBy());
        entity.setVerifiedByName(payment.getVerifiedByName());
        entity.setVerifiedAt(payment.getVerifiedAt());
        return entity;
    }

    public static SupplierPayment toDomain(SupplierPaymentEntity entity) {
        return SupplierPayment.reconstitute(
                SupplierPaymentId.of(entity.getId()),
                new SupplierPaymentNumber(entity.getPaymentNumber()),
                entity.getSupplierId(), entity.getAmount(),
                entity.getPaymentMethodId(),
                SupplierPaymentSource.valueOf(entity.getSource()),
                entity.getReferenceNumber(), entity.getPaymentDate(),
                entity.getNotes(),
                SupplierPaymentStatus.valueOf(entity.getStatus()),
                entity.getAppliedAmount(), entity.getCreatedBy(),
                entity.getCreatedByName(), entity.getCreatedAt(),
                entity.getUpdatedAt(), entity.getCancelledReason(),
                entity.getVerifiedBy(), entity.getVerifiedByName(),
                entity.getVerifiedAt());
    }

    public static SupplierPaymentAllocationEntity toEntity(
            SupplierPaymentAllocation allocation) {
        SupplierPaymentAllocationEntity entity = new SupplierPaymentAllocationEntity();
        entity.setId(allocation.getId());
        entity.setPaymentId(allocation.getPaymentId());
        entity.setPurchaseId(allocation.getPurchaseId());
        entity.setAllocatedAmount(allocation.getAllocatedAmount());
        entity.setCreatedAt(allocation.getCreatedAt());
        return entity;
    }

    public static SupplierPaymentAllocation toDomain(
            SupplierPaymentAllocationEntity entity) {
        return SupplierPaymentAllocation.reconstitute(entity.getId(),
                entity.getPaymentId(), entity.getPurchaseId(),
                entity.getAllocatedAmount(), entity.getCreatedAt());
    }
}
