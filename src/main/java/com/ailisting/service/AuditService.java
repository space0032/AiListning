package com.ailisting.service;

import com.ailisting.model.entity.AuditLog;
import com.ailisting.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    @Async
    public void log(String action, String entityType, Long entityId, String method,
                    String endpoint, Long userId, String username, String ipAddress,
                    String userAgent, Integer statusCode, boolean success,
                    String errorMessage, Long durationMs) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .method(method)
                .endpoint(endpoint)
                .userId(userId)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .statusCode(statusCode)
                .success(success)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .build();
        log(auditLog);
    }

    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }

    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByDateRange(start, end, pageable);
    }

    public List<Object[]> getActionStats() {
        return auditLogRepository.countByAction();
    }

    public List<Object[]> getEntityTypeStats() {
        return auditLogRepository.countByEntityType();
    }

    public long getFailedCount() {
        return auditLogRepository.countBySuccessFalse();
    }
}
