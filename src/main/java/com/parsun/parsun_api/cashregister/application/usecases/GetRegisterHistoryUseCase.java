package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterMapper;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;

public class GetRegisterHistoryUseCase {

    private final CashRegisterRepository closureRepository;

    public GetRegisterHistoryUseCase(CashRegisterRepository closureRepository) {
        this.closureRepository = closureRepository;
    }

    public Page<CashRegisterResponse> execute(int page, int size, String sort,
                                               LocalDate dateFrom, LocalDate dateTo) {
        var closurePage = closureRepository.findAll(page, size, sort, dateFrom, dateTo);
        List<CashRegisterResponse> responses = closurePage.getContent().stream()
                .map(CashRegisterMapper::toResponse)
                .toList();
        return new PageImpl<>(responses, closurePage.getPageable(), closurePage.getTotalElements());
    }
}
