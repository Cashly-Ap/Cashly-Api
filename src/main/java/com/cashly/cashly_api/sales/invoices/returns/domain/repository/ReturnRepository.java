package com.cashly.cashly_api.sales.invoices.returns.domain.repository;

import com.cashly.cashly_api.sales.invoices.returns.domain.entity.Return;
import com.cashly.cashly_api.sales.invoices.returns.domain.valueobjects.ReturnId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRepository {

    Return save(Return return_);

    Optional<Return> findById(ReturnId id);

    List<Return> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, UUID invoiceId);

    void delete(ReturnId id);

    int getNextSequenceForYear(int year);
}
