package com.canbankx.customer.controller;

import com.canbankx.customer.domain.AuditLog;
import com.canbankx.customer.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Immutable audit trail for regulatory compliance (UC-07)")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs for a specific entity")
    public List<AuditLog> getAuditLogs(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        return auditService.getAuditLogs(entityType, entityId);
    }

    @GetMapping("/{entityType}")
    @Operation(summary = "Get all audit logs by entity type")
    public List<AuditLog> getAuditLogsByType(@PathVariable String entityType) {
        return auditService.getAuditLogsByType(entityType);
    }
}
