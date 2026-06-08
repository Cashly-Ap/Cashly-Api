package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.invoices.application.dto.request.ConfirmInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.shared.services.CreditGuard;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.ReferenceType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ConfirmInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CreditGuard creditGuard;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;

    public ConfirmInvoiceUseCase(InvoiceRepository invoiceRepository,
                                  WarehouseStockRepository warehouseStockRepository,
                                  StockMovementRepository stockMovementRepository,
                                  CreditGuard creditGuard,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                                  PaymentRepository paymentRepository,
                                  PaymentAllocationRepository allocationRepository) {
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.creditGuard = creditGuard;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.consumeCustomerBalanceUseCase = consumeCustomerBalanceUseCase;
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, ConfirmInvoiceRequest request, UUID authenticatedUserId) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        String userName = user.getUserName().getValue();

        if (invoice.getPaymentType() == PaymentType.CREDIT) {
            boolean exceedsLimit = !creditGuard.isCreditAvailable(invoice.getCustomerId(), invoice.calculateTotal());
            if (exceedsLimit) {
                if (request.confirmationPassword() == null || request.confirmationPassword().isBlank()) {
                    throw new IllegalArgumentException("Se requiere contraseña para confirmar facturas que exceden el límite de crédito");
                }
                if (!passwordEncoder.matches(request.confirmationPassword(), user.getPassword().getHashedValue())) {
                    throw new IllegalArgumentException("Contraseña incorrecta");
                }
            }
        }

        if (invoice.getPaymentType() == PaymentType.CASH && request.useBalance() != null && request.useBalance()) {
            BigDecimal balanceToUse = request.balanceAmount() != null ? request.balanceAmount() : invoice.calculateTotal();
            if (balanceToUse.compareTo(BigDecimal.ZERO) > 0) {
                consumeCustomerBalanceUseCase.execute(
                    invoice.getCustomerId(),
                    BalanceType.INVOICES,
                    balanceToUse,
                    BalanceType.INVOICES,
                    invoice.getId().value(),
                    invoice.getInvoiceNumber().value(),
                    null,
                    authenticatedUserId
                );
            }
            BigDecimal remaining = invoice.calculateTotal().subtract(balanceToUse);
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                if (request.paymentMethodId() == null) {
                    throw new IllegalArgumentException("Debe seleccionar un método de pago para el monto restante");
                }
                int year = LocalDate.now().getYear();
                int seq = paymentRepository.getNextSequenceForYear(year);
                Payment payment = Payment.create(
                    PaymentNumber.of(year, seq),
                    invoice.getCustomerId(),
                    remaining,
                    request.paymentMethodId(),
                    null,
                    LocalDate.now(),
                    "Pago de factura " + invoice.getInvoiceNumber().value(),
                    authenticatedUserId,
                    userName,
                    PaymentStatus.APPLIED
                );
                Payment savedPayment = paymentRepository.save(payment);
                PaymentAllocation allocation = PaymentAllocation.create(
                    savedPayment.getId().value(), invoice.getId().value(), remaining
                );
                allocationRepository.save(allocation);
                payment.apply(remaining);
                paymentRepository.save(payment);
            }
        }

        invoice.confirm();
        deductStockAllocations(invoice);

        Invoice saved = invoiceRepository.save(invoice);
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
