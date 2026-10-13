package com.cashly.cashly_api.returns.application.services;

import com.cashly.cashly_api.returns.application.ports.out.ReturnProjectionQueryGateway;
import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnLineAllocation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ReturnResponseAssembler {
    private final ReturnProjectionQueryGateway projections;
    private final ReturnMapper mapper;

    public ReturnResponseAssembler(ReturnProjectionQueryGateway projections, ReturnMapper mapper) {
        this.projections = projections;
        this.mapper = mapper;
    }

    public ReturnResponse toResponse(Return return_) {
        var invoice = projections.findInvoice(return_.getInvoiceId()).orElse(null);
        Map<UUID, String> warehouseNames = return_.getLines().stream()
                .flatMap(line -> line.getAllocations().stream())
                .map(ReturnLineAllocation::getWarehouseId)
                .distinct()
                .map(id -> Map.entry(id, projections.findWarehouseName(id).orElse("")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return mapper.toResponse(return_, invoice, warehouseNames);
    }

    public ReturnListResponse toListResponse(Return return_) {
        var invoice = projections.findInvoice(return_.getInvoiceId()).orElse(null);
        return mapper.toListResponse(return_, invoice);
    }
}
