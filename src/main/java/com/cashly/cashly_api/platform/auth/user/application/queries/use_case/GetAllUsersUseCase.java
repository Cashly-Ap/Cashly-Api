package com.cashly.cashly_api.platform.auth.user.application.queries.use_case;

import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetAllUsersUseCase {

    private final UserRepository userRepository;

    public GetAllUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResponse<UserResponse> execute(String authenticatedUserId, UUID roleId, Boolean active,
                                               String search, int page, int size,
                                               String orderBy, String orderDir) {
        Boolean effectiveActive = active != null ? active : true;
        PageResponse<User> userPage =
            userRepository.findAllWithFilters(search, roleId, effectiveActive, authenticatedUserId, page, size, orderBy, orderDir);

        Set<UUID> idsWithRecords = userRepository.findIdsWithAssociatedRecords(
            userPage.content().stream()
                .map(u -> u.getId().getValue())
                .collect(Collectors.toSet())
        );

        List<UserResponse> content = userPage.content().stream()
            .map(u -> UserResponse.from(u, !idsWithRecords.contains(u.getId().getValue())))
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

