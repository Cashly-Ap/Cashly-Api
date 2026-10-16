package com.cashly.cashly_api.returns.infrastructure.config;
import com.cashly.cashly_api.returns.application.ports.out.ReturnBalanceGateway;
import com.cashly.cashly_api.returns.application.ports.out.ReturnCashGateway;
import com.cashly.cashly_api.returns.application.ports.out.ReturnInventoryGateway;
import com.cashly.cashly_api.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.returns.application.ports.out.ReturnInvoiceQueryGateway;
import com.cashly.cashly_api.returns.application.ports.out.ReturnPaymentSettlementGateway;
import com.cashly.cashly_api.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.returns.application.create.CreateReturnUseCase;
import com.cashly.cashly_api.returns.application.delete.DeleteReturnUseCase;
import com.cashly.cashly_api.returns.application.list.GetAllReturnsUseCase;
import com.cashly.cashly_api.returns.application.get.GetReturnByIdUseCase;
import com.cashly.cashly_api.returns.domain.ReturnRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnConfig {
    private final ReturnRepository returnRepository;
    public ReturnConfig(ReturnRepository returnRepository) { this.returnRepository = returnRepository; }
    @Bean
    public CreateReturnUseCase createReturnUseCase(ReturnInvoiceAdjustmentGateway adjustments, ReturnResponseAssembler responses,
            ReturnInvoiceQueryGateway invoices, ReturnPaymentSettlementGateway payments, ReturnBalanceGateway balances,
            ReturnInventoryGateway inventory, ReturnCashGateway cash) {
        return new CreateReturnUseCase(adjustments, returnRepository, responses, invoices, payments, balances, inventory, cash);
    }
    @Bean
    public GetAllReturnsUseCase getAllReturnsUseCase(ReturnResponseAssembler responses) { return new GetAllReturnsUseCase(returnRepository, responses); }
    @Bean
    public GetReturnByIdUseCase getReturnByIdUseCase(ReturnResponseAssembler responses) { return new GetReturnByIdUseCase(returnRepository, responses); }
    @Bean
    public DeleteReturnUseCase deleteReturnUseCase(ReturnInvoiceAdjustmentGateway adjustments, ReturnInvoiceQueryGateway invoices,
            ReturnInventoryGateway inventory, ReturnPaymentSettlementGateway payments, ReturnBalanceGateway balances) {
        return new DeleteReturnUseCase(adjustments, returnRepository, invoices, inventory, payments, balances);
    }
}
