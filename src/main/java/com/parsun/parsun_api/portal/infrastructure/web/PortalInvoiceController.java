package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.portal.application.usecases.GetPortalInvoiceUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalInvoicesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal/invoices")
public class PortalInvoiceController {

    private final ListPortalInvoicesUseCase listPortalInvoicesUseCase;
    private final GetPortalInvoiceUseCase getPortalInvoiceUseCase;

    public PortalInvoiceController(ListPortalInvoicesUseCase listPortalInvoicesUseCase,
                                    GetPortalInvoiceUseCase getPortalInvoiceUseCase) {
        this.listPortalInvoicesUseCase = listPortalInvoicesUseCase;
        this.getPortalInvoiceUseCase = getPortalInvoiceUseCase;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceListResponse>> listInvoices(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> returnStatus,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDir) {
        List<InvoiceListResponse> response = listPortalInvoicesUseCase.execute(
                dateFrom, dateTo, statuses, returnStatus, productId, categoryId, orderBy, orderDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailResponse> getInvoice(@PathVariable UUID id) {
        InvoiceDetailResponse response = getPortalInvoiceUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}
