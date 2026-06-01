package com.parsun.parsun_api.receivables.domain.repositories;

import com.parsun.parsun_api.receivables.application.dto.response.CustomerDebtListResponse;
import com.parsun.parsun_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.parsun.parsun_api.receivables.application.dto.response.CustomerInvoiceDebtResponse;
import com.parsun.parsun_api.receivables.application.dto.response.PendingPaymentResponse;
import com.parsun.parsun_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ReceivablesRepository {

    ReceivablesSummaryResponse findSummary();

    Page<CustomerDebtListResponse> findCustomersWithDebt(String search, int page, int size);

    CustomerDebtSummaryResponse findCustomerDebt(UUID customerId);

    Page<CustomerInvoiceDebtResponse> findCustomerInvoices(UUID customerId, String search, String status, int page, int size);

    Page<PendingPaymentResponse> findPaymentHistory(String search, int page, int size);

    Page<PendingPaymentResponse> findCustomerPaymentHistory(UUID customerId, UUID invoiceId, int page, int size);
}
