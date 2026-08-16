package com.ERP_SYSTEM.audit.controller;

import com.ERP_SYSTEM.audit.dto.AuditLogResponse;
import com.ERP_SYSTEM.audit.service.AuditLogService;
import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping("/by-entity")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByEntity(
            @RequestParam String entityType,
            @RequestParam UUID entityId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                auditLogService.getByEntity(entityType, entityId, pageable)));
    }

    @GetMapping("/by-user/{performedById}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByUser(
            @PathVariable UUID performedById, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                auditLogService.getByUser(performedById, pageable)));
    }

    @GetMapping("/by-module/{module}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByModule(
            @PathVariable SourceModule module, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                auditLogService.getByModule(module, pageable)));
    }
}
