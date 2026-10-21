package com.cashly.cashly_api.platform.auth.user.domain.entity;

import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class User {
    private final UserId id;
    private String photoPath;
    public String getPhotoPath() { return photoPath; }
    public void changePhoto(String photoPath) {
        this.photoPath = photoPath;
        this.updatedAt = LocalDateTime.now();
    }
    private Email email;
    private UserName userName;
    private Password password;
    private UserRole role;
    private com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile profile;
    private String roleName;
    private String departmentName;
    public String getRoleName() { return roleName; }
    public String getDepartmentName() { return departmentName; }
    public void setAssignmentNames(String roleName,String departmentName) { this.roleName=roleName;this.departmentName=departmentName; }
    private java.util.UUID roleId;
    private java.util.UUID departmentId;
    public com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile getProfile() { return profile; }
    public java.util.UUID getRoleId() { return roleId; }
    public java.util.UUID getDepartmentId() { return departmentId; }
    public void updateProfile(com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile profile,java.util.UUID roleId,java.util.UUID departmentId) {
        this.profile=java.util.Objects.requireNonNull(profile);
        this.roleId=java.util.Objects.requireNonNull(roleId);
        this.departmentId=java.util.Objects.requireNonNull(departmentId);
        this.updatedAt=LocalDateTime.now();
    }

    public void updatePersonalProfile(com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile profile) {
        this.profile = java.util.Objects.requireNonNull(profile);
        this.updatedAt = LocalDateTime.now();
    }

    private boolean active;
    private boolean emailVerified;
    private Set<BusinessModule> modulePermissions;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(UserId id, Email email, UserName userName, Password password) {
        this(id, email, userName, password, UserRole.OPERATOR);
    }

    public User(UserId id, Email email, UserName userName, Password password, UserRole role) {
        validateParameters(id, email, userName, password);

        this.id = id;
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.role = role != null ? role : UserRole.OPERATOR;
        this.active = true;
        this.emailVerified = false;
        this.modulePermissions = EnumSet.noneOf(BusinessModule.class);
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

    public void updateEmail(Email newEmail) {
        if (newEmail == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        this.email = newEmail;
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

    public Set<BusinessModule> getModulePermissions() {
        return Collections.unmodifiableSet(modulePermissions);
    }

    public void grantPermission(BusinessModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        }
        this.modulePermissions.add(module);
        this.updatedAt = LocalDateTime.now();
    }

    public void revokePermission(BusinessModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        }
        this.modulePermissions.remove(module);
        this.updatedAt = LocalDateTime.now();
    }

    public void setModulePermissions(Set<BusinessModule> permissions) {
        this.modulePermissions = (permissions != null && !permissions.isEmpty())
                ? EnumSet.copyOf(permissions)
                : EnumSet.noneOf(BusinessModule.class);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasModulePermission(BusinessModule module) {
        return modulePermissions.contains(module);
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
                ", modulePermissions=" + modulePermissions +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
