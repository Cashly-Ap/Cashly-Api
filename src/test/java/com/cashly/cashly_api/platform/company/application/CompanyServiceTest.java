package com.cashly.cashly_api.platform.company.application;
import com.cashly.cashly_api.platform.company.domain.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class CompanyServiceTest {
    @Test void updatingDetailsPreservesInstallationAndBranding() {
        var repository=mock(CompanyRepository.class); var images=mock(CompanyMediaStorage.class);
        var existing=new Company(Company.INSTALLATION_ID,"Old",null,null,null,null,null,new Company.Address(null,null,null,null,null),"company/logo.png");
        when(repository.lock()).thenReturn(existing);
        var request=new CompanyService.Request(" New Company "," ","B123",null,null,null,new CompanyService.AddressRequest("Street",null,null,"Spain","28001"));
        var updated=new CompanyService(repository,images).update(request);
        assertThat(updated.id()).isEqualTo(Company.INSTALLATION_ID);
        assertThat(updated.logoPath()).isEqualTo(existing.logoPath());
        assertThat(updated.legalName()).isEqualTo("New Company");
        assertThat(updated.tradeName()).isNull();
        verify(repository).save(updated); verifyNoInteractions(images);
    }
    @Test void removingLogoSchedulesCleanupAndKeepsCompany() {
        var repository=mock(CompanyRepository.class); var images=mock(CompanyMediaStorage.class);
        var existing=new Company(Company.INSTALLATION_ID,"Company",null,null,null,null,null,new Company.Address(null,null,null,null,null),"company/logo.png");
        when(repository.lock()).thenReturn(existing);
        var updated=new CompanyService(repository,images).deleteLogo();
        assertThat(updated.logoPath()).isNull(); verify(repository).save(updated); verify(images).deleteAfterCommit("company/logo.png");
    }
}
