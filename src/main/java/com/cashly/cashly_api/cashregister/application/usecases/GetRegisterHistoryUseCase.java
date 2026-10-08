package com.cashly.cashly_api.cashregister.application.usecases;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterMapper;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cashregister.application.contracts.CashRegisterQueryGateway;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;


import java.time.LocalDate;
import java.util.List;

public class GetRegisterHistoryUseCase {

    private final CashRegisterRepository closureRepository;
    private final CashRegisterQueryGateway queries;

    public GetRegisterHistoryUseCase(CashRegisterRepository closureRepository, CashRegisterQueryGateway queries) {
        this.closureRepository = closureRepository;
        this.queries = queries;
    }

    public PageResponse<CashRegisterResponse> execute(int page, int size, String sort,
                                               LocalDate dateFrom, LocalDate dateTo) {
        return execute(page, size, sort, dateFrom, dateTo, false);
    }

    public PageResponse<CashRegisterResponse> execute(int page, int size, String sort,
                                               LocalDate dateFrom, LocalDate dateTo,
                                               boolean detailed) {
        var closurePage = queries.findAll(page, size, sort, dateFrom, dateTo);
        List<CashRegisterResponse> responses = closurePage.content().stream()
                .map(register -> sessionResponse(register, detailed))
                .toList();
        return new PageResponse<>(responses, closurePage.totalElements(), closurePage.totalPages(),
                closurePage.size(), closurePage.number(), closurePage.first(), closurePage.last(), closurePage.empty());
    }

    private CashRegisterResponse sessionResponse(
            com.cashly.cashly_api.cashregister.domain.entities.CashRegister register,
            boolean detailed) {
        var movements = closureRepository.findMovements(register.getId());
        if (movements.isEmpty()) {
            return CashRegisterMapper.toResponse(register);
        }
        var totals = queries.findSessionTotals(register.getId());
        register.applyMovementTotals(totals.totalRevenue(), totals.totalExpenses());
        return CashRegisterMapper.toResponse(register, totals, detailed ? movements : List.of());
    }
}
