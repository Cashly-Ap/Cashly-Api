package com.cashly.cashly_api.finance.treasury.payment_methods.application.queries.use_case;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.mappers.PaymentMethodMapper;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.out.PaymentMethodUsageGateway;
import com.cashly.cashly_api.finance.treasury.payment_methods.domain.repository.PaymentMethodRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListPaymentMethodsUseCase {

    private final PaymentMethodRepository repository;
    private final PaymentMethodUsageGateway usageGateway;

    public ListPaymentMethodsUseCase(PaymentMethodRepository repository, PaymentMethodUsageGateway usageGateway) {
        this.repository = repository;
        this.usageGateway = usageGateway;
    }

    public PageResponse<PaymentMethodResponse> execute(String search, Boolean active,
                                                        int page, int size,
                                                        String orderBy, String orderDir) {
        PageResponse<PaymentMethod> pageResult = repository.findAllWithFilters(
                search, active, page, size, orderBy, orderDir);

        List<PaymentMethodResponse> content = pageResult.content().stream()
                .map(pm -> PaymentMethodMapper.toResponse(pm, usageGateway.hasAssociatedRecords(pm.getId().value())))
                .toList();

        return new PageResponse<>(content, pageResult.totalElements(), pageResult.totalPages(),
                pageResult.size(), pageResult.number(), pageResult.first(),
                pageResult.last(), pageResult.empty());
    }
}
