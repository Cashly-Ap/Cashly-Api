package com.cashly.cashly_api.platform.audit.infrastructure.aspect;

import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;
import com.cashly.cashly_api.platform.audit.application.ports.in.AuditRecorder;
import com.cashly.cashly_api.platform.audit.application.record.model.AuditEntry;
import com.cashly.cashly_api.platform.audit.application.support.services.AuditValueExtractor;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class AuditAspect {

    private final AuditRecorder recorder;

    public AuditAspect(AuditRecorder recorder) {
        this.recorder = recorder;
    }

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint joinPoint, Audited audited)
            throws Throwable {
        Object result = joinPoint.proceed();
        Object[] arguments = joinPoint.getArgs();
        Object source = result != null
                ? result
                : (arguments.length == 0 ? null : arguments[0]);
        UUID entityId = AuditValueExtractor.uuid(source);
        if (entityId == null) {
            entityId = AuditValueExtractor.firstUuid(arguments);
        }
        if (entityId == null) {
            entityId = AuditValueExtractor.fallbackId(
                    joinPoint.getSignature().toLongString()
                            + Arrays.toString(arguments));
        }
        String entityLabel = AuditValueExtractor.label(source);
        if (entityLabel == null || entityLabel.isBlank()) {
            entityLabel = AuditValueExtractor.firstLabel(arguments);
        }
        if (entityLabel == null || entityLabel.isBlank()) {
            entityLabel = entityId.toString();
        }
        recorder.record(AuditEntry.of(audited.module(),
                AuditEntityType.valueOf(audited.entityType()), entityId,
                entityLabel, AuditAction.valueOf(audited.action())));
        return result;
    }
}
