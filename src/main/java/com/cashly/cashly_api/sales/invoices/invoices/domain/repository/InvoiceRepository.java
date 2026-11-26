package com.cashly.cashly_api.sales.invoices.invoices.domain.repository;

import com.cashly.cashly_api.sales.invoices.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.sales.invoices.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {

    record InvoiceHeader(UUID id, UUID customerId, String number, String status,
            BigDecimal subtotal, BigDecimal returnedTotal, LocalDate dueDate) {
    }

    record ReportRow(UUID id, String number, String customerName,
            String paymentType, String status, BigDecimal total,
            LocalDateTime createdAt, long lineCount) {
    }

    List<ReportRow> findReportRows(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, List<String> statuses, UUID productId,
            UUID categoryId);

    List<InvoiceHeader> findHeaders(UUID customerId, List<String> statuses);
    List<InvoiceHeader> findAllHeaders();
    void updateStatuses(Collection<UUID> invoiceIds, String status);
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(InvoiceId id);
    List<Invoice> findAllById(Collection<InvoiceId> ids);
    List<Invoice> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, List<String> statuses, List<String> returnStatus,
            String dueStatus, UUID productId, UUID categoryId, String orderBy,
            String orderDir);

    PageResponse<Invoice> findPageWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            String search, List<String> returnStatus, String dueStatus,
            String paymentType, UUID productId, UUID categoryId, int page,
            int size, String orderBy, String orderDir);

    default List<Invoice> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            List<String> returnStatus, UUID productId, UUID categoryId,
            String orderBy, String orderDir) {
        return findAllWithFilters(dateFrom, dateTo, customerId, statuses,
                returnStatus, null, productId, categoryId, orderBy, orderDir);
    }

    default List<Invoice> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            String search, List<String> returnStatus, UUID productId,
            UUID categoryId, String orderBy, String orderDir) {
        return findAllWithFilters(dateFrom, dateTo, customerId, statuses,
                search, returnStatus, null, productId, categoryId, orderBy,
                orderDir);
    }

    default List<Invoice> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            String search, List<String> returnStatus, String dueStatus,
            UUID productId, UUID categoryId, String orderBy, String orderDir) {
        String normalizedSearch = search == null
                ? ""
                : search.trim().toLowerCase();
        return findAllWithFilters(dateFrom, dateTo, customerId, statuses,
                returnStatus, dueStatus, productId, categoryId, orderBy,
                orderDir)
                .stream()
                .filter(invoice -> normalizedSearch.isBlank()
                        || invoice.getInvoiceNumber().value().toLowerCase()
                                .contains(normalizedSearch)
                        || invoice.getCustomerName().toLowerCase()
                                .contains(normalizedSearch)
                        || invoice.getCustomerRif().toLowerCase()
                                .contains(normalizedSearch))
                .toList();
    }
    void deleteById(InvoiceId id);
    int getNextSequenceForYear(int year);
}
