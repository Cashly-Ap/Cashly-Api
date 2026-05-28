package com.parsun.parsun_api.receivables.application.mappers;

import com.parsun.parsun_api.receivables.application.dto.response.ReceivableResponse;
import com.parsun.parsun_api.receivables.domain.entities.Receivable;
import com.parsun.parsun_api.receivables.domain.valueobjects.ReceivableStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReceivableMapper {

    private ReceivableMapper() {}

    public static ReceivableResponse toResponse(Receivable receivable, String invoiceNumber,
                                                  UUID customerId, String customerName) {
        return new ReceivableResponse(
                receivable.getId().value(),
                receivable.getInvoiceId().value(),
                invoiceNumber,
                customerId,
                customerName,
                receivable.getTotalAmount(),
                receivable.getPaidAmount(),
                receivable.getBalance(),
                receivable.getStatus().name(),
                receivable.getDueDate(),
                receivable.getCreatedAt()
        );
    }

    public static ReceivableResponse toResponse(Receivable receivable) {
        return toResponse(receivable, null, null, null);
    }

    public static ReceivableResponse.AgingReport toAgingReport(List<Receivable> receivables,
                                                                  Map<UUID, String> invoiceNumbers,
                                                                  Map<UUID, UUID> invoiceCustomerIds,
                                                                  Map<UUID, String> customerNames) {
        Map<String, List<Receivable>> buckets = new LinkedHashMap<>();
        buckets.put("0-30", new ArrayList<>());
        buckets.put("31-60", new ArrayList<>());
        buckets.put("61-90", new ArrayList<>());
        buckets.put("90+", new ArrayList<>());

        LocalDate today = LocalDate.now();

        for (Receivable receivable : receivables) {
            if (receivable.getStatus() == ReceivableStatus.PAID
                    || receivable.getStatus() == ReceivableStatus.WRITTEN_OFF) {
                continue;
            }

            long daysOverdue = ChronoUnit.DAYS.between(receivable.getDueDate(), today);
            if (daysOverdue < 0) daysOverdue = 0;

            String bucketKey;
            if (daysOverdue <= 30) bucketKey = "0-30";
            else if (daysOverdue <= 60) bucketKey = "31-60";
            else if (daysOverdue <= 90) bucketKey = "61-90";
            else bucketKey = "90+";

            buckets.get(bucketKey).add(receivable);
        }

        List<ReceivableResponse.AgingBucket> agingBuckets = buckets.entrySet().stream()
                .map(entry -> {
                    String label = entry.getKey();
                    List<Receivable> bucketReceivables = entry.getValue();
                    BigDecimal total = bucketReceivables.stream()
                            .map(Receivable::getBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int minDays = switch (label) {
                        case "0-30" -> 0;
                        case "31-60" -> 31;
                        case "61-90" -> 61;
                        default -> 91;
                    };

                    int maxDays = switch (label) {
                        case "0-30" -> 30;
                        case "31-60" -> 60;
                        case "61-90" -> 90;
                        default -> Integer.MAX_VALUE;
                    };

                    List<ReceivableResponse> responses = bucketReceivables.stream()
                            .map(r -> toResponse(r,
                                    invoiceNumbers.get(r.getInvoiceId().value()),
                                    invoiceCustomerIds.get(r.getInvoiceId().value()),
                                    customerNames.get(r.getInvoiceId().value())))
                            .toList();

                    return new ReceivableResponse.AgingBucket(
                            label, minDays, maxDays, total, bucketReceivables.size(), responses
                    );
                })
                .toList();

        return new ReceivableResponse.AgingReport(agingBuckets, LocalDate.now());
    }
}
