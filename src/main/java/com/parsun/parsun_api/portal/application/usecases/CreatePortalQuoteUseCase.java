package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.portal.application.dto.request.CreatePortalQuoteRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.quotes.application.dto.request.CreateQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.application.usecases.CreateQuoteUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreatePortalQuoteUseCase {

    private final CreateQuoteUseCase createQuoteUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public CreatePortalQuoteUseCase(CreateQuoteUseCase createQuoteUseCase,
                                     PortalAuthorizationService portalAuthorizationService) {
        this.createQuoteUseCase = createQuoteUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional
    public QuoteDetailResponse execute(CreatePortalQuoteRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();

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

        return createQuoteUseCase.executeCustomerDraft(internalRequest);
    }
}
