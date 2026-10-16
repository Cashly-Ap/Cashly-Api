package com.cashly.cashly_api.returns.domain;

import com.cashly.cashly_api.returns.domain.Return;
import com.cashly.cashly_api.returns.domain.ReturnId;

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
