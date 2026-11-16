package com.cashly.cashly_api.platform.auth.user.infrastructure.web;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.shared.storage.infrastructure.images.SafeImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserPhotoService {
    private final UserPreferencesRepository preferences;

    private final UserRepository users;
    private final AccessService access;
    private final SafeImageStorage images;

    public UserPhotoService(UserRepository users, AccessService access,
            SafeImageStorage images, UserPreferencesRepository preferences) {
        this.preferences = preferences;
        this.users = users;
        this.access = access;
        this.images = images;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse upload(String id, MultipartFile file) {
        access.lockAdministration();
        User user = find(id);
        String oldPath = user.getPhotoPath();
        user.changePhoto(
                images.store(file, "users/" + user.getId().getValue()));
        UserResponse result = UserResponse.from(users.save(user))
                .withLanguage(preferences.languageFor(user.getId().getValue()));
        images.deleteAfterCommit(oldPath);
        return result;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public UserResponse delete(String id) {
        access.lockAdministration();
        User user = find(id);
        String oldPath = user.getPhotoPath();
        user.changePhoto(null);
        UserResponse result = UserResponse.from(users.save(user))
                .withLanguage(preferences.languageFor(user.getId().getValue()));
        images.deleteAfterCommit(oldPath);
        return result;
    }

    @Transactional(readOnly = true)
    public byte[] read(String id) {
        String path = find(id).getPhotoPath();
        if (path == null)
            return null;
        return images.load(path);
    }

    private User find(String id) {
        return users.findById(UserId.from(id))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
