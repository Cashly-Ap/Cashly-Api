package com.cashly.cashly_api.cash_register.infrastructure.config;
import com.cashly.cashly_api.cash_register.application.ports.in.ReturnCashRecording;
import com.cashly.cashly_api.cash_register.application.services.ReturnCashRecordingService;
import com.cashly.cashly_api.cash_register.application.services.CashMovementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnCashRecordingConfig {
    @Bean
    public ReturnCashRecording returnCashRecording(CashMovementService movements) { return new ReturnCashRecordingService(movements); }
}
