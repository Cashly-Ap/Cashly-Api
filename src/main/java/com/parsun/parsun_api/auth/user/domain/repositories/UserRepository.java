package com.parsun.parsun_api.auth.user.domain.repositories;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void delete(User user);

    List<User> findAll();

    List<User> findByRole(UserRole role);
}
