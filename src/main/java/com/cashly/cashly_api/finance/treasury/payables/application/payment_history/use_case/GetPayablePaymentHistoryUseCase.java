package com.cashly.cashly_api.finance.treasury.payables.application.payment_history.use_case;

import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablePaymentHistoryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPayablePaymentHistoryUseCase {

    private final PayablesQueryGateway gateway;

    public GetPayablePaymentHistoryUseCase(PayablesQueryGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional(readOnly = true)
    public PageResponse<PayablePaymentHistoryResponse> execute(String search,
            int page, int size) {
        return gateway.findPaymentHistory(search, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<PayablePaymentHistoryResponse> execute(UUID supplierId,
            UUID purchaseId, int page, int size) {
        return gateway.findSupplierPaymentHistory(supplierId, purchaseId, page,
                size);
    }
}
