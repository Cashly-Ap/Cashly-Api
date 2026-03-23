package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.request.ChangeCommissionDeductionRequest;
import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.application.exceptions.UserNotFoundException;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.CommissionDeductionPercentage;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCommissionDeductionUseCase {

    private final UserRepository userRepository;

    public ChangeCommissionDeductionUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse execute(String userId, ChangeCommissionDeductionRequest request) {
        UserId id = UserId.from(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.updateCommissionDeduction(
            CommissionDeductionPercentage.of(request.commissionDeductionPercentage())
        );

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}
