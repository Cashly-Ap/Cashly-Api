package com.cashly.cashly_api.platform.audit.infrastructure.config;

import com.cashly.cashly_api.platform.audit.application.mappers.AuditMapper;
import com.cashly.cashly_api.platform.audit.application.ports.in.AuditRecorder;
import com.cashly.cashly_api.platform.audit.application.ports.out.AuditActorGateway;
import com.cashly.cashly_api.platform.audit.application.ports.out.AuditQueryPort;
import com.cashly.cashly_api.platform.audit.application.get_events.use_case.GetAuditEventsUseCase;
import com.cashly.cashly_api.platform.audit.application.get_filter_options.use_case.GetAuditFilterOptionsUseCase;
import com.cashly.cashly_api.platform.audit.application.record.use_case.RecordAuditUseCase;
import com.cashly.cashly_api.platform.audit.domain.repository.AuditEventRepository;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.mappers.AuditJpaMapper;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import com.cashly.cashly_api.platform.company.domain.entity.Company;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AuditConfig {

    @Bean
    public Clock auditClock() {
        return Clock.systemUTC();
    }

    @Bean
    public AuditMapper auditMapper() {
        return new AuditMapper();
    }

    @Bean
    public AuditJpaMapper auditJpaMapper() {
        return new AuditJpaMapper();
    }

    @Bean
    public AuditRecorder auditRecorder(AuditEventRepository events,
            AuditActorGateway actors, CurrentActor currentActor, Clock clock,
            @Value("${cashly.audit.fail-closed:true}") boolean failClosed) {
        return new RecordAuditUseCase(events, actors, currentActor, clock,
                Company.INSTALLATION_ID, failClosed);
    }

    @Bean
    public GetAuditFilterOptionsUseCase getAuditFilterOptionsUseCase(
            AuditQueryPort query, AuditMapper mapper) {
        return new GetAuditFilterOptionsUseCase(query, mapper);
    }

    @Bean
    public GetAuditEventsUseCase getAuditEventsUseCase(AuditQueryPort query,
            AuditMapper mapper,
            GetAuditFilterOptionsUseCase filterOptions) {
        return new GetAuditEventsUseCase(query, mapper, filterOptions);
    }
}
