package com.cashly.cashly_api.returns.application.mappers;

import com.cashly.cashly_api.returns.application.ports.out.ReturnProjectionQueryGateway.InvoiceLabels;
import com.cashly.cashly_api.returns.application.create.request.CreateReturnRequest;
import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.returns.domain.entity.Return;
import com.cashly.cashly_api.returns.domain.entity.ReturnLine;
import com.cashly.cashly_api.returns.domain.entity.ReturnLineAllocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ReturnMapper {

    public ReturnResponse toResponse(Return return_, InvoiceLabels invoice, Map<UUID, String> warehouseNames) {
        String invoiceNumber = invoice != null ? invoice.number() : "";
        String customerName = invoice != null ? invoice.customerName() : "";
        String customerRif = invoice != null ? invoice.customerRif() : "";

        List<ReturnResponse.ReturnLineResponse> lineResponses = return_.getLines().stream()
                .map(line -> toLineResponse(line, warehouseNames))
                .toList();

        return new ReturnResponse(
                return_.getId().value(),
                return_.getReturnNumber().value(),
                return_.getInvoiceId(),
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

    public ReturnListResponse toListResponse(Return return_, InvoiceLabels invoice) {
        String invoiceNumber = invoice != null ? invoice.number() : "";
        String customerName = invoice != null ? invoice.customerName() : "";

        return new ReturnListResponse(
                return_.getId().value(),
                return_.getReturnNumber().value(),
                return_.getInvoiceId(),
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

    private ReturnResponse.ReturnLineResponse toLineResponse(ReturnLine line, Map<UUID, String> warehouseNames) {
        List<ReturnResponse.ReturnLineResponse.AllocationResponse> allocationResponses = line.getAllocations().stream()
                .map(allocation -> toAllocationResponse(allocation, warehouseNames))
                .toList();

        return new ReturnResponse.ReturnLineResponse(
                line.getProductId(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getSubtotal(),
                allocationResponses
        );
    }

    private ReturnResponse.ReturnLineResponse.AllocationResponse toAllocationResponse(
            ReturnLineAllocation alloc, Map<UUID, String> warehouseNames) {
        String warehouseName = warehouseNames.getOrDefault(alloc.getWarehouseId(), "");
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
