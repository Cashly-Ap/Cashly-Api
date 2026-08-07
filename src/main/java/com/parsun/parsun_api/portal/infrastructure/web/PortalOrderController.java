package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;
import com.parsun.parsun_api.portal.application.dto.request.CancelPortalOrderRequest;
import com.parsun.parsun_api.portal.application.dto.request.CreatePortalOrderRequest;
import com.parsun.parsun_api.portal.application.dto.request.UpdatePortalOrderRequest;
import com.parsun.parsun_api.portal.application.usecases.CancelPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.ConfirmPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.CreatePortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalOrdersUseCase;
import com.parsun.parsun_api.portal.application.usecases.UpdatePortalOrderUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal/orders")
public class PortalOrderController {

    private final ListPortalOrdersUseCase listPortalOrdersUseCase;
    private final GetPortalOrderUseCase getPortalOrderUseCase;
    private final CreatePortalOrderUseCase createPortalOrderUseCase;
    private final UpdatePortalOrderUseCase updatePortalOrderUseCase;
    private final ConfirmPortalOrderUseCase confirmPortalOrderUseCase;
    private final CancelPortalOrderUseCase cancelPortalOrderUseCase;

    public PortalOrderController(ListPortalOrdersUseCase listPortalOrdersUseCase,
                                  GetPortalOrderUseCase getPortalOrderUseCase,
                                  CreatePortalOrderUseCase createPortalOrderUseCase,
                                  UpdatePortalOrderUseCase updatePortalOrderUseCase,
                                  ConfirmPortalOrderUseCase confirmPortalOrderUseCase,
                                  CancelPortalOrderUseCase cancelPortalOrderUseCase) {
        this.listPortalOrdersUseCase = listPortalOrdersUseCase;
        this.getPortalOrderUseCase = getPortalOrderUseCase;
        this.createPortalOrderUseCase = createPortalOrderUseCase;
        this.updatePortalOrderUseCase = updatePortalOrderUseCase;
        this.confirmPortalOrderUseCase = confirmPortalOrderUseCase;
        this.cancelPortalOrderUseCase = cancelPortalOrderUseCase;
    }

    @GetMapping
    public ResponseEntity<List<OrderListResponse>> listOrders(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDir) {
        List<OrderListResponse> response = listPortalOrdersUseCase.execute(
                dateFrom, dateTo, status, productId, categoryId, orderBy, orderDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable UUID id) {
        OrderDetailResponse response = getPortalOrderUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderDetailResponse> createOrder(
            @Valid @RequestBody CreatePortalOrderRequest request) {
        OrderDetailResponse response = createPortalOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePortalOrderRequest request) {
        OrderDetailResponse response = updatePortalOrderUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderDetailResponse> confirmOrder(@PathVariable UUID id) {
        OrderDetailResponse response = confirmPortalOrderUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDetailResponse> cancelOrder(@PathVariable UUID id,
                                                            @Valid @RequestBody CancelPortalOrderRequest request) {
        OrderDetailResponse response = cancelPortalOrderUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }
}
