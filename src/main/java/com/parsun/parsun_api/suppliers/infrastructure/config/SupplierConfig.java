package com.parsun.parsun_api.suppliers.infrastructure.config;

import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.suppliers.application.mappers.SupplierMapper;
import com.parsun.parsun_api.suppliers.application.usecases.*;
import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.suppliers.domain.services.SupplierCodeGenerator;
import com.parsun.parsun_api.suppliers.infrastructure.services.SequentialSupplierCodeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupplierConfig {

    @Bean
    public SupplierMapper supplierMapper() {
        return new SupplierMapper();
    }

    @Bean
    public SupplierCodeGenerator supplierCodeGenerator(SupplierRepository supplierRepository) {
        return new SequentialSupplierCodeGenerator(supplierRepository);
    }

    @Bean
    public RegisterSupplierUseCase registerSupplierUseCase(SupplierRepository supplierRepository,
                                                            SupplierCodeGenerator supplierCodeGenerator,
                                                            SupplierMapper supplierMapper) {
        return new RegisterSupplierUseCase(supplierRepository, supplierCodeGenerator, supplierMapper);
    }

    @Bean
    public GetSupplierUseCase getSupplierUseCase(SupplierRepository supplierRepository,
                                                  SupplierMapper supplierMapper) {
        return new GetSupplierUseCase(supplierRepository, supplierMapper);
    }

    @Bean
    public ListSuppliersUseCase listSuppliersUseCase(SupplierRepository supplierRepository,
                                                       SupplierMapper supplierMapper,
                                                       PurchaseRepository purchaseRepository) {
        return new ListSuppliersUseCase(supplierRepository, supplierMapper, purchaseRepository);
    }

    @Bean
    public UpdateSupplierUseCase updateSupplierUseCase(SupplierRepository supplierRepository,
                                                        SupplierMapper supplierMapper) {
        return new UpdateSupplierUseCase(supplierRepository, supplierMapper);
    }

    @Bean
    public DeleteSupplierUseCase deleteSupplierUseCase(SupplierRepository supplierRepository,
                                                        PurchaseRepository purchaseRepository) {
        return new DeleteSupplierUseCase(supplierRepository, purchaseRepository);
    }
}
