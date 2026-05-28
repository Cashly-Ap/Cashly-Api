package com.parsun.parsun_api.returns.application.usecases;

import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.returns.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.returns.application.dto.response.ReturnResponse;
import com.parsun.parsun_api.returns.application.mappers.ReturnMapper;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnNumber;
import com.parsun.parsun_api.returns.domain.entities.ReturnType;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.returns.domain.services.CreditNoteCreator;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateReturnUseCase {

    private final ReturnRepository returnRepository;
    private final CreditNoteCreator creditNoteCreator;
    private final ReturnMapper returnMapper;

    public CreateReturnUseCase(ReturnRepository returnRepository,
                                CreditNoteCreator creditNoteCreator,
                                ReturnMapper returnMapper) {
        this.returnRepository = returnRepository;
        this.creditNoteCreator = creditNoteCreator;
        this.returnMapper = returnMapper;
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

        creditNoteCreator.createCreditNote(
                request.customerId(),
                request.invoiceId(),
                saved.getTotal(),
                request.reason(),
                authenticatedUserId,
                userName
        );

        return returnMapper.toResponse(saved);
    }
}
