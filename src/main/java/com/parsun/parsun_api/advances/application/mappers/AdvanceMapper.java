package com.parsun.parsun_api.advances.application.mappers;

import com.parsun.parsun_api.advances.application.dto.response.AdvanceConsumptionResponse;
import com.parsun.parsun_api.advances.application.dto.response.AdvanceListResponse;
import com.parsun.parsun_api.advances.application.dto.response.AdvanceResponse;
import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.entities.AdvanceConsumption;

import java.util.Collections;
import java.util.List;

public class AdvanceMapper {

    private AdvanceMapper() {}

    public static AdvanceResponse toResponse(Advance advance) {
        return toResponse(advance, Collections.emptyList());
    }

    public static AdvanceResponse toResponse(Advance advance, List<AdvanceConsumption> consumptions) {
        List<AdvanceConsumptionResponse> consumptionResponses = consumptions != null
                ? consumptions.stream().map(AdvanceMapper::toConsumptionResponse).toList()
                : Collections.emptyList();

        return new AdvanceResponse(
                advance.getId().value(),
                advance.getAdvanceNumber().value(),
                advance.getCustomerId(),
                advance.getAmount(),
                advance.getRemainingAmount(),
                advance.getStatus().name(),
                advance.getNotes(),
                advance.getCreatedBy(),
                advance.getCreatedAt(),
                advance.getUpdatedAt(),
                consumptionResponses
        );
    }

    public static AdvanceListResponse toListResponse(Advance advance) {
        return new AdvanceListResponse(
                advance.getId().value(),
                advance.getAdvanceNumber().value(),
                advance.getAmount(),
                advance.getRemainingAmount(),
                advance.getStatus().name(),
                advance.getNotes(),
                advance.getCreatedAt()
        );
    }

    public static AdvanceConsumptionResponse toConsumptionResponse(AdvanceConsumption consumption) {
        return new AdvanceConsumptionResponse(
                consumption.getId(),
                consumption.getAdvanceId(),
                consumption.getInvoiceId(),
                consumption.getAmount(),
                consumption.getPaymentId(),
                consumption.getCreatedAt()
        );
    }
}
