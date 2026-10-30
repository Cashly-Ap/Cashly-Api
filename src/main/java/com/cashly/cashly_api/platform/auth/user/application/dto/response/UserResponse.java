package com.cashly.cashly_api.platform.auth.user.application.dto.response;

import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
    String id,
    String email,
    String userName,
    UserRole role,
    boolean active,
    boolean emailVerified,
    boolean deletable,
    Set<BusinessModule> modulePermissions,
    String firstName, String lastName,
    com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile.Address address,
    String phone,
    java.util.UUID roleId, java.util.UUID departmentId,
    String roleName, String departmentName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String photoPath,
    String language
) {
    public UserResponse(String id, String email, String userName, UserRole role, boolean active,
            boolean emailVerified, boolean deletable, Set<BusinessModule> modulePermissions,
            String firstName, String lastName,
            com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile.Address address,
            String phone, java.util.UUID roleId, java.util.UUID departmentId, String roleName,
            String departmentName, LocalDateTime createdAt, LocalDateTime updatedAt, String photoPath) {
        this(id, email, userName, role, active, emailVerified, deletable, modulePermissions,
            firstName, lastName, address, phone, roleId, departmentId, roleName, departmentName,
            createdAt, updatedAt, photoPath, "es");
    }

    public UserResponse withLanguage(String language) {
        return new UserResponse(id, email, userName, role, active, emailVerified, deletable, modulePermissions,
            firstName, lastName, address, phone, roleId, departmentId, roleName, departmentName,
            createdAt, updatedAt, photoPath, language);
    }

    public UserResponse(String id, String email, String userName, UserRole role, boolean active,
            boolean emailVerified, boolean deletable, Set<BusinessModule> modulePermissions,
            String firstName, String lastName,
            com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserProfile.Address address,
            String phone, java.util.UUID roleId, java.util.UUID departmentId, String roleName,
            String departmentName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, email, userName, role, active, emailVerified, deletable, modulePermissions,
            firstName, lastName, address, phone, roleId, departmentId, roleName, departmentName,
            createdAt, updatedAt, null);
    }

    public static UserResponse from(User user) {
        return from(user, true);
    }

    public static UserResponse from(User user, boolean deletable) {
        return new UserResponse(
            user.getId().getValue().toString(),
            user.getEmail().getValue(),
            user.getUserName().getValue(),
            user.getRole(),
            user.isActive(),
            user.isEmailVerified(),
            deletable,
            user.getModulePermissions(),
            user.getProfile()==null?null:user.getProfile().firstName(),
            user.getProfile()==null?null:user.getProfile().lastName(),
            user.getProfile()==null?null:user.getProfile().address(),
            user.getProfile()==null?null:user.getProfile().phone(),
            user.getRoleId(),user.getDepartmentId(),
            user.getRoleName(),user.getDepartmentName(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getPhotoPath()
        );
    }
}
