package com.cashly.cashly_api.platform.auth.user.domain.repository;

import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    boolean canAuthenticate(UserId id);

    boolean hasAssociatedRecords(UserId id);
    Set<UUID> findIdsWithAssociatedRecords(Set<UUID> ids);
    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void delete(User user);

    List<User> findAll();

    List<User> findByRole(UserRole role);

    PageResponse<User> findAllWithFilters(String search, UUID roleId, Boolean active, String excludeId, int page, int size, String orderBy, String orderDir);
}
