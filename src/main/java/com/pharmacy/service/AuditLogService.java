package com.pharmacy.service;

import com.pharmacy.entity.AuditLog;
import com.pharmacy.entity.User;
import com.pharmacy.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Transactional
    public void log(String action, String entityType, Long entityId, String details, User user) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .performedBy(user)
                .build();
        auditLogRepository.save(log);
    }
    
    public List<AuditLog> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return auditLogRepository.findRecentLogs(pageable);
    }
    
    public List<AuditLog> getEntityLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }
}
