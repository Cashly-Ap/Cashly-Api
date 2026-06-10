package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterMapper;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;

import java.time.LocalDate;
import java.util.UUID;

public class GetActiveRegisterUseCase {

    private final CashRegisterRepository closureRepository;
    private final UserRepository userRepository;

    public GetActiveRegisterUseCase(CashRegisterRepository closureRepository,
                                    UserRepository userRepository) {
        this.closureRepository = closureRepository;
        this.userRepository = userRepository;
    }

    public CashRegisterResponse execute(UUID userId) {
        CashRegister closure = closureRepository.findActiveClosure()
                .orElseGet(() -> autoOpenRegister(userId));

        DailyTotals dailyTotals = closureRepository.findDailyTotals(closure.getClosureDate());

        closure.applyTotals(
                dailyTotals.totalPayments(), dailyTotals.paymentCount(),
                dailyTotals.totalAdvances(), dailyTotals.advanceCount(),
                dailyTotals.totalReturns(), dailyTotals.returnCount(),
                dailyTotals.totalCreditNotes(),
                dailyTotals.totalDebitNotes(),
                dailyTotals.totalInvoices(), dailyTotals.invoiceCount(),
                dailyTotals.paymentDetails()
        );

        CashRegister saved = closureRepository.save(closure);
        return CashRegisterMapper.toResponse(saved, dailyTotals);
    }

    private CashRegister autoOpenRegister(UUID userId) {
        LocalDate today = LocalDate.now();

        if (closureRepository.findByClosureDate(today).isPresent()) {
            throw new IllegalStateException("A closure already exists for today: " + today);
        }

        var user = userRepository.findById(new UserId(userId));
        String userName = user.map(u -> u.getUserName().getValue())
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        CashRegister closure = CashRegister.open(RegisterId.generate(), today, userId, userName);
        return closureRepository.save(closure);
    }
}
