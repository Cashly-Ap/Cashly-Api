package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoicesUseCase;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.portal.application.dto.response.PortalInvoiceListResponse;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ListPortalInvoicesUseCase {

    private final GetInvoicesUseCase getInvoicesUseCase;
    private final PortalAuthorizationService portalAuthorizationService;
    private final PaymentRepository paymentRepository;

    public ListPortalInvoicesUseCase(GetInvoicesUseCase getInvoicesUseCase,
                                      PortalAuthorizationService portalAuthorizationService,
                                      PaymentRepository paymentRepository) {
        this.getInvoicesUseCase = getInvoicesUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<PortalInvoiceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                                    List<String> statuses,
                                                    List<String> returnStatus,
                                                    UUID productId, UUID categoryId,
                                                    String orderBy, String orderDir) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        List<InvoiceListResponse> invoices = getInvoicesUseCase.execute(dateFrom, dateTo, customerId, statuses,
                returnStatus, productId, categoryId, orderBy, orderDir);

        List<Payment> pendingPayments = paymentRepository.findAllWithFilters(null, null, customerId, "PENDING");
        Map<UUID, BigDecimal> pendingByInvoice = new HashMap<>();
        for (Payment p : pendingPayments) {
            String notes = p.getNotes();
            if (notes != null && notes.startsWith("INVOICE_ID:")) {
                try {
                    UUID invoiceId = UUID.fromString(notes.substring("INVOICE_ID:".length()).trim());
                    pendingByInvoice.merge(invoiceId, p.getAmount(), BigDecimal::add);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return invoices.stream()
                .map(inv -> new PortalInvoiceListResponse(
                        inv.id(), inv.invoiceNumber(), inv.invoiceNumber(), inv.createdAt(), null,
                        inv.status(), inv.total(), inv.subtotal(), 
                        inv.total().subtract(inv.paidAmount()), inv.paidAmount(),
                        inv.paymentType(), inv.createdAt(),
                        pendingByInvoice.getOrDefault(inv.id(), BigDecimal.ZERO)))
                .toList();
    }
}
