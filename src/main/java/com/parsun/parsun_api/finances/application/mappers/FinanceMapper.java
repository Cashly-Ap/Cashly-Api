package com.parsun.parsun_api.finances.application.mappers;

import com.parsun.parsun_api.finances.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.finances.application.dto.response.PaymentDetailResponse;
import com.parsun.parsun_api.finances.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.dto.response.ReturnDetailResponse;
import com.parsun.parsun_api.finances.application.dto.response.ReturnListResponse;
import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.entities.Payment;
import com.parsun.parsun_api.finances.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.entities.ReturnLine;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FinanceMapper {

    private FinanceMapper() {}

    public static PaymentMethodResponse toPaymentMethodResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId().value(),
                paymentMethod.getCode(),
                paymentMethod.getName(),
                paymentMethod.getDescription(),
                paymentMethod.isActive(),
                paymentMethod.getCreatedAt(),
                paymentMethod.getUpdatedAt()
        );
    }

    public static List<PaymentMethodResponse> toPaymentMethodResponseList(List<PaymentMethod> paymentMethods) {
        return paymentMethods.stream()
                .map(FinanceMapper::toPaymentMethodResponse)
                .toList();
    }

    public static PaymentListResponse toPaymentListResponse(Payment payment, String paymentMethodName) {
        return new PaymentListResponse(
                payment.getId().value(),
                payment.getPaymentNumber().value(),
                payment.getCustomerId(),
                null,
                payment.getType().name(),
                payment.getAmount(),
                paymentMethodName,
                payment.getStatus().name(),
                payment.getAppliedAmount(),
                payment.getPaymentDate(),
                payment.getCreatedAt()
        );
    }

    public static PaymentDetailResponse toPaymentDetailResponse(Payment payment,
                                                                 List<PaymentAllocation> allocations,
                                                                 String paymentMethodName,
                                                                 String customerName) {
        List<PaymentDetailResponse.AllocationResponse> allocationResponses = allocations.stream()
                .map(a -> new PaymentDetailResponse.AllocationResponse(
                        a.getId(),
                        a.getInvoiceId(),
                        a.getAllocatedAmount(),
                        a.getCreatedAt()
                ))
                .toList();

        return new PaymentDetailResponse(
                payment.getId().value(),
                payment.getPaymentNumber().value(),
                payment.getCustomerId(),
                customerName,
                payment.getType().name(),
                payment.getAmount(),
                payment.getPaymentMethodId(),
                paymentMethodName,
                payment.getReferenceNumber(),
                payment.getPaymentDate(),
                payment.getNotes(),
                payment.getStatus().name(),
                payment.getAppliedAmount(),
                payment.getCreatedBy(),
                payment.getCreatedByName(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                allocationResponses
        );
    }

    public static CreditNoteResponse toCreditNoteResponse(CreditNote creditNote,
                                                           String customerName,
                                                           String customerRif,
                                                           String invoiceNumber) {
        return new CreditNoteResponse(
                creditNote.getId().value(),
                creditNote.getNoteNumber().value(),
                creditNote.getCustomerId(),
                customerName,
                customerRif,
                creditNote.getInvoiceId(),
                invoiceNumber,
                creditNote.getAmount(),
                creditNote.getReason(),
                creditNote.getStatus().name(),
                creditNote.getAppliedAmount(),
                creditNote.getCreatedBy(),
                creditNote.getCreatedByName(),
                creditNote.getCreatedAt(),
                creditNote.getUpdatedAt()
        );
    }

    public static DebitNoteResponse toDebitNoteResponse(DebitNote debitNote,
                                                         String customerName,
                                                         String customerRif,
                                                         String invoiceNumber) {
        return new DebitNoteResponse(
                debitNote.getId().value(),
                debitNote.getNoteNumber().value(),
                debitNote.getCustomerId(),
                customerName,
                customerRif,
                debitNote.getInvoiceId(),
                invoiceNumber,
                debitNote.getAmount(),
                debitNote.getReason(),
                debitNote.getStatus().name(),
                debitNote.getAppliedAmount(),
                debitNote.getCreatedBy(),
                debitNote.getCreatedByName(),
                debitNote.getCreatedAt(),
                debitNote.getUpdatedAt()
        );
    }

    public static ReturnListResponse toReturnListResponse(Return returnEntity,
                                                           String invoiceNumber,
                                                           String customerName) {
        return new ReturnListResponse(
                returnEntity.getId().value(),
                returnEntity.getReturnNumber().value(),
                invoiceNumber,
                customerName,
                returnEntity.getReturnType().name(),
                returnEntity.getReason(),
                returnEntity.getCreatedAt()
        );
    }

    public static ReturnDetailResponse toReturnDetailResponse(Return returnEntity,
                                                               List<ReturnLine> lines,
                                                               String invoiceNumber,
                                                               String customerName,
                                                               Map<UUID, String> warehouseNames,
                                                               Map<UUID, String> productNames) {
        List<ReturnDetailResponse.ReturnLineResponse> lineResponses = lines.stream()
                .map(line -> new ReturnDetailResponse.ReturnLineResponse(
                        line.getId(),
                        line.getInvoiceLineId(),
                        line.getProductId(),
                        productNames.getOrDefault(line.getProductId(), null),
                        line.getQuantity(),
                        line.getWarehouseId(),
                        warehouseNames != null ? warehouseNames.getOrDefault(line.getWarehouseId(), null) : null,
                        line.getUnitPrice(),
                        line.getLineTotal()
                ))
                .toList();

        return new ReturnDetailResponse(
                returnEntity.getId().value(),
                returnEntity.getReturnNumber().value(),
                returnEntity.getInvoiceId(),
                invoiceNumber,
                returnEntity.getCustomerId(),
                customerName,
                returnEntity.getReturnType().name(),
                returnEntity.getReason(),
                null,
                returnEntity.getCreatedBy(),
                returnEntity.getCreatedByName(),
                returnEntity.getCreatedAt(),
                returnEntity.getUpdatedAt(),
                lineResponses
        );
    }

    public static ReturnDetailResponse toReturnDetailResponse(Return returnEntity,
                                                               List<ReturnLine> lines,
                                                               String invoiceNumber,
                                                               String customerName,
                                                               Map<UUID, String> warehouseNames) {
        return toReturnDetailResponse(returnEntity, lines, invoiceNumber, customerName, warehouseNames, Map.of());
    }
}
