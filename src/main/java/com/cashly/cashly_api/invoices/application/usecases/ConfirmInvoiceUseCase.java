package com.cashly.cashly_api.invoices.application.usecases;

import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.invoices.application.dto.request.ConfirmInvoiceRequest;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLineAllocation;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.ReferenceType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConfirmInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final InvoiceAuthorizationService authorizationService;
    private final NotificationService notificationService;
    private final CashMovementService cashMovementService;

    public ConfirmInvoiceUseCase(InvoiceRepository invoiceRepository,
                                  WarehouseStockRepository warehouseStockRepository,
                                  StockMovementRepository stockMovementRepository,
                                  InvoiceAuthorizationService authorizationService,
                                  UserRepository userRepository,
                                  NotificationService notificationService,
                                  CashMovementService cashMovementService) {
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.cashMovementService = cashMovementService;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, ConfirmInvoiceRequest request, UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        authorizationService.authorize(
                invoice, user,
                request.confirmationPassword(),
                request.useBalance(),
                request.balanceAmount(),
                request.paymentMethodId(),
                authenticatedUserId
        );

        invoice.confirm();
        deductStockAllocations(invoice);

        Invoice saved = invoiceRepository.save(invoice);
        cashMovementService.record(
                CashMovementDirection.IN,
                CashMovementType.INVOICE,
                saved.calculateTotal(),
                null,
                "Factura confirmada " + saved.getInvoiceNumber().value(),
                "INVOICE",
                saved.getId().value(),
                authenticatedUserId,
                user.getUserName().getValue()
        );
        notificationService.notifyCustomerInvoiceConfirmed(
                saved.getCustomerId(), saved.getId().value(), saved.getInvoiceNumber().value());
        return InvoiceMapper.toDetailResponse(saved);
    }

    private void deductStockAllocations(Invoice invoice) {
        for (InvoiceLine line : invoice.getLines()) {
            for (InvoiceLineAllocation allocation : line.getAllocations()) {
                warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                        .ifPresent(stock -> {
                            int previousQuantity = stock.getQuantity();
                            int previousReserved = stock.getReserved();
                            stock.deductReserved(allocation.getAllocated().intValue());
                            warehouseStockRepository.save(stock);
                            stockMovementRepository.save(StockMovement.createWithReference(
                                    allocation.getWarehouseId(),
                                    line.getProductId(),
                                    MovementType.SALE_EXIT,
                                    allocation.getAllocated().intValue(),
                                    previousQuantity,
                                    stock.getQuantity(),
                                    null,
                                    null,
                                    ReferenceType.INVOICE,
                                    invoice.getId().value(),
                                    invoice.getInvoiceNumber().value(),
                                    "Invoice confirmed"
                            ));
                        });
            }
        }
    }
}
