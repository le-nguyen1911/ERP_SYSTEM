package com.ERP_SYSTEM.audit.annotation;

import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    Class<?> entityClass();

    String entityType();

    AuditAction action();

    SourceModule module();

    String idExpression() default "#id";
}
