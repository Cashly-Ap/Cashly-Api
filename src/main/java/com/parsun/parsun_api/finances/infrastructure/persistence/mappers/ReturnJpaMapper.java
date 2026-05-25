package com.parsun.parsun_api.finances.infrastructure.persistence.mappers;

import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.entities.ReturnLine;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnId;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnNumber;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnType;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnEntity;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnLineEntity;

public class ReturnJpaMapper {

    private ReturnJpaMapper() {}

    public static ReturnEntity toEntity(Return returnDomain) {
        ReturnEntity entity = new ReturnEntity();
        entity.setId(returnDomain.getId().value());
        entity.setReturnNumber(returnDomain.getReturnNumber().value());
        entity.setInvoiceId(returnDomain.getInvoiceId());
        entity.setCustomerId(returnDomain.getCustomerId());
        entity.setReturnType(returnDomain.getReturnType().name());
        entity.setReason(returnDomain.getReason());
        entity.setCreatedBy(returnDomain.getCreatedBy());
        entity.setCreatedByName(returnDomain.getCreatedByName());
        entity.setCreatedAt(returnDomain.getCreatedAt());
        entity.setUpdatedAt(returnDomain.getUpdatedAt());
        return entity;
    }

    public static Return toDomain(ReturnEntity entity) {
        return Return.reconstitute(
                ReturnId.of(entity.getId()),
                new ReturnNumber(entity.getReturnNumber()),
                entity.getInvoiceId(),
                entity.getCustomerId(),
                ReturnType.valueOf(entity.getReturnType()),
                entity.getReason(),
                entity.getCreatedBy(),
                entity.getCreatedByName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ReturnLineEntity toLineEntity(ReturnLine line) {
        ReturnLineEntity entity = new ReturnLineEntity();
        entity.setId(line.getId());
        entity.setReturnId(line.getReturnId());
        entity.setInvoiceLineId(line.getInvoiceLineId());
        entity.setProductId(line.getProductId());
        entity.setQuantity(line.getQuantity());
        entity.setWarehouseId(line.getWarehouseId());
        entity.setUnitPrice(line.getUnitPrice());
        entity.setLineTotal(line.getLineTotal());
        return entity;
    }

    public static ReturnLine toLineDomain(ReturnLineEntity entity) {
        return ReturnLine.reconstitute(
                entity.getId(),
                entity.getReturnId(),
                entity.getInvoiceLineId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getWarehouseId(),
                entity.getUnitPrice()
        );
    }
}
