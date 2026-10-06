package com.cashly.cashly_api.cashregister.application.usecases;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterMapper;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
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
        return execute(page, size, sort, dateFrom, dateTo, false);
    }

    public Page<CashRegisterResponse> execute(int page, int size, String sort,
                                               LocalDate dateFrom, LocalDate dateTo,
                                               boolean detailed) {
        var closurePage = closureRepository.findAll(page, size, sort, dateFrom, dateTo);
        List<CashRegisterResponse> responses = closurePage.getContent().stream()
                .map(register -> sessionResponse(register, detailed))
                .toList();
        return new PageImpl<>(responses, closurePage.getPageable(), closurePage.getTotalElements());
    }

    private CashRegisterResponse sessionResponse(
            com.cashly.cashly_api.cashregister.domain.entities.CashRegister register,
            boolean detailed) {
        var movements = closureRepository.findMovements(register.getId());
        if (movements.isEmpty()) {
            return CashRegisterMapper.toResponse(register);
        }
        var totals = closureRepository.findSessionTotals(register.getId());
        register.applyMovementTotals(totals.totalRevenue(), totals.totalExpenses());
        return CashRegisterMapper.toResponse(register, totals, detailed ? movements : List.of());
    }
}
