package com.parsun.parsun_api.returns.infrastructure.config;

import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.returns.application.mappers.ReturnMapper;
import com.parsun.parsun_api.returns.application.usecases.CreateReturnUseCase;
import com.parsun.parsun_api.returns.application.usecases.DeleteReturnUseCase;
import com.parsun.parsun_api.returns.application.usecases.GetAllReturnsUseCase;
import com.parsun.parsun_api.returns.application.usecases.GetReturnByIdUseCase;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReturnConfig {

    private final ReturnRepository returnRepository;

    public ReturnConfig(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Bean
    public CreateReturnUseCase createReturnUseCase(InvoiceRepository invoiceRepository,
                                                      ReturnMapper returnMapper) {
        return new CreateReturnUseCase(returnRepository, invoiceRepository, returnMapper);
    }

    @Bean
    public GetAllReturnsUseCase getAllReturnsUseCase(ReturnMapper returnMapper) {
        return new GetAllReturnsUseCase(returnRepository, returnMapper);
    }

    @Bean
    public GetReturnByIdUseCase getReturnByIdUseCase(ReturnMapper returnMapper) {
        return new GetReturnByIdUseCase(returnRepository, returnMapper);
    }

    @Bean
    public DeleteReturnUseCase deleteReturnUseCase() {
        return new DeleteReturnUseCase(returnRepository);
    }
}
