package com.parsun.parsun_api.receivables.domain.repositories;

import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.receivables.domain.entities.Receivable;
import com.parsun.parsun_api.receivables.domain.valueobjects.ReceivableId;
import com.parsun.parsun_api.receivables.domain.valueobjects.ReceivableStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivableRepository {
    Receivable save(Receivable receivable);
    Optional<Receivable> findById(ReceivableId id);
    Optional<Receivable> findByInvoiceId(InvoiceId invoiceId);
    List<Receivable> findAllWithFilters(UUID customerId, List<ReceivableStatus> statuses,
                                         LocalDate dueDateFrom, LocalDate dueDateTo,
                                         String orderBy, String orderDir);
    List<Receivable> findByDueDateBeforeAndStatusIn(LocalDate date, List<ReceivableStatus> statuses);
}
