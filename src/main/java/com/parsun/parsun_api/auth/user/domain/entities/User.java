package com.parsun.parsun_api.auth.user.domain.entities;

import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private final UserId id;
    private final Email email;
    private UserName userName;
    private Password password;
    private UserRole role;
    private boolean active;
    private boolean emailVerified;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(UserId id, Email email, UserName userName, Password password) {
        this(id, email, userName, password, UserRole.CUSTOMER);
    }

    public User(UserId id, Email email, UserName userName, Password password, UserRole role) {
        validateParameters(id, email, userName, password);

        this.id = id;
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.role = role != null ? role : UserRole.CUSTOMER;
        this.active = true;
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private void validateParameters(UserId id, Email email, UserName userName, Password password) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (userName == null) {
            throw new IllegalArgumentException("User name cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
    }

    public void changePassword(Password newPassword) {
        if (newPassword == null) {
            throw new IllegalArgumentException("New password cannot be null");
        }
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateUserName(UserName newUserName) {
        if (newUserName == null) {
            throw new IllegalArgumentException("User name cannot be null");
        }
        this.userName = newUserName;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canAuthenticate() {
        return active;
    }

    public boolean hasEmail(Email email) {
        if (email == null) {
            return false;
        }
        return this.email.equals(email);
    }

    public UserId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public UserName getUserName() {
        return userName;
    }

    public Password getPassword() {
        return password;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasRole(UserRole role) {
        if (role == null) {
            return false;
        }
        return this.role == role;
    }

    public boolean isSuperAdmin() {
        return this.role == UserRole.SUPER_ADMIN;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isOperator() {
        return this.role == UserRole.OPERATOR;
    }

    public boolean isCustomer() {
        return this.role == UserRole.CUSTOMER;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + email +
                ", userName=" + userName +
                ", password=***" +
                ", role=" + role +
                ", active=" + active +
                ", emailVerified=" + emailVerified +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
