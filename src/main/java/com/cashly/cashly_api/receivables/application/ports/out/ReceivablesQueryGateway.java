package com.cashly.cashly_api.receivables.application.ports.out;

import com.cashly.cashly_api.receivables.application.ports.out.model.AccountsReceivableRow;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerDebtListResponse;
import com.cashly.cashly_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.cashly.cashly_api.receivables.application.dto.response.CustomerInvoiceDebtResponse;
import com.cashly.cashly_api.receivables.application.dto.response.PendingPaymentResponse;
import com.cashly.cashly_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.List;

import java.util.UUID;

public interface ReceivablesQueryGateway {

    ReceivablesSummaryResponse findSummary();

    PageResponse<CustomerDebtListResponse> findCustomersWithDebt(String search, int page, int size, String sort);

    CustomerDebtSummaryResponse findCustomerDebt(UUID customerId);

    PageResponse<CustomerInvoiceDebtResponse> findCustomerInvoices(UUID customerId, String search, String status, int page, int size);

    PageResponse<PendingPaymentResponse> findPaymentHistory(String search, int page, int size);

    PageResponse<PendingPaymentResponse> findCustomerPaymentHistory(UUID customerId, UUID invoiceId, int page, int size);

    List<AccountsReceivableRow> findAccountsReceivableForReport(UUID customerId, boolean overdueOnly);
}
