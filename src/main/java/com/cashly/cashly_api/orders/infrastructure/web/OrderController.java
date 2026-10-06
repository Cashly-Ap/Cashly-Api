package com.cashly.cashly_api.orders.infrastructure.web;

import com.cashly.cashly_api.orders.application.dto.request.AssignOrderWarehousesRequest;
import com.cashly.cashly_api.orders.application.dto.request.CancelOrderRequest;
import com.cashly.cashly_api.orders.application.dto.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.orders.application.dto.request.CreateOrderRequest;
import com.cashly.cashly_api.orders.application.dto.request.UpdateOrderRequest;
import com.cashly.cashly_api.orders.application.dto.response.ConvertToInvoiceResponse;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.application.usecases.*;
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
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ConvertToInvoiceUseCase convertToInvoiceUseCase;
    private final DeleteOrderUseCase deleteOrderUseCase;
    private final AssignOrderWarehousesUseCase assignOrderWarehousesUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                            GetOrderByIdUseCase getOrderByIdUseCase,
                            GetOrdersUseCase getOrdersUseCase,
                            UpdateOrderUseCase updateOrderUseCase,
                            ConfirmOrderUseCase confirmOrderUseCase,
                            CancelOrderUseCase cancelOrderUseCase,
                            ConvertToInvoiceUseCase convertToInvoiceUseCase,
                            DeleteOrderUseCase deleteOrderUseCase,
                            AssignOrderWarehousesUseCase assignOrderWarehousesUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
        this.updateOrderUseCase = updateOrderUseCase;
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.convertToInvoiceUseCase = convertToInvoiceUseCase;
        this.deleteOrderUseCase = deleteOrderUseCase;
        this.assignOrderWarehousesUseCase = assignOrderWarehousesUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<OrderDetailResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        OrderDetailResponse response = createOrderUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<List<OrderListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir) {
        List<OrderListResponse> orders = getOrdersUseCase.execute(fromDate, toDate, customerId, status, productId, categoryId, orderBy, orderDir);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<OrderDetailResponse> getById(@PathVariable UUID id) {
        OrderDetailResponse order = getOrderByIdUseCase.execute(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<OrderDetailResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateOrderRequest request) {
        OrderDetailResponse order = updateOrderUseCase.execute(id, request);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<OrderDetailResponse> confirm(@PathVariable UUID id) {
        OrderDetailResponse order = confirmOrderUseCase.execute(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/allocations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<OrderDetailResponse> assignWarehouses(
            @PathVariable UUID id,
            @Valid @RequestBody AssignOrderWarehousesRequest request) {
        OrderDetailResponse order = assignOrderWarehousesUseCase.execute(id, request);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<OrderDetailResponse> cancel(@PathVariable UUID id,
                                                       @Valid @RequestBody CancelOrderRequest request) {
        OrderDetailResponse order = cancelOrderUseCase.execute(id, request);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/convert-to-invoice")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<ConvertToInvoiceResponse> convertToInvoice(@PathVariable UUID id,
                                                                      @Valid @RequestBody ConvertToInvoiceRequest request,
                                                                      Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        ConvertToInvoiceResponse response = convertToInvoiceUseCase.execute(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteOrderUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
