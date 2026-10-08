package com.cashly.cashly_api.cashregister.infrastructure.config;
import com.cashly.cashly_api.cashregister.application.api.ReturnCashRecording;
import com.cashly.cashly_api.cashregister.application.services.ReturnCashRecordingService;
import com.cashly.cashly_api.cashregister.application.services.CashMovementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnCashRecordingConfig {
    @Bean
    public ReturnCashRecording returnCashRecording(CashMovementService movements) { return new ReturnCashRecordingService(movements); }
}
