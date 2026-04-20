package com.parsun.parsun_api.auth.user.application.usecases;

import com.parsun.parsun_api.auth.user.application.dto.response.UserResponse;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetAllUsersUseCase {

    private final UserRepository userRepository;

    public GetAllUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResponse<UserResponse> execute(String authenticatedUserId, UserRole role, Boolean active,
                                               String search, int page, int size,
                                               String orderBy, String orderDir) {
        Boolean effectiveActive = active != null ? active : true;
        PageResponse<com.parsun.parsun_api.auth.user.domain.entities.User> userPage =
            userRepository.findAllWithFilters(search, role, effectiveActive, authenticatedUserId, page, size, orderBy, orderDir);

        List<UserResponse> content = userPage.content().stream()
            .map(UserResponse::from)
            .toList();

        return new PageResponse<>(
            content,
            userPage.totalElements(),
            userPage.totalPages(),
            userPage.size(),
            userPage.number(),
            userPage.first(),
            userPage.last(),
            userPage.empty()
        );
    }
}

