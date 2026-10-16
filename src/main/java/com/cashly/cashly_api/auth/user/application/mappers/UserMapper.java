package com.cashly.cashly_api.auth.user.application.mappers;

import com.cashly.cashly_api.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.auth.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.from(user);
    }
}
