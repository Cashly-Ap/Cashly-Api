package com.cashly.cashly_api.platform.company.application.services;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.cashly.cashly_api.platform.company.domain.entity.Company;
import com.cashly.cashly_api.platform.company.domain.repository.CompanyRepository;
import com.cashly.cashly_api.platform.company.application.ports.out.CompanyMediaStorage;
class CompanyServiceTest {
    @Test
    void updatingDetailsPreservesInstallationAndBranding() {
        var repository = mock(CompanyRepository.class);
        var images = mock(CompanyMediaStorage.class);
        var existing = new Company(Company.INSTALLATION_ID, "Old", null, null,
                null, null, null,
                new Company.Address(null, null, null, null, null),
                "company/logo.png", null);
        when(repository.lock()).thenReturn(existing);
        var request = new CompanyService.Request(" New Company ", " ", "B123",
                null, null, null, new CompanyService.AddressRequest("Street",
                        null, null, "Spain", "28001"));
        var updated = new CompanyService(repository, images, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class)).update(request);
        assertThat(updated.id()).isEqualTo(Company.INSTALLATION_ID);
        assertThat(updated.logoPath()).isEqualTo(existing.logoPath());
        assertThat(updated.legalName()).isEqualTo("New Company");
        assertThat(updated.tradeName()).isNull();
        verify(repository).save(updated);
        verifyNoInteractions(images);
    }
    @Test
    void removingLogoSchedulesCleanupAndKeepsCompany() {
        var repository = mock(CompanyRepository.class);
        var images = mock(CompanyMediaStorage.class);
        var existing = new Company(Company.INSTALLATION_ID, "Company", null,
                null, null, null, null,
                new Company.Address(null, null, null, null, null),
                "company/logo.png", null);
        when(repository.lock()).thenReturn(existing);
        var updated = new CompanyService(repository, images, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class)).deleteLogo();
        assertThat(updated.logoPath()).isNull();
        verify(repository).save(updated);
        verify(images).deleteAfterCommit("company/logo.png");
    }
    @Test
    void replacingPdfLogoKeepsNavbarLogoAndDeletesOnlyPreviousPdfLogo() {
        var repository = mock(CompanyRepository.class);
        var images = mock(CompanyMediaStorage.class);
        var existing = company("company/navbar.png", "company/old-pdf.png");
        when(repository.lock()).thenReturn(existing);
        when(images.store(new byte[]{1})).thenReturn("company/new-pdf.png");
        var updated = new CompanyService(repository, images, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class))
                .uploadPdfLogo(new byte[]{1});
        assertThat(updated.logoPath()).isEqualTo("company/navbar.png");
        assertThat(updated.pdfLogoPath()).isEqualTo("company/new-pdf.png");
        verify(repository).save(updated);
        verify(images).deleteAfterCommit("company/old-pdf.png");
        verify(images, never()).deleteAfterCommit("company/navbar.png");
    }
    @Test
    void removingPdfLogoKeepsNavbarLogoAndRestoresDocumentFallback() {
        var repository = mock(CompanyRepository.class);
        var images = mock(CompanyMediaStorage.class);
        when(repository.lock())
                .thenReturn(company("company/navbar.png", "company/pdf.png"));
        var service = new CompanyService(repository, images, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
        var updated = service.deletePdfLogo();
        assertThat(updated.pdfLogoPath()).isNull();
        assertThat(updated.logoPath()).isEqualTo("company/navbar.png");
        verify(images).deleteAfterCommit("company/pdf.png");
        when(repository.find()).thenReturn(updated);
        when(images.load("company/navbar.png")).thenReturn(new byte[]{3});
        assertThat(service.documentLogo()).containsExactly(3);
        assertThat(service.pdfLogo()).isNull();
    }
    @Test
    void documentsPreferPdfLogoAndUseDefaultWhenNeitherLogoExists() {
        var repository = mock(CompanyRepository.class);
        var images = mock(CompanyMediaStorage.class);
        var service = new CompanyService(repository, images, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
        when(repository.find())
                .thenReturn(company("company/navbar.png", "company/pdf.png"));
        when(images.load("company/pdf.png")).thenReturn(new byte[]{2});
        assertThat(service.documentLogo()).containsExactly(2);
        verify(images, never()).load("company/navbar.png");
        when(repository.find()).thenReturn(company(null, null));
        assertThat(service.documentLogo()).isNull();
    }
    @Test
    void updatingDetailsPreservesDedicatedPdfLogo() {
        var repository = mock(CompanyRepository.class);
        var images = mock(CompanyMediaStorage.class);
        when(repository.lock())
                .thenReturn(company("company/navbar.png", "company/pdf.png"));
        var updated = new CompanyService(repository, images, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class))
                .update(new CompanyService.Request("Company", null, null, null,
                        null, null, new CompanyService.AddressRequest(null,
                                null, null, null, null)));
        assertThat(updated.pdfLogoPath()).isEqualTo("company/pdf.png");
    }
    @Test
    void previewHonorsUnsavedChangesAndRemovalFlagsWithoutWrites() {
        var repository = mock(CompanyRepository.class);
        var images = mock(CompanyMediaStorage.class);
        when(repository.find()).thenReturn(company("navbar", "pdf"));
        when(images.load("navbar")).thenReturn(new byte[]{1});
        when(images.load("pdf")).thenReturn(new byte[]{2});
        var service = new CompanyService(repository, images, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
        assertThat(service.previewDocumentLogo(new byte[]{3}, false,
                new byte[]{4}, false)).containsExactly(3);
        assertThat(
                service.previewDocumentLogo(null, false, new byte[]{4}, false))
                .containsExactly(2);
        assertThat(
                service.previewDocumentLogo(null, true, new byte[]{4}, false))
                .containsExactly(4);
        assertThat(service.previewDocumentLogo(null, true, null, false))
                .containsExactly(1);
        assertThat(service.previewDocumentLogo(null, true, null, true))
                .isNull();
        assertThat(service.previewDocumentLogo(new byte[]{3}, true, null, true))
                .containsExactly(3);
        verify(repository, never()).lock();
        verify(repository, never()).save(any());
        verify(images, never()).store(any());
        verify(images, never()).deleteAfterCommit(any());
    }
    private Company company(String logo, String pdfLogo) {
        return new Company(Company.INSTALLATION_ID, "Company", null, null, null,
                null, null, new Company.Address(null, null, null, null, null),
                logo, pdfLogo);
    }
}
