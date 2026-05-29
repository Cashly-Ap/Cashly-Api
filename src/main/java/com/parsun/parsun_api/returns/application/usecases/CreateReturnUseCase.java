package com.parsun.parsun_api.returns.application.usecases;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.returns.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.returns.application.dto.response.ReturnResponse;
import com.parsun.parsun_api.returns.application.mappers.ReturnMapper;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnNumber;
import com.parsun.parsun_api.returns.domain.entities.ReturnType;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateReturnUseCase {

    private final ReturnRepository returnRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReturnMapper returnMapper;
    private final PaymentAllocationService paymentAllocationService;
    private final PaymentAllocationRepository allocationRepository;

    public CreateReturnUseCase(ReturnRepository returnRepository,
                                InvoiceRepository invoiceRepository,
                                ReturnMapper returnMapper,
                                PaymentAllocationService paymentAllocationService,
                                PaymentAllocationRepository allocationRepository) {
        this.returnRepository = returnRepository;
        this.invoiceRepository = invoiceRepository;
        this.returnMapper = returnMapper;
        this.paymentAllocationService = paymentAllocationService;
        this.allocationRepository = allocationRepository;
    }

    @Transactional
    public ReturnResponse execute(CreateReturnRequest request, UUID authenticatedUserId, String userName) {
        int year = LocalDate.now().getYear();
        int sequence = returnRepository.getNextSequenceForYear(year);
        ReturnNumber returnNumber = ReturnNumber.of(year, sequence);

        List<ReturnLine> lines = request.lines().stream()
                .map(ReturnMapper::toLine)
                .toList();

        BigDecimal total = lines.stream()
                .map(ReturnLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Return return_ = Return.create(
                returnNumber,
                InvoiceId.of(request.invoiceId()),
                request.customerId(),
                ReturnType.valueOf(request.returnType()),
                request.reason(),
                total,
                lines,
                authenticatedUserId,
                userName
        );

        Return saved = returnRepository.save(return_);

        Invoice invoice = invoiceRepository.findById(return_.getInvoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException(return_.getInvoiceId().value()));

        invoice.applyReturn(saved);

        invoiceRepository.save(invoice);

        if (return_.getReturnType() == ReturnType.AUTOMATIC) {
            cancelInvoicePayments(return_.getInvoiceId().value());
        }

        return returnMapper.toResponse(saved);
    }

    private void cancelInvoicePayments(UUID invoiceId) {
        allocationRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentAllocation::getPaymentId)
                .distinct()
                .forEach(paymentAllocationService::cancelPayment);
    }
}
