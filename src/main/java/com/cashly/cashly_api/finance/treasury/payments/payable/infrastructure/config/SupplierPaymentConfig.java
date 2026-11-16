package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.config;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case.CreateSupplierAdvanceUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case.GetSupplierBalanceUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case.RefundSupplierBalanceUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.approve.use_case.ApproveSupplierPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.create.use_case.RegisterSupplierPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.delete.use_case.DeleteSupplierPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.list.use_case.ListSupplierPaymentsUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.pay_purchase.use_case.PayPurchaseUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.pay_supplier_debt.use_case.PaySupplierDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.PayablePurchaseGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierBalanceCashGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierBalanceGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.SupplierPaymentCashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.reject.use_case.RejectSupplierPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.services.SupplierPaymentSettlementService;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.payable.domain.repository.SupplierPaymentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupplierPaymentConfig {

    @Bean
    public SupplierPaymentSettlementService supplierPaymentSettlementService(
            PayablePurchaseGateway purchaseGateway,
            SupplierPaymentAllocationRepository allocationRepository,
            SupplierPaymentRepository payments,
            SupplierPaymentCashRegisterGateway cashRegisterGateway,
            SupplierBalanceGateway balanceGateway) {
        return new SupplierPaymentSettlementService(purchaseGateway,
                allocationRepository, payments, cashRegisterGateway,
                balanceGateway);
    }

    @Bean
    public RegisterSupplierPaymentUseCase registerSupplierPaymentUseCase(
            SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        return new RegisterSupplierPaymentUseCase(payments, settlement);
    }

    @Bean
    public PayPurchaseUseCase payPurchaseUseCase(
            PayablePurchaseGateway purchaseGateway,
            SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        return new PayPurchaseUseCase(purchaseGateway, payments, settlement);
    }

    @Bean
    public PaySupplierDebtUseCase paySupplierDebtUseCase(
            SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        return new PaySupplierDebtUseCase(payments, settlement);
    }

    @Bean
    public ApproveSupplierPaymentUseCase approveSupplierPaymentUseCase(
            SupplierPaymentRepository payments,
            SupplierPaymentSettlementService settlement) {
        return new ApproveSupplierPaymentUseCase(payments, settlement);
    }

    @Bean
    public RejectSupplierPaymentUseCase rejectSupplierPaymentUseCase(
            SupplierPaymentRepository payments) {
        return new RejectSupplierPaymentUseCase(payments);
    }

    @Bean
    public DeleteSupplierPaymentUseCase deleteSupplierPaymentUseCase(
            SupplierPaymentRepository payments) {
        return new DeleteSupplierPaymentUseCase(payments);
    }

    @Bean
    public ListSupplierPaymentsUseCase listSupplierPaymentsUseCase(
            SupplierPaymentRepository payments) {
        return new ListSupplierPaymentsUseCase(payments);
    }

    @Bean
    public GetSupplierBalanceUseCase getSupplierBalanceUseCase(
            SupplierBalanceGateway balances) {
        return new GetSupplierBalanceUseCase(balances);
    }

    @Bean
    public CreateSupplierAdvanceUseCase createSupplierAdvanceUseCase(
            SupplierBalanceGateway balances,
            SupplierBalanceCashGateway cashGateway,
            ActivityNotificationRecorder activityNotifications) {
        return new CreateSupplierAdvanceUseCase(balances, cashGateway,
                activityNotifications);
    }

    @Bean
    public RefundSupplierBalanceUseCase refundSupplierBalanceUseCase(
            SupplierBalanceGateway balances,
            SupplierBalanceCashGateway cashGateway,
            ActivityNotificationRecorder activityNotifications) {
        return new RefundSupplierBalanceUseCase(balances, cashGateway,
                activityNotifications);
    }
}
