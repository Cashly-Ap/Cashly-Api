package com.cashly.cashly_api.price_types.infrastructure.config;

import com.cashly.cashly_api.price_types.application.usecases.*;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import com.cashly.cashly_api.price_types.application.contracts.PriceTypeUsageGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PriceTypeConfig {

    @Bean
    public CreatePriceTypeUseCase createPriceTypeUseCase(PriceTypeRepository priceTypeRepository) {
        return new CreatePriceTypeUseCase(priceTypeRepository);
    }

    @Bean
    public UpdatePriceTypeUseCase updatePriceTypeUseCase(PriceTypeRepository priceTypeRepository) {
        return new UpdatePriceTypeUseCase(priceTypeRepository);
    }

    @Bean
    public DeletePriceTypeUseCase deletePriceTypeUseCase(PriceTypeUsageGateway usageGateway, PriceTypeRepository priceTypeRepository) {
        return new DeletePriceTypeUseCase(usageGateway, priceTypeRepository);
    }

    @Bean
    public GetPriceTypeUseCase getPriceTypeUseCase(PriceTypeRepository priceTypeRepository) {
        return new GetPriceTypeUseCase(priceTypeRepository);
    }

    @Bean
    public ListPriceTypesUseCase listPriceTypesUseCase(PriceTypeUsageGateway usageGateway, PriceTypeRepository priceTypeRepository) {
        return new ListPriceTypesUseCase(usageGateway, priceTypeRepository);
    }
}
