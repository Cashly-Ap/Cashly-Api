package com.parsun.parsun_api.finances.domain.repositories;

import com.parsun.parsun_api.finances.domain.entities.Return;
import com.parsun.parsun_api.finances.domain.entities.ReturnLine;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnId;
import com.parsun.parsun_api.finances.domain.valueobjects.ReturnType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRepository {
    Return save(Return returnEntity);
    ReturnLine saveLine(ReturnLine line);
    Optional<Return> findById(ReturnId id);
    List<Return> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo, UUID customerId, ReturnType returnType);
    Optional<Return> findByInvoiceId(UUID invoiceId);
    int getNextSequenceForYear(int year);
}
