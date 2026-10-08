package com.cashly.cashly_api.auth.user.infrastructure.persistence.repositories;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.auth.user.infrastructure.persistence.entities.UserEntity;
import com.cashly.cashly_api.shared.dto.PageResponse;


@Component
public class JpaUserRepository implements UserRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("userName", "email", "role", "createdAt");

    private final SpringDataUserRepository springDataUserRepository;

    public JpaUserRepository(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Optional<UserEntity> existingEntity = springDataUserRepository.findById(user.getId().getValue());

        UserEntity entity;
        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            entity.updateFromDomain(user);
        } else {
            entity = UserEntity.fromDomain(user);
        }

        UserEntity savedEntity = springDataUserRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<User> findById(UserId id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return springDataUserRepository.findById(id.getValue())
            .map(UserEntity::toDomain);
    }

    @Override
    public boolean canAuthenticate(UserId id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return springDataUserRepository.canAuthenticate(id.getValue());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        return springDataUserRepository.findByEmail(email.getValue())
            .map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        return springDataUserRepository.existsByEmail(email.getValue());
    }

    @Override
    public boolean hasAssociatedRecords(UserId id) {
        return !findIdsWithAssociatedRecords(Set.of(id.getValue())).isEmpty();
    }

    @Override
    public Set<UUID> findIdsWithAssociatedRecords(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        return springDataUserRepository.findIdsWithAssociatedRecords(ids);
    }

    @Override
    public void delete(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        springDataUserRepository.deleteById(user.getId().getValue());
    }

    @Override
    public List<User> findAll() {
        return springDataUserRepository.findAll().stream()
            .map(UserEntity::toDomain)
            .toList();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        return springDataUserRepository.findByRole(role).stream()
            .map(UserEntity::toDomain)
            .toList();
    }

    @Override
    public PageResponse<User> findAllWithFilters(String search, UserRole role, Boolean active, String excludeId,
                                                  int page, int size, String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "userName";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        UUID excludeUuid = excludeId != null ? UUID.fromString(excludeId) : null;
        Page<UserEntity> result = springDataUserRepository.findAllWithFilters(search, role, active, excludeUuid, pageable);
        Page<User> mapped = result.map(UserEntity::toDomain);
        return PageResponseMapper.from(mapped);
    }
}
