package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.ReturnDetailResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.entities.ReturnLine;
import com.parsun.parsun_api.finances.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnId;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseEntity;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories.SpringDataWarehouseRepository;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.ReturnLineJpaRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetReturnByIdUseCase {

    private final ReturnRepository returnRepository;
    private final ReturnLineJpaRepository returnLineJpaRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;
    private final SpringDataWarehouseRepository warehouseJpaRepository;

    public GetReturnByIdUseCase(ReturnRepository returnRepository,
                                ReturnLineJpaRepository returnLineJpaRepository,
                                InvoiceJpaRepository invoiceJpaRepository,
                                SpringDataWarehouseRepository warehouseJpaRepository) {
        this.returnRepository = returnRepository;
        this.returnLineJpaRepository = returnLineJpaRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
        this.warehouseJpaRepository = warehouseJpaRepository;
    }

    public ReturnDetailResponse execute(UUID returnId) {
        Return returnEntity = returnRepository.findById(ReturnId.of(returnId))
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

        List<ReturnLine> lines = returnLineJpaRepository.findByReturnId(returnId).stream()
                .map(com.parsun.parsun_api.finances.infrastructure.persistence.mappers.ReturnJpaMapper::toLineDomain)
                .toList();

        InvoiceEntity invoice = invoiceJpaRepository.findById(returnEntity.getInvoiceId())
                .orElse(null);

        String invoiceNumber = invoice != null ? invoice.getInvoiceNumber() : null;
        String customerName = invoice != null ? invoice.getCustomerName() : null;

        List<UUID> warehouseIds = lines.stream()
                .map(ReturnLine::getWarehouseId)
                .distinct()
                .toList();

        Map<UUID, String> warehouseNames = warehouseJpaRepository.findAllById(warehouseIds).stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        Map<UUID, String> productNames = invoice != null && invoice.getLines() != null
                ? invoice.getLines().stream()
                .collect(Collectors.toMap(
                        line -> line.getProductId(),
                        line -> line.getProductName(),
                        (existing, replacement) -> existing
                ))
                : Map.of();

        return FinanceMapper.toReturnDetailResponse(returnEntity, lines,
                invoiceNumber, customerName, warehouseNames, productNames);
    }
}
