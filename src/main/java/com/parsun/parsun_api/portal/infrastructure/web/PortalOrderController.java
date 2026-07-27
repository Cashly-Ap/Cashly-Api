package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;
import com.parsun.parsun_api.portal.application.dto.request.CancelPortalOrderRequest;
import com.parsun.parsun_api.portal.application.usecases.CancelPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.ConfirmPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalOrdersUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ConfirmPortalOrderUseCase confirmPortalOrderUseCase;
    private final CancelPortalOrderUseCase cancelPortalOrderUseCase;

    public PortalOrderController(ListPortalOrdersUseCase listPortalOrdersUseCase,
                                  GetPortalOrderUseCase getPortalOrderUseCase,
                                  ConfirmPortalOrderUseCase confirmPortalOrderUseCase,
                                  CancelPortalOrderUseCase cancelPortalOrderUseCase) {
        this.listPortalOrdersUseCase = listPortalOrdersUseCase;
        this.getPortalOrderUseCase = getPortalOrderUseCase;
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
