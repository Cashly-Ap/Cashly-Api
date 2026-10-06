package com.cashly.cashly_api.returns.application.mappers;

import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.returns.application.dto.request.CreateReturnRequest;
import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.returns.domain.entities.Return;
import com.cashly.cashly_api.returns.domain.entities.ReturnLine;
import com.cashly.cashly_api.returns.domain.entities.ReturnLineAllocation;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReturnMapper {

    private final InvoiceRepository invoiceRepository;
    private final WarehouseRepository warehouseRepository;

    public ReturnMapper(InvoiceRepository invoiceRepository, WarehouseRepository warehouseRepository) {
        this.invoiceRepository = invoiceRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public ReturnResponse toResponse(Return return_) {
        var invoiceOpt = invoiceRepository.findById(return_.getInvoiceId());
        String invoiceNumber = invoiceOpt.map(inv -> inv.getInvoiceNumber().value()).orElse("");
        String customerName = invoiceOpt.map(inv -> inv.getCustomerName()).orElse("");
        String customerRif = invoiceOpt.map(inv -> inv.getCustomerRif()).orElse("");

        List<ReturnResponse.ReturnLineResponse> lineResponses = return_.getLines().stream()
                .map(this::toLineResponse)
                .toList();

        return new ReturnResponse(
                return_.getId().value(),
                return_.getReturnNumber().value(),
                return_.getInvoiceId().value(),
                invoiceNumber,
                return_.getCustomerId(),
                customerName,
                customerRif,
                return_.getReturnType().name(),
                return_.getReason(),
                return_.getTotal(),
                return_.getCreatedBy(),
                return_.getCreatedByName(),
                return_.getCreatedAt(),
                return_.getUpdatedAt(),
                lineResponses
        );
    }

    public ReturnListResponse toListResponse(Return return_) {
        var invoiceOpt = invoiceRepository.findById(return_.getInvoiceId());
        String invoiceNumber = invoiceOpt.map(inv -> inv.getInvoiceNumber().value()).orElse("");
        String customerName = invoiceOpt.map(inv -> inv.getCustomerName()).orElse("");

        return new ReturnListResponse(
                return_.getId().value(),
                return_.getReturnNumber().value(),
                return_.getInvoiceId().value(),
                invoiceNumber,
                return_.getCustomerId(),
                customerName,
                return_.getReturnType().name(),
                return_.getReason(),
                return_.getTotal(),
                return_.getLines().size(),
                return_.getCreatedAt()
        );
    }

    private ReturnResponse.ReturnLineResponse toLineResponse(ReturnLine line) {
        List<ReturnResponse.ReturnLineResponse.AllocationResponse> allocationResponses = line.getAllocations().stream()
                .map(this::toAllocationResponse)
                .toList();

        return new ReturnResponse.ReturnLineResponse(
                line.getProductId(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getSubtotal(),
                allocationResponses
        );
    }

    private ReturnResponse.ReturnLineResponse.AllocationResponse toAllocationResponse(ReturnLineAllocation alloc) {
        String warehouseName = warehouseRepository.findById(alloc.getWarehouseId())
                .map(wh -> wh.getName())
                .orElse("");
        return new ReturnResponse.ReturnLineResponse.AllocationResponse(
                alloc.getWarehouseId(),
                warehouseName,
                alloc.getAllocated()
        );
    }

    public static ReturnLine toLine(CreateReturnRequest.ReturnLineRequest request) {
        List<ReturnLineAllocation> allocations = request.allocations().stream()
                .map(alloc -> ReturnLineAllocation.create(alloc.warehouseId(), alloc.allocated()))
                .toList();

        return ReturnLine.create(
                request.invoiceLineId(),
                request.productId(),
                request.quantity(),
                request.unitPrice(),
                request.subtotal(),
                allocations
        );
    }
}
