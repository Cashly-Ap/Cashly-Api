package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.close.request.CloseRegisterRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.open.request.OpenRegisterRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CancelCashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.close.use_case.CloseRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.queries.use_case.GetActiveRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.queries.use_case.GetRegisterHistoryUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.open.use_case.OpenRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case.RecordCashMovementUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case.DeleteCashMovementUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case.CancelCashMovementUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/cash-register")
public class CashRegisterController {

    private final OpenRegisterUseCase openRegisterUseCase;
    private final GetActiveRegisterUseCase getActiveRegisterUseCase;
    private final CloseRegisterUseCase closeRegisterUseCase;
    private final GetRegisterHistoryUseCase getRegisterHistoryUseCase;
    private final RecordCashMovementUseCase recordCashMovementUseCase;
    private final DeleteCashMovementUseCase deleteCashMovementUseCase;
    private final CancelCashMovementUseCase cancelCashMovementUseCase;

    public CashRegisterController(OpenRegisterUseCase openRegisterUseCase,
            GetActiveRegisterUseCase getActiveRegisterUseCase,
            CloseRegisterUseCase closeRegisterUseCase,
            GetRegisterHistoryUseCase getRegisterHistoryUseCase,
            RecordCashMovementUseCase recordCashMovementUseCase,
            DeleteCashMovementUseCase deleteCashMovementUseCase,
            CancelCashMovementUseCase cancelCashMovementUseCase) {
        this.openRegisterUseCase = openRegisterUseCase;
        this.getActiveRegisterUseCase = getActiveRegisterUseCase;
        this.closeRegisterUseCase = closeRegisterUseCase;
        this.getRegisterHistoryUseCase = getRegisterHistoryUseCase;
        this.recordCashMovementUseCase = recordCashMovementUseCase;
        this.deleteCashMovementUseCase = deleteCashMovementUseCase;
        this.cancelCashMovementUseCase = cancelCashMovementUseCase;
    }

    @PostMapping("/open")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_CASH_REGISTER_OPEN')")
    public ResponseEntity<CashRegisterResponse> open(
            @Valid @RequestBody OpenRegisterRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CashRegisterResponse response = openRegisterUseCase.execute(userId,
                request.openedByName(), request.openingBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_CASH_REGISTER_VIEW_LIST')")
    public ResponseEntity<CashRegisterResponse> getActive() {
        return ResponseEntity.of(getActiveRegisterUseCase.execute());
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_CASH_REGISTER_CLOSE')")
    public ResponseEntity<CashRegisterResponse> close(@PathVariable UUID id,
            @Valid @RequestBody CloseRegisterRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CashRegisterResponse response = closeRegisterUseCase.execute(id, userId,
                request.closedByName(), request.declaredBalance(),
                request.closingNotes());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/movements")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_CASH_REGISTER_OPEN')")
    public ResponseEntity<CashMovementResponse> addMovement(
            @PathVariable UUID id,
            @Valid @RequestBody CashMovementRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recordCashMovementUseCase.execute(id, request, userId));
    }

    @DeleteMapping("/movements/{movementId}")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_CASH_REGISTER_OPEN')")
    public ResponseEntity<Void> deleteMovement(@PathVariable UUID movementId) {
        deleteCashMovementUseCase.execute(movementId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/movements/{movementId}/cancel")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_CASH_REGISTER_OPEN')")
    public ResponseEntity<CashMovementResponse> cancelMovement(
            @PathVariable UUID movementId,
            @Valid @RequestBody CancelCashMovementRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(
                cancelCashMovementUseCase.execute(movementId, request, userId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_CASH_REGISTER_CASH_HISTORY')")
    public ResponseEntity<Page<CashRegisterResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "closureDate,desc") String sort,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) UUID registerId,
            @RequestParam(defaultValue = "false") boolean detailed) {
        var history = getRegisterHistoryUseCase.execute(page, size, sort,
                dateFrom, dateTo, registerId, detailed);
        String[] sortParts = sort == null || sort.isBlank()
                ? new String[0]
                : sort.split(",");
        Sort historySort = sortParts.length == 0
                ? Sort.unsorted()
                : sortParts.length < 2
                        ? Sort.by(sortParts[0].trim())
                        : Sort.by(
                                Sort.Direction.fromString(sortParts[1].trim()),
                                sortParts[0].trim());
        return ResponseEntity.ok(new PageImpl<>(history.content(),
                PageRequest.of(page, size, historySort),
                history.totalElements()));
    }
}
