package com.cashly.cashly_api.platform.company.application.services;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.platform.company.domain.entity.Company;
import com.cashly.cashly_api.platform.company.domain.repository.CompanyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cashly.cashly_api.platform.company.application.ports.out.CompanyMediaStorage;
import com.cashly.cashly_api.platform.audit.application.ports.in.AuditRecorder;
@Service
public class CompanyService {
    private final CompanyRepository repository;
    private final CompanyMediaStorage images;

    private final ActivityNotificationRecorder activityNotifications;

    private final AuditRecorder auditRecorder;

    public CompanyService(CompanyRepository repository,
            CompanyMediaStorage images,
            ActivityNotificationRecorder activityNotifications,
            AuditRecorder auditRecorder) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.auditRecorder = auditRecorder != null
                ? auditRecorder
                : AuditRecorder.NONE;
        this.repository = repository;
        this.images = images;
    }

    public record AddressRequest(@Size(max = 255) String street,
            @Size(max = 100) String city, @Size(max = 100) String region,
            @Size(max = 100) String country,
            @Size(max = 30) String postalCode) {
    }

    public record Request(@NotBlank @Size(max = 200) String legalName,
            @Size(max = 200) String tradeName, @Size(max = 50) String taxId,
            @Email @Size(max = 254) String email, @Size(max = 40) String phone,
            @Size(max = 255) @Pattern(regexp = "(?i)^(https?://[^\\s]+)?$", message = "Website must use HTTP or HTTPS") String website,
            @NotNull @Valid AddressRequest address) {
    }

    @Transactional(readOnly = true)
    public Company find() {
        return repository.find();
    }

    @Transactional
    public Company update(Request request) {
        var previous = repository.lock();
        var address = request.address();
        var company = new Company(previous.id(), request.legalName().trim(),
                clean(request.tradeName()), clean(request.taxId()),
                clean(request.email()), clean(request.phone()),
                clean(request.website()),
                new Company.Address(clean(address.street()),
                        clean(address.city()), clean(address.region()),
                        clean(address.country()), clean(address.postalCode())),
                previous.logoPath(), previous.pdfLogoPath());
        repository.save(company);
        auditRecorder.record("platform", "COMPANY", company.id(),
                company.legalName(), "EDIT");
        activityNotifications.record(ActivityEvent.of("COMPANY",
                "EDIT", "COMPANY", company.id(),
                company.legalName(), null));
        return company;
    }

    @Transactional
    public Company uploadLogo(byte[] bytes) {
        var previous = repository.lock();
        var path = images.store(bytes);
        var company = withLogo(previous, path);
        repository.save(company);
        auditRecorder.record("platform", "COMPANY", company.id(),
                company.legalName(), "UPLOAD_PHOTO");
        images.deleteAfterCommit(previous.logoPath());
        return company;
    }

    @Transactional
    public Company deleteLogo() {
        var previous = repository.lock();
        var company = withLogo(previous, null);
        repository.save(company);
        auditRecorder.record("platform", "COMPANY", company.id(),
                company.legalName(), "REMOVE_PHOTO");
        images.deleteAfterCommit(previous.logoPath());
        return company;
    }

    @Transactional(readOnly = true)
    public byte[] logo() {
        String path = repository.find().logoPath();
        return path == null ? null : images.load(path);
    }

    @Transactional
    public Company uploadPdfLogo(byte[] bytes) {
        var previous = repository.lock();
        var company = withPdfLogo(previous, images.store(bytes));
        repository.save(company);
        auditRecorder.record("platform", "COMPANY", company.id(),
                company.legalName(), "UPLOAD_PHOTO");
        images.deleteAfterCommit(previous.pdfLogoPath());
        return company;
    }

    @Transactional
    public Company deletePdfLogo() {
        var previous = repository.lock();
        var company = withPdfLogo(previous, null);
        repository.save(company);
        auditRecorder.record("platform", "COMPANY", company.id(),
                company.legalName(), "REMOVE_PHOTO");
        images.deleteAfterCommit(previous.pdfLogoPath());
        return company;
    }

    @Transactional(readOnly = true)
    public byte[] pdfLogo() {
        String path = repository.find().pdfLogoPath();
        return path == null ? null : images.load(path);
    }

    @Transactional(readOnly = true)
    public byte[] documentLogo() {
        var company = repository.find();
        var path = company.pdfLogoPath() != null
                ? company.pdfLogoPath()
                : company.logoPath();
        return path == null ? null : images.load(path);
    }

    @Transactional(readOnly = true)
    public byte[] previewDocumentLogo(byte[] pdfLogo, boolean removePdfLogo,
            byte[] logo, boolean removeLogo) {
        var company = repository.find();
        if (pdfLogo != null)
            return pdfLogo;
        if (!removePdfLogo && company.pdfLogoPath() != null)
            return images.load(company.pdfLogoPath());
        if (logo != null)
            return logo;
        return !removeLogo && company.logoPath() != null
                ? images.load(company.logoPath())
                : null;
    }

    private static Company withPdfLogo(Company company, String path) {
        return new Company(company.id(), company.legalName(),
                company.tradeName(), company.taxId(), company.email(),
                company.phone(), company.website(), company.address(),
                company.logoPath(), path);
    }

    private static Company withLogo(Company company, String path) {
        return new Company(company.id(), company.legalName(),
                company.tradeName(), company.taxId(), company.email(),
                company.phone(), company.website(), company.address(), path,
                company.pdfLogoPath());
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
