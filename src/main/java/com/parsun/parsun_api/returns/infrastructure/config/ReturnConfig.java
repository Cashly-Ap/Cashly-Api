package com.parsun.parsun_api.returns.infrastructure.config;

import com.parsun.parsun_api.credit_notes.application.services.CreditNoteCreatorImpl;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.returns.application.usecases.CreateReturnUseCase;
import com.parsun.parsun_api.returns.application.usecases.DeleteReturnUseCase;
import com.parsun.parsun_api.returns.application.usecases.GetAllReturnsUseCase;
import com.parsun.parsun_api.returns.application.usecases.GetReturnByIdUseCase;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.returns.domain.services.CreditNoteCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReturnConfig {

    private final ReturnRepository returnRepository;

    public ReturnConfig(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Bean
    public CreditNoteCreator creditNoteCreator(CreditNoteRepository creditNoteRepository) {
        return new CreditNoteCreatorImpl(creditNoteRepository);
    }

    @Bean
    public CreateReturnUseCase createReturnUseCase(CreditNoteCreator creditNoteCreator,
                                                     InvoiceRepository invoiceRepository) {
        return new CreateReturnUseCase(returnRepository, creditNoteCreator, invoiceRepository);
    }

    @Bean
    public GetAllReturnsUseCase getAllReturnsUseCase() {
        return new GetAllReturnsUseCase(returnRepository);
    }

    @Bean
    public GetReturnByIdUseCase getReturnByIdUseCase() {
        return new GetReturnByIdUseCase(returnRepository);
    }

    @Bean
    public DeleteReturnUseCase deleteReturnUseCase() {
        return new DeleteReturnUseCase(returnRepository);
    }
}
