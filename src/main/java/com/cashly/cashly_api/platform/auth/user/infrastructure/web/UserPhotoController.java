package com.cashly.cashly_api.platform.auth.user.infrastructure.web;

import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserPhotoController {
    private final UserPhotoService photos;
    public UserPhotoController(UserPhotoService photos) {
        this.photos = photos;
    }

    @GetMapping("/api/auth/profile/photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> ownPhoto(Authentication authentication) {
        return image(photos.read(authentication.getName()));
    }

    @PostMapping(value = "/api/auth/profile/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public UserResponse uploadOwnPhoto(Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        return photos.upload(authentication.getName(), file);
    }

    @DeleteMapping("/api/auth/profile/photo")
    @PreAuthorize("isAuthenticated()")
    public UserResponse deleteOwnPhoto(Authentication authentication) {
        return photos.delete(authentication.getName());
    }

    @GetMapping("/api/users/{id}/photo")
    @PreAuthorize("hasAuthority('USERS:READ')")
    public ResponseEntity<byte[]> userPhoto(@PathVariable String id) {
        return image(photos.read(id));
    }

    @PostMapping(value = "/api/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('USERS:UPDATE')")
    public UserResponse uploadUserPhoto(@PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        return photos.upload(id, file);
    }

    @DeleteMapping("/api/users/{id}/photo")
    @PreAuthorize("hasAuthority('USERS:UPDATE')")
    public UserResponse deleteUserPhoto(@PathVariable String id) {
        return photos.delete(id);
    }

    private ResponseEntity<byte[]> image(byte[] bytes) {
        if (bytes == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.IMAGE_PNG).body(bytes);
    }
}
