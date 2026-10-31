package com.cashly.cashly_api.finance.billing.invoices.domain.repository;

import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(InvoiceId id);
    List<Invoice> findAllById(Collection<InvoiceId> ids);
    List<Invoice> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, List<String> statuses, List<String> returnStatus,
            String dueStatus, UUID productId, UUID categoryId, String orderBy,
            String orderDir);

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
