package com.parsun.parsun_api.auth.user.application.mappers;

import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId().getValue().toString(),
            user.getEmail().getValue(),
            user.getUserName().getValue(),
            user.getRole(),
            user.isActive(),
            user.isEmailVerified(),
            user.getCommissionDeductionPercentage().value(),
            user.getCreatedAt()
        );
    }
}
