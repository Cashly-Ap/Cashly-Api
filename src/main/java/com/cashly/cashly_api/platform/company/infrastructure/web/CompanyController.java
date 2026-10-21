package com.cashly.cashly_api.platform.company.infrastructure.web;

import com.cashly.cashly_api.platform.company.application.CompanyService;
import com.cashly.cashly_api.platform.company.domain.Company;
import com.cashly.cashly_api.platform.company.infrastructure.CompanyPdfPreview;
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
    private final CompanyService service;
    private final CompanyPdfPreview preview;

    public CompanyController(CompanyService service, CompanyPdfPreview preview) {
        this.service = service;
        this.preview = preview;
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
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload a PNG or JPEG image");
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
    @PostMapping(value = "/pdf-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('COMPANY:UPDATE')")
    public Company uploadPdfLogo(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload a PNG or JPEG image");
        }
        return service.uploadPdfLogo(file.getBytes());
    }

    @DeleteMapping("/pdf-logo")
    @PreAuthorize("hasAuthority('COMPANY:UPDATE')")
    public Company deletePdfLogo() {
        return service.deletePdfLogo();
    }

    @GetMapping(value = "/pdf-logo", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> pdfLogo() {
        var bytes = service.pdfLogo();
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff").body(bytes);
    }
    @PostMapping(value = "/pdf-logo/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('COMPANY:UPDATE')")
    public ResponseEntity<byte[]> previewPdfLogo(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "companyLogo", required = false) MultipartFile companyLogo,
            @RequestParam(value = "removePdfLogo", defaultValue = "false") boolean removePdfLogo,
            @RequestParam(value = "removeLogo", defaultValue = "false") boolean removeLogo) throws IOException {
        var bytes = preview.generate(previewBytes(file), removePdfLogo, previewBytes(companyLogo), removeLogo);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=company-logo-preview.pdf")
                .header("X-Content-Type-Options", "nosniff").body(bytes);
    }

    private static byte[] previewBytes(MultipartFile file) throws IOException {
        if (file == null) return null;
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload a PNG or JPEG image");
        }
        return file.getBytes();
    }
}
