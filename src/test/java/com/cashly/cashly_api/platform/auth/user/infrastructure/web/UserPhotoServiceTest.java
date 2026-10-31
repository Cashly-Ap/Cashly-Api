package com.cashly.cashly_api.platform.auth.user.infrastructure.web;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.*;
import com.cashly.cashly_api.shared.storage.infrastructure.images.SafeImageStorage;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserPhotoServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AccessService access = mock(AccessService.class);
    private final SafeImageStorage images = mock(SafeImageStorage.class);
    private final UserPhotoService service = new UserPhotoService(users, access,
            images,
            mock(com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository.class));
    private User user() {
        var user = new User(UserId.generate(), new Email("photo@example.com"),
                new UserName("Photo User"), Password.fromHash("hash"));
        user.changePhoto("old.png");
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);
        return user;
    }
    @Test
    void replacingPhotoSavesBeforeCleaningOldImageAndPreservesCredentials() {
        var user = user();
        var file = new MockMultipartFile("file", "photo.png", "image/png",
                new byte[]{1});
        when(images.store(file, "users/" + user.getId().getValue()))
                .thenReturn("new.png");
        var response = service.upload(user.getId().getValue().toString(), file);
        assertThat(response.photoPath()).isEqualTo("new.png");
        assertThat(user.getPassword().getHashedValue()).isEqualTo("hash");
        var order = inOrder(access, users, images);
        order.verify(access).lockAdministration();
        order.verify(users).findById(user.getId());
        order.verify(images).store(file, "users/" + user.getId().getValue());
        order.verify(users).save(user);
        order.verify(images).deleteAfterCommit("old.png");
    }
    @Test
    void failedPersistenceNeverDeletesPreviousPhoto() {
        var user = user();
        var file = new MockMultipartFile("file", new byte[]{1});
        when(images.store(
                any(org.springframework.web.multipart.MultipartFile.class),
                anyString())).thenReturn("new.png");
        when(users.save(user))
                .thenThrow(new IllegalStateException("DB unavailable"));
        assertThatThrownBy(
                () -> service.upload(user.getId().getValue().toString(), file))
                .isInstanceOf(IllegalStateException.class);
        verify(images, never()).deleteAfterCommit(any());
    }
    @Test
    void deletingPhotoClearsReferenceBeforeCleaningFile() {
        var user = user();
        assertThat(
                service.delete(user.getId().getValue().toString()).photoPath())
                .isNull();
        verify(images).deleteAfterCommit("old.png");
    }
    @Test
    void missingPhotoReturnsNotFoundWithoutReadingStorage() {
        var user = user();
        user.changePhoto(null);
        assertThat(service.read(user.getId().getValue().toString())).isNull();
        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn(user.getId().getValue().toString());
        assertThat(new UserPhotoController(service).ownPhoto(auth)
                .getStatusCode().value()).isEqualTo(404);
        verifyNoInteractions(images);
    }
    @Test
    void ownControllerAlwaysUsesAuthenticatedIdentity() {
        var photos = mock(UserPhotoService.class);
        var auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("authenticated-user");
        when(photos.read("authenticated-user")).thenReturn(new byte[]{1});
        var response = new UserPhotoController(photos).ownPhoto(auth);
        assertThat(response.getHeaders().getCacheControl())
                .isEqualTo("no-store");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options"))
                .isEqualTo("nosniff");
        verify(photos).read("authenticated-user");
    }
}
