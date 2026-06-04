package com.parsun.parsun_api.advances.application.usecases;

import com.parsun.parsun_api.advances.application.dto.request.CreateAdvanceRequest;
import com.parsun.parsun_api.advances.application.dto.response.AdvanceResponse;
import com.parsun.parsun_api.advances.application.mappers.AdvanceMapper;
import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceRepository;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceNumber;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class CreateAdvanceUseCase {

    private final AdvanceRepository advanceRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CreateAdvanceUseCase(AdvanceRepository advanceRepository,
                                 CustomerRepository customerRepository,
                                 UserRepository userRepository) {
        this.advanceRepository = advanceRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AdvanceResponse execute(CreateAdvanceRequest request, UUID authenticatedUserId) {
        customerRepository.findById(new CustomerId(request.customerId()))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        String createdByName = userRepository.findById(new UserId(authenticatedUserId))
                .map(user -> user.getUserName().getValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        // Create advance record
        int advanceYear = LocalDate.now().getYear();
        int advanceSequence = advanceRepository.getNextSequenceForYear(advanceYear);
        AdvanceNumber advanceNumber = AdvanceNumber.of(advanceYear, advanceSequence);

        Advance advance = Advance.create(
                advanceNumber,
                request.customerId(),
                request.amount(),
                request.notes(),
                authenticatedUserId
        );

        Advance saved = advanceRepository.save(advance);
        return AdvanceMapper.toResponse(saved);
    }
}
