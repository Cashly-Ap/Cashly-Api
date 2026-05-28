package com.parsun.parsun_api.returns.application.mappers;

import com.parsun.parsun_api.returns.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.returns.application.dto.response.ReturnListResponse;
import com.parsun.parsun_api.returns.application.dto.response.ReturnResponse;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnLineAllocation;

import java.util.List;

public class ReturnMapper {

    private ReturnMapper() {}

    public static ReturnResponse toResponse(Return return_) {
        List<ReturnResponse.ReturnLineResponse> lineResponses = return_.getLines().stream()
                .map(ReturnMapper::toLineResponse)
                .toList();

        return new ReturnResponse(
                return_.getId().value(),
                return_.getReturnNumber().value(),
                return_.getInvoiceId().value(),
                return_.getCustomerId(),
                return_.getCustomerName(),
                return_.getCustomerRif(),
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

    public static ReturnListResponse toListResponse(Return return_) {
        return new ReturnListResponse(
                return_.getId().value(),
                return_.getReturnNumber().value(),
                return_.getInvoiceId().value(),
                return_.getCustomerId(),
                return_.getCustomerName(),
                return_.getReturnType().name(),
                return_.getReason(),
                return_.getTotal(),
                return_.getLines().size(),
                return_.getCreatedAt()
        );
    }

    private static ReturnResponse.ReturnLineResponse toLineResponse(ReturnLine line) {
        List<ReturnResponse.ReturnLineResponse.AllocationResponse> allocationResponses = line.getAllocations().stream()
                .map(alloc -> new ReturnResponse.ReturnLineResponse.AllocationResponse(
                        alloc.getWarehouseId(),
                        alloc.getAllocated()
                ))
                .toList();

        return new ReturnResponse.ReturnLineResponse(
                line.getProductId(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getSubtotal(),
                allocationResponses
        );
    }

    public static ReturnLine toLine(CreateReturnRequest.ReturnLineRequest request) {
        List<ReturnLineAllocation> allocations = request.allocations().stream()
                .map(alloc -> ReturnLineAllocation.create(alloc.warehouseId(), alloc.allocated()))
                .toList();

        return ReturnLine.create(
                request.productId(),
                request.quantity(),
                request.unitPrice(),
                request.subtotal(),
                allocations
        );
    }
}
