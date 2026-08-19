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
import java.util.*;

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

        private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

        private static final Set<String> SENSITIVE_FIELDS = Set.of(
                        "password",
                        "passwordHash",
                        "refreshToken",
                        "otpSecret");

        /**
         * Main audit interceptor.
         */
        @Around("@annotation(auditable)")
        public Object audit(
                        ProceedingJoinPoint joinPoint,
                        Auditable auditable) throws Throwable {

                /*
                 * ============================================================
                 * 1. Resolve entity ID BEFORE business method
                 * ============================================================
                 */
                UUID entityId = resolveEntityIdBeforeProceed(
                                joinPoint,
                                auditable);

                /*
                 * ============================================================
                 * 2. Capture OLD state
                 * ============================================================
                 */
                Map<String, Object> oldValueSnapshot = null;

                if (entityId != null) {

                        Object entityBeforeProceed = entityManager.find(
                                        auditable.entityClass(),
                                        entityId);

                        if (entityBeforeProceed != null) {

                                oldValueSnapshot = captureLoadedState(entityBeforeProceed);

                        } else {

                                log.debug(
                                                "Audit: Entity {} with id {} does not exist before operation",
                                                auditable.entityClass().getSimpleName(),
                                                entityId);
                        }
                }

                /*
                 * ============================================================
                 * 3. Execute business method
                 * ============================================================
                 */
                Object result = joinPoint.proceed();

                /*
                 * ============================================================
                 * 4. Resolve final entity ID
                 * ============================================================
                 */
                UUID finalEntityId = entityId != null
                                ? entityId
                                : resolveEntityIdFromResult(result);

                /*
                 * ============================================================
                 * 5. Capture NEW state
                 * ============================================================
                 */
                Map<String, Object> newValueSnapshot = null;

                if (finalEntityId != null) {

                        Object entityAfterProceed = entityManager.find(
                                        auditable.entityClass(),
                                        finalEntityId);

                        if (entityAfterProceed != null) {

                                newValueSnapshot = captureCurrentState(entityAfterProceed);

                        } else {

                                log.debug(
                                                "Audit: Entity {} with id {} does not exist after operation",
                                                auditable.entityClass().getSimpleName(),
                                                finalEntityId);
                        }
                }

                /*
                 * ============================================================
                 * 6. Save audit
                 * ============================================================
                 */
                saveAuditLog(
                                auditable,
                                finalEntityId,
                                oldValueSnapshot,
                                newValueSnapshot);

                return result;
        }

        /**
         * Resolve entity ID before business method executes.
         */
        private UUID resolveEntityIdBeforeProceed(
                        ProceedingJoinPoint joinPoint,
                        Auditable auditable) {

                if (auditable.idExpression().startsWith("#result")) {
                        return null;
                }

                MethodSignature signature = (MethodSignature) joinPoint.getSignature();

                Method method = signature.getMethod();

                Object[] args = joinPoint.getArgs();

                MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                                null,
                                method,
                                args,
                                parameterNameDiscoverer);

                Expression expression = expressionParser.parseExpression(
                                auditable.idExpression());

                Object value = expression.getValue(context);

                if (value == null) {
                        return null;
                }

                if (value instanceof UUID uuid) {
                        return uuid;
                }

                try {
                        return UUID.fromString(value.toString());
                } catch (Exception ex) {

                        log.warn(
                                        "Audit: Cannot convert entity ID [{}] to UUID",
                                        value);

                        return null;
                }
        }

        /**
         * Resolve entity ID from method result.
         * <p>
         * Supports record DTO:
         * <p>
         * public record UserResponse(UUID id, ...) {}
         */
        private UUID resolveEntityIdFromResult(Object result) {

                if (result == null) {
                        return null;
                }

                try {

                        Method idAccessor = result.getClass().getMethod("id");

                        Object id = idAccessor.invoke(result);

                        if (id instanceof UUID uuid) {
                                return uuid;
                        }

                        if (id != null) {
                                return UUID.fromString(id.toString());
                        }

                } catch (NoSuchMethodException ignored) {

                        /*
                         * Some DTOs use getId() instead of id().
                         */

                        try {

                                Method getIdAccessor = result.getClass().getMethod("getId");

                                Object id = getIdAccessor.invoke(result);

                                if (id instanceof UUID uuid) {
                                        return uuid;
                                }

                                if (id != null) {
                                        return UUID.fromString(id.toString());
                                }

                        } catch (Exception ex) {

                                log.warn(
                                                "Không thể trích xuất id từ kết quả trả về: {}",
                                                ex.getMessage());
                        }

                } catch (Exception ex) {

                        log.warn(
                                        "Không thể trích xuất id từ kết quả trả về: {}",
                                        ex.getMessage());
                }

                return null;
        }

        /**
         * Capture OLD state.
         * <p>
         * Hibernate's EntityEntry contains the original loaded state.
         */
        private Map<String, Object> captureLoadedState(Object entity) {

                if (entity == null) {
                        return null;
                }

                try {

                        SessionImplementor session = entityManager.unwrap(
                                        SessionImplementor.class);

                        PersistenceContext persistenceContext = session.getPersistenceContext();

                        EntityEntry entry = persistenceContext.getEntry(entity);

                        /*
                         * IMPORTANT:
                         *
                         * getEntry(entity) may return null.
                         *
                         * Never call:
                         *
                         * entry.getPersister()
                         *
                         * before checking this.
                         */
                        if (entry == null) {

                                log.warn(
                                                "Audit: EntityEntry is null while capturing OLD state. Entity={}",
                                                entity.getClass().getSimpleName());

                                return captureStateUsingPersister(
                                                session,
                                                entity);
                        }

                        Object[] loadedState = entry.getLoadedState();

                        if (loadedState == null) {

                                log.warn(
                                                "Audit: loadedState is null for entity {}",
                                                entity.getClass().getSimpleName());

                                return captureStateUsingPersister(
                                                session,
                                                entity);
                        }

                        org.hibernate.persister.entity.EntityPersister persister = entry.getPersister();

                        String[] propertyNames = persister.getPropertyNames();

                        return buildSanitizedMap(
                                        propertyNames,
                                        loadedState);

                } catch (Exception ex) {

                        log.error(
                                        "Audit: Failed to capture OLD state for {}: {}",
                                        entity.getClass().getSimpleName(),
                                        ex.getMessage(),
                                        ex);

                        return new LinkedHashMap<>();
                }
        }

        /**
         * Capture NEW/current state.
         * <p>
         * This method no longer assumes EntityEntry is always available.
         */
        private Map<String, Object> captureCurrentState(Object entity) {

                if (entity == null) {
                        return null;
                }

                try {

                        SessionImplementor session = entityManager.unwrap(
                                        SessionImplementor.class);

                        PersistenceContext persistenceContext = session.getPersistenceContext();

                        EntityEntry entry = persistenceContext.getEntry(entity);

                        /*
                         * EntityEntry exists.
                         */
                        if (entry != null) {

                                org.hibernate.persister.entity.EntityPersister persister = entry.getPersister();

                                String[] propertyNames = persister.getPropertyNames();

                                Object[] currentState = persister.getPropertyValues(entity);

                                return buildSanitizedMap(
                                                propertyNames,
                                                currentState);
                        }

                        /*
                         * EntityEntry does NOT exist.
                         *
                         * Do NOT call entry.getPersister().
                         *
                         * Get the persister from Hibernate SessionFactory instead.
                         */
                        log.debug(
                                        "Audit: EntityEntry is null while capturing NEW state. Using EntityPersister fallback. Entity={}",
                                        entity.getClass().getSimpleName());

                        return captureStateUsingPersister(
                                        session,
                                        entity);

                } catch (Exception ex) {

                        log.error(
                                        "Audit: Failed to capture NEW state for {}: {}",
                                        entity.getClass().getSimpleName(),
                                        ex.getMessage(),
                                        ex);

                        return new LinkedHashMap<>();
                }
        }

        /**
         * Fallback when EntityEntry is unavailable.
         * <p>
         * Hibernate's SessionFactory can resolve the EntityPersister
         * directly from the entity's actual class.
         */
        private Map<String, Object> captureStateUsingPersister(
                        SessionImplementor session,
                        Object entity) {

                try {

                        org.hibernate.persister.entity.EntityPersister persister = session.getEntityPersister(
                                        null,
                                        entity);

                        if (persister == null) {

                                log.warn(
                                                "Audit: Cannot resolve EntityPersister for {}",
                                                entity.getClass().getName());

                                return new LinkedHashMap<>();
                        }

                        String[] propertyNames = persister.getPropertyNames();

                        Object[] currentState = persister.getPropertyValues(entity);

                        return buildSanitizedMap(
                                        propertyNames,
                                        currentState);

                } catch (Exception ex) {

                        log.error(
                                        "Audit: Failed to resolve EntityPersister for {}: {}",
                                        entity.getClass().getName(),
                                        ex.getMessage(),
                                        ex);

                        return new LinkedHashMap<>();
                }
        }

        /**
         * Remove sensitive fields and convert entity references
         * into their IDs.
         */
        private Map<String, Object> buildSanitizedMap(
                        String[] propertyNames,
                        Object[] values) {

                Map<String, Object> result = new LinkedHashMap<>();

                if (propertyNames == null || values == null) {
                        return result;
                }

                int length = Math.min(
                                propertyNames.length,
                                values.length);

                for (int i = 0; i < length; i++) {

                        String propertyName = propertyNames[i];

                        if (propertyName == null) {
                                continue;
                        }

                        /*
                         * Never write sensitive data to audit log.
                         */
                        if (SENSITIVE_FIELDS.contains(propertyName)) {
                                continue;
                        }

                        Object value = values[i];

                        if (value == null) {

                                result.put(
                                                propertyName,
                                                null);

                        } else if (value instanceof Collection<?>) {

                                /*
                                 * Avoid huge recursive collections.
                                 */
                                continue;

                        } else if (isEntityReference(value)) {

                                result.put(
                                                propertyName,
                                                extractIdFromEntity(value));

                        } else {

                                result.put(
                                                propertyName,
                                                value);
                        }
                }

                return result;
        }

        /**
         * Check whether an object is a JPA entity.
         */
        private boolean isEntityReference(Object value) {

                if (value == null) {
                        return false;
                }

                Class<?> clazz = value.getClass();

                /*
                 * Hibernate proxy class may be generated dynamically.
                 * Walk through the inheritance hierarchy.
                 */
                while (clazz != null && clazz != Object.class) {

                        for (Field field : clazz.getDeclaredFields()) {

                                if (field.isAnnotationPresent(Id.class)) {
                                        return true;
                                }
                        }

                        clazz = clazz.getSuperclass();
                }

                return false;
        }

        /**
         * Extract entity ID.
         */
        private Object extractIdFromEntity(Object entity) {

                if (entity == null) {
                        return null;
                }

                /*
                 * Try getId()
                 */
                try {

                        Method getId = entity.getClass().getMethod("getId");

                        return getId.invoke(entity);

                } catch (Exception ignored) {
                }

                /*
                 * Try record-style id()
                 */
                try {

                        Method id = entity.getClass().getMethod("id");

                        return id.invoke(entity);

                } catch (Exception ignored) {
                }

                return null;
        }

        /**
         * Save AuditLog.
         * <p>
         * Audit failure must NEVER break the business API.
         */
        private void saveAuditLog(
                        Auditable auditable,
                        UUID entityId,
                        Map<String, Object> oldValue,
                        Map<String, Object> newValue) {

                try {

                        User currentUser = getCurrentUser();

                        AuditLog auditLog = AuditLog.builder()
                                        .entityType(
                                                        auditable.entityType())
                                        .entityId(entityId)
                                        .action(
                                                        auditable.action())
                                        .oldValue(
                                                        oldValue != null
                                                                        ? objectMapper.writeValueAsString(
                                                                                        oldValue)
                                                                        : null)
                                        .newValue(
                                                        newValue != null
                                                                        ? objectMapper.writeValueAsString(
                                                                                        newValue)
                                                                        : null)
                                        .performedById(
                                                        currentUser.getId())
                                        .module(
                                                        auditable.module())
                                        .build();

                        auditLogRepository.save(auditLog);

                } catch (Exception ex) {

                        /*
                         * Audit should not make the actual business operation fail.
                         */
                        log.error(
                                        "Ghi Audit Log thất bại: {}",
                                        ex.getMessage(),
                                        ex);
                }
        }

        /**
         * Resolve authenticated user.
         */
        private User getCurrentUser() {

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (authentication == null
                                || !authentication.isAuthenticated()) {

                        throw new IllegalStateException(
                                        "Người dùng chưa được xác thực");
                }

                String username = authentication.getName();

                return userRepository
                                .findByUsername(username)
                                .orElseThrow(
                                                () -> new IllegalStateException(
                                                                "Không tìm thấy người dùng: "
                                                                                + username));
        }
}