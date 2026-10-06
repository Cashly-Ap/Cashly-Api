package com.cashly.cashly_api.returns.infrastructure.web;

import com.cashly.cashly_api.returns.application.dto.request.CreateReturnRequest;
import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.returns.application.usecases.CreateReturnUseCase;
import com.cashly.cashly_api.returns.application.usecases.DeleteReturnUseCase;
import com.cashly.cashly_api.returns.application.usecases.GetAllReturnsUseCase;
import com.cashly.cashly_api.returns.application.usecases.GetReturnByIdUseCase;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<ReturnResponse> create(
            @Valid @RequestBody CreateReturnRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        ReturnResponse response = createReturnUseCase.execute(request, userId, "User");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<List<ReturnListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID invoiceId) {
        List<ReturnListResponse> returns = getAllReturnsUseCase.execute(dateFrom, dateTo, customerId, invoiceId);
        return ResponseEntity.ok(returns);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<ReturnResponse> getById(@PathVariable UUID id) {
        ReturnResponse response = getReturnByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        deleteReturnUseCase.execute(id, userId, "User");
        return ResponseEntity.noContent().build();
    }
}
