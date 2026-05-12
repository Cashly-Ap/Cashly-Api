package com.parsun.parsun_api.invoices.domain.repositories;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(InvoiceId id);
    List<Invoice> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                     UUID customerId, String status);
    void deleteById(InvoiceId id);
    int getNextSequenceForYear(int year);
}
