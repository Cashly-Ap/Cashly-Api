package com.cashly.cashly_api.finance.billing.returns.infrastructure.web;

import com.cashly.cashly_api.finance.billing.returns.application.create.request.CreateReturnRequest;
import com.cashly.cashly_api.finance.billing.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.finance.billing.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.finance.billing.returns.application.create.use_case.CreateReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.delete.use_case.DeleteReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.list.use_case.GetAllReturnsUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.get.use_case.GetReturnByIdUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final CreateReturnUseCase createReturnUseCase;
    private final GetAllReturnsUseCase getAllReturnsUseCase;
    private final GetReturnByIdUseCase getReturnByIdUseCase;
    private final DeleteReturnUseCase deleteReturnUseCase;

    public ReturnController(CreateReturnUseCase createReturnUseCase,
            GetAllReturnsUseCase getAllReturnsUseCase,
            GetReturnByIdUseCase getReturnByIdUseCase,
            DeleteReturnUseCase deleteReturnUseCase) {
        this.createReturnUseCase = createReturnUseCase;
        this.getAllReturnsUseCase = getAllReturnsUseCase;
        this.getReturnByIdUseCase = getReturnByIdUseCase;
        this.deleteReturnUseCase = deleteReturnUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FINANCE_RETURNS:CREATE') and hasAuthority('INVENTORY_WAREHOUSES:EXECUTE')")
    public ResponseEntity<ReturnResponse> create(
            @Valid @RequestBody CreateReturnRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        ReturnResponse response = createReturnUseCase.execute(request, userId,
                "User");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_RETURNS:READ')")
    public ResponseEntity<List<ReturnListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID invoiceId) {
        List<ReturnListResponse> returns = getAllReturnsUseCase
                .execute(dateFrom, dateTo, customerId, invoiceId);
        return ResponseEntity.ok(returns);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_RETURNS:READ')")
    public ResponseEntity<ReturnResponse> getById(@PathVariable UUID id) {
        ReturnResponse response = getReturnByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_RETURNS:DELETE') and hasAuthority('INVENTORY_WAREHOUSES:EXECUTE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        deleteReturnUseCase.execute(id, userId, "User");
        return ResponseEntity.noContent().build();
    }
}
