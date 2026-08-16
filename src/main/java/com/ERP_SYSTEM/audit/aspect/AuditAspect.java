package com.ERP_SYSTEM.audit.aspect;


import com.ERP_SYSTEM.audit.annotation.Auditable;
import com.ERP_SYSTEM.audit.entity.AuditLog;
import com.ERP_SYSTEM.audit.repository.AuditLogRepository;
import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class AuditAspect {

    private final EntityManager entityManager;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer =
            new DefaultParameterNameDiscoverer();


    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        UUID entityId = resolveEntityIdBeforeProceed(joinPoint, auditable);


        Object entityBeforeProceed = entityId != null
                ? entityManager.find(auditable.entityClass(), entityId)
                : null;

        Map<String, Object> oldValueSnapshot = entityBeforeProceed != null
                ? captureLoadedState(entityBeforeProceed)
                : null;


        Object result = joinPoint.proceed();


        UUID finalEntityId = entityId != null
                ? entityId
                : resolveEntityIdFromResult(result);

        Object entityAfterProceed = entityManager.find(auditable.entityClass(), finalEntityId);

        Map<String, Object> newValueSnapshot = captureCurrentState(entityAfterProceed);


        saveAuditLog(auditable, finalEntityId, oldValueSnapshot, newValueSnapshot);

        return result;
    }


    private UUID resolveEntityIdBeforeProceed(ProceedingJoinPoint joinPoint, Auditable auditable) {

        if (auditable.idExpression().startsWith("#result")) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                null, method, args, parameterNameDiscoverer);

        Expression expression = expressionParser.parseExpression(auditable.idExpression());
        Object value = expression.getValue(context);
        return value != null ? (UUID) value : null;
    }

    private UUID resolveEntityIdFromResult(Object result) {
        try {

            Method idAccessor = result.getClass().getMethod("id");
            return (UUID) idAccessor.invoke(result);
        } catch (Exception ex) {
            log.warn("Không thể trích xuất id từ kết quả trả về để ghi Audit Log: {}",
                    ex.getMessage());
            return null;
        }
    }


    private Map<String, Object> captureLoadedState(Object entity) {
        // Unwrap THẲNG xuống SessionImplementor - KHÔNG qua Session rồi
        // cast thủ công. Vì Spring's Shared EntityManager Proxy implement
        // luôn interface Session (do Session extends EntityManager ở
        // Hibernate 6), nếu unwrap(Session.class) trước, Spring sẽ trả về
        // CHÍNH PROXY (vì proxy "is-a" Session) thay vì Session thật -
        // proxy đó không implement SessionImplementor -> ClassCastException.
        // Unwrap thẳng SessionImplementor buộc Spring phải delegate xuống
        // SessionImpl THẬT của Hibernate (vì proxy không "is-a"
        // SessionImplementor), tránh hoàn toàn vấn đề trên.
        SessionImplementor session = entityManager.unwrap(SessionImplementor.class);
        PersistenceContext persistenceContext = session.getPersistenceContext();
        EntityEntry entry = persistenceContext.getEntry(entity);

        if (entry == null || entry.getLoadedState() == null) {
            return new HashMap<>();
        }

        Object[] loadedState = entry.getLoadedState();
        String[] propertyNames = entry.getPersister().getPropertyNames();

        return buildSanitizedMap(propertyNames, loadedState);
    }

    private Map<String, Object> captureCurrentState(Object entity) {
        SessionImplementor session = entityManager.unwrap(SessionImplementor.class);
        PersistenceContext persistenceContext = session.getPersistenceContext();
        EntityEntry entry = persistenceContext.getEntry(entity);

        String[] propertyNames = entry.getPersister().getPropertyNames();
        Object[] currentState = entry.getPersister().getPropertyValues(entity);

        return buildSanitizedMap(propertyNames, currentState);
    }

    private static final java.util.Set<String> SENSITIVE_FIELDS =
            java.util.Set.of("password", "passwordHash", "refreshToken", "otpSecret");

    private Map<String, Object> buildSanitizedMap(String[] propertyNames, Object[] values) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (int i = 0; i < propertyNames.length; i++) {
            if (SENSITIVE_FIELDS.contains(propertyNames[i])) {
                continue;
            }

            Object value = values[i];
            if (value == null) {
                result.put(propertyNames[i], null);
            } else if (value instanceof java.util.Collection) {
                continue;
            } else if (isEntityReference(value)) {
                result.put(propertyNames[i], extractIdFromEntity(value));
            } else {
                result.put(propertyNames[i], value);
            }
        }
        return result;
    }


    private boolean isEntityReference(Object value) {
        for (Field field : value.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return true;
            }
        }

        Class<?> superclass = value.getClass().getSuperclass();
        while (superclass != null) {
            for (Field field : superclass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    return true;
                }
            }
            superclass = superclass.getSuperclass();
        }
        return false;
    }

    private Object extractIdFromEntity(Object entity) {
        try {
            Method getId = entity.getClass().getMethod("getId");
            return getId.invoke(entity);
        } catch (Exception ex) {
            return null;
        }
    }

    private void saveAuditLog(Auditable auditable, UUID entityId,
                              Map<String, Object> oldValue, Map<String, Object> newValue) {
        try {
            AuditLog log = AuditLog.builder()
                    .entityType(auditable.entityType())
                    .entityId(entityId)
                    .action(auditable.action())
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(objectMapper.writeValueAsString(newValue))
                    .performedById(getCurrentUser().getId())
                    .module(auditable.module())
                    .build();

            auditLogRepository.save(log);
        } catch (Exception ex) {

            log.error("Ghi Audit Log thất bại: {}", ex.getMessage(), ex);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa được xác thực");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng: " + username));
    }
}
