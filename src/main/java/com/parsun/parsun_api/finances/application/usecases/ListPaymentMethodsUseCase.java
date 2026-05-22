package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;

import java.util.List;

public class ListPaymentMethodsUseCase {

    private final PaymentMethodRepository repository;

    public ListPaymentMethodsUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    public List<PaymentMethodResponse> execute(Boolean active) {
        List<PaymentMethod> paymentMethods = repository.findAll(active);
        return FinanceMapper.toPaymentMethodResponseList(paymentMethods);
    }
}
