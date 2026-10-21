package com.cashly.cashly_api.platform.company.infrastructure.web;

import com.cashly.cashly_api.platform.company.application.CompanyService;
import com.cashly.cashly_api.platform.company.domain.Company;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/company")
public class CompanyController {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Company find() {
        return service.find();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('COMPANY:UPDATE')")
    public Company update(@Valid @RequestBody CompanyService.Request request) {
        return service.update(request);
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('COMPANY:UPDATE')")
    public Company upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Upload a PNG or JPEG image up to 5 MB");
        }
        return service.uploadLogo(file.getBytes());
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasAuthority('COMPANY:UPDATE')")
    public Company deleteLogo() {
        return service.deleteLogo();
    }

    @GetMapping(value = "/logo", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> logo() {
        var bytes = service.logo();
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff").body(bytes);
    }
}
