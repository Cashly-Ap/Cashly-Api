package com.parsun.parsun_api.auth.user.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.infrastructure.persistence.entities.UserEntity;


@Component
public class JpaUserRepository implements UserRepository {

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
    public void delete(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        springDataUserRepository.deleteById(user.getId().getValue());
    }
}
