package com.parsun.parsun_api.auth.user.application.mappers;

import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.from(user);
    }
}
