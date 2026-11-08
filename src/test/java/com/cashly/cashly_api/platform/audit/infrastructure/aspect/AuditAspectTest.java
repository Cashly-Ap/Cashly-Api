package com.cashly.cashly_api.platform.audit.infrastructure.aspect;

import com.cashly.cashly_api.platform.audit.application.ports.in.AuditRecorder;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;
import com.cashly.cashly_api.platform.audit.application.record.model.AuditEntry;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AuditAspectTest {

    private final UUID id = UUID.randomUUID();

    record Result(UUID id, String number) {
    }

    static class SampleUseCase {
        @Audited(module = "sales", entityType = "ORDER", action = "CREATE")
        public Result execute() {
            return new Result(UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"), "ORD-1");
        }

        @Audited(module = "sales", entityType = "ORDER", action = "DELETE")
        public void fail() {
            throw new IllegalStateException("business failure");
        }
    }

    @Test
    void recordsEntityFromResult() {
        AuditRecorder recorder = mock(AuditRecorder.class);
        SampleUseCase proxy = proxy(new SampleUseCase(), recorder);

        proxy.execute();

        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor
                .forClass(AuditEntry.class);
        verify(recorder).record(captor.capture());
        assertThat(captor.getValue().module()).isEqualTo("sales");
        assertThat(captor.getValue().entityType())
                .isEqualTo(AuditEntityType.ORDER);
        assertThat(captor.getValue().action()).isEqualTo(AuditAction.CREATE);
        assertThat(captor.getValue().entityId())
                .isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(captor.getValue().entityLabel()).isEqualTo("ORD-1");
    }

    @Test
    void doesNotRecordWhenBusinessOperationFails() {
        AuditRecorder recorder = mock(AuditRecorder.class);
        SampleUseCase proxy = proxy(new SampleUseCase(), recorder);

        assertThatThrownBy(proxy::fail)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("business failure");
        verifyNoInteractions(recorder);
    }

    private SampleUseCase proxy(SampleUseCase target, AuditRecorder recorder) {
        var factory = new AspectJProxyFactory(target);
        factory.addAspect(new AuditAspect(recorder));
        return factory.getProxy();
    }
}
