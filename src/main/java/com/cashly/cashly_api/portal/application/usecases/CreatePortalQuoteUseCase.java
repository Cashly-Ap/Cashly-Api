package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.portal.application.dto.request.CreatePortalQuoteRequest;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import com.cashly.cashly_api.quotes.application.dto.request.CreateQuoteRequest;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.usecases.CreateQuoteUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreatePortalQuoteUseCase {

    private final CreateQuoteUseCase createQuoteUseCase;
    private final PortalAuthorizationService portalAuthorizationService;
    private final NotificationService notificationService;

    public CreatePortalQuoteUseCase(CreateQuoteUseCase createQuoteUseCase,
                                     PortalAuthorizationService portalAuthorizationService,
                                     NotificationService notificationService) {
        this.createQuoteUseCase = createQuoteUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public QuoteDetailResponse execute(CreatePortalQuoteRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        request.lines().forEach(line -> portalAuthorizationService.requireProductAccess(line.productId()));

        CreateQuoteRequest internalRequest = new CreateQuoteRequest(
                customerId,
                request.expiryDate(),
                request.notes(),
                request.lines().stream()
                        .map(line -> new CreateQuoteRequest.QuoteLineRequest(
                                line.productId(),
                                line.quantity(),
                                line.unitPrice(),
                                List.of()
                        ))
                        .collect(Collectors.toList())
        );

        QuoteDetailResponse response = createQuoteUseCase.executeCustomerDraft(internalRequest);
        notificationService.notifyAdminsQuoteCreated(response.id(), response.customerName());
        return response;
    }
}
