package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.application.mappers.PaymentMethodMapper;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListPaymentMethodsUseCase {

    private final PaymentMethodRepository repository;

    public ListPaymentMethodsUseCase(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    public List<PaymentMethodResponse> execute(Boolean active) {
        List<PaymentMethod> paymentMethods = repository.findAll(active);
        return PaymentMethodMapper.toResponseList(paymentMethods);
    }
}
