package com.parsun.parsun_api.finances.infrastructure.web;

import com.parsun.parsun_api.finances.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.finances.application.dto.response.ReturnDetailResponse;
import com.parsun.parsun_api.finances.application.dto.response.ReturnListResponse;
import com.parsun.parsun_api.finances.application.usecases.CancelReturnUseCase;
import com.parsun.parsun_api.finances.application.usecases.CreateReturnUseCase;
import com.parsun.parsun_api.finances.application.usecases.GetReturnByIdUseCase;
import com.parsun.parsun_api.finances.application.usecases.GetReturnsUseCase;
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
@RequestMapping("/api/finances/returns")
public class ReturnController {

    private final CreateReturnUseCase createReturnUseCase;
    private final CancelReturnUseCase cancelReturnUseCase;
    private final GetReturnsUseCase getReturnsUseCase;
    private final GetReturnByIdUseCase getReturnByIdUseCase;

    public ReturnController(CreateReturnUseCase createReturnUseCase,
                            CancelReturnUseCase cancelReturnUseCase,
                            GetReturnsUseCase getReturnsUseCase,
                            GetReturnByIdUseCase getReturnByIdUseCase) {
        this.createReturnUseCase = createReturnUseCase;
        this.cancelReturnUseCase = cancelReturnUseCase;
        this.getReturnsUseCase = getReturnsUseCase;
        this.getReturnByIdUseCase = getReturnByIdUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<ReturnDetailResponse> create(
            @Valid @RequestBody CreateReturnRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        ReturnDetailResponse response = createReturnUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<ReturnListResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String returnType) {
        List<ReturnListResponse> returns = getReturnsUseCase.execute(dateFrom, dateTo, customerId, returnType);
        return ResponseEntity.ok(returns);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<ReturnDetailResponse> getById(@PathVariable UUID id) {
        ReturnDetailResponse response = getReturnByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        cancelReturnUseCase.execute(id);
        return ResponseEntity.ok().build();
    }
}
