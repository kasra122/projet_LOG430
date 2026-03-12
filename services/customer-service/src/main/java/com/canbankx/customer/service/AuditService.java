package com.canbankx.customer.service;

import com.canbankx.customer.domain.AuditLog;
import com.canbankx.customer.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public void logAction(String entityType, String entityId, String action, String details, String performedBy) {
        AuditLog entry = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .performedBy(performedBy)
                .createdAt(Instant.now())
                .build();

        auditLogRepository.save(entry);
        log.info("AUDIT: [{}] {} on {}:{} by {}", action, details, entityType, entityId, performedBy);
    }

    public List<AuditLog> getAuditLogs(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsByType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType);
    }
}
