package com.cashly.cashly_api.sales.orders.infrastructure.web;

import com.cashly.cashly_api.sales.orders.application.cancel.request.CancelOrderRequest;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.sales.orders.application.create.request.CreateOrderRequest;
import com.cashly.cashly_api.sales.orders.application.update.request.UpdateOrderRequest;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.response.ConvertToInvoiceResponse;
import com.cashly.cashly_api.sales.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.sales.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.sales.orders.application.cancel.use_case.CancelOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.confirm.use_case.ConfirmOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.use_case.ConvertToInvoiceUseCase;
import com.cashly.cashly_api.sales.orders.application.create.use_case.CreateOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.delete.use_case.DeleteOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.get.use_case.GetOrderByIdUseCase;
import com.cashly.cashly_api.sales.orders.application.list.use_case.GetOrdersUseCase;
import com.cashly.cashly_api.sales.orders.application.update.use_case.UpdateOrderUseCase;
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

    public OrderController(CreateOrderUseCase createOrderUseCase,
                            GetOrderByIdUseCase getOrderByIdUseCase,
                            GetOrdersUseCase getOrdersUseCase,
                            UpdateOrderUseCase updateOrderUseCase,
                            ConfirmOrderUseCase confirmOrderUseCase,
                            CancelOrderUseCase cancelOrderUseCase,
                            ConvertToInvoiceUseCase convertToInvoiceUseCase,
                            DeleteOrderUseCase deleteOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
        this.updateOrderUseCase = updateOrderUseCase;
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.convertToInvoiceUseCase = convertToInvoiceUseCase;
        this.deleteOrderUseCase = deleteOrderUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES:CREATE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<OrderDetailResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        OrderDetailResponse response = createOrderUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES:READ')")
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
    @PreAuthorize("hasAuthority('SALES:READ') and hasAuthority('INVENTORY:READ')")
    public ResponseEntity<OrderDetailResponse> getById(@PathVariable UUID id) {
        OrderDetailResponse order = getOrderByIdUseCase.execute(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES:UPDATE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<OrderDetailResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateOrderRequest request) {
        OrderDetailResponse order = updateOrderUseCase.execute(id, request);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('SALES:EXECUTE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<OrderDetailResponse> confirm(@PathVariable UUID id) {
        OrderDetailResponse order = confirmOrderUseCase.execute(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SALES:EXECUTE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<OrderDetailResponse> cancel(@PathVariable UUID id,
                                                       @Valid @RequestBody CancelOrderRequest request) {
        OrderDetailResponse order = cancelOrderUseCase.execute(id, request);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/convert-to-invoice")
    @PreAuthorize("hasAuthority('SALES:EXECUTE') and hasAuthority('FINANCES:CREATE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<ConvertToInvoiceResponse> convertToInvoice(@PathVariable UUID id,
                                                                      @Valid @RequestBody ConvertToInvoiceRequest request,
                                                                      Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        ConvertToInvoiceResponse response = convertToInvoiceUseCase.execute(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES:DELETE') and hasAuthority('INVENTORY:EXECUTE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteOrderUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
