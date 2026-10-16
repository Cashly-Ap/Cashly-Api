package com.cashly.cashly_api.suppliers.infrastructure.config;

import com.cashly.cashly_api.suppliers.application.ports.out.PurchaseUsageGateway;
import com.cashly.cashly_api.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.suppliers.application.delete.use_case.DeleteSupplierUseCase;
import com.cashly.cashly_api.suppliers.application.queries.use_case.GetSupplierUseCase;
import com.cashly.cashly_api.suppliers.application.queries.use_case.ListSuppliersUseCase;
import com.cashly.cashly_api.suppliers.application.register.use_case.RegisterSupplierUseCase;
import com.cashly.cashly_api.suppliers.application.update.use_case.UpdateSupplierUseCase;
import com.cashly.cashly_api.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.services.SupplierCodeGenerator;
import com.cashly.cashly_api.suppliers.infrastructure.services.SequentialSupplierCodeGenerator;
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
                                                       PurchaseUsageGateway purchaseUsage) {
        return new ListSuppliersUseCase(supplierRepository, supplierMapper, purchaseUsage);
    }

    @Bean
    public UpdateSupplierUseCase updateSupplierUseCase(SupplierRepository supplierRepository,
                                                        SupplierMapper supplierMapper) {
        return new UpdateSupplierUseCase(supplierRepository, supplierMapper);
    }

    @Bean
    public DeleteSupplierUseCase deleteSupplierUseCase(SupplierRepository supplierRepository,
                                                        PurchaseUsageGateway purchaseUsage) {
        return new DeleteSupplierUseCase(supplierRepository, purchaseUsage);
    }
}
