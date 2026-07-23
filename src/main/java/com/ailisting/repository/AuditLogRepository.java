package com.ailisting.repository;

import com.ailisting.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<AuditLog> findBySuccessOrderByCreatedAtDesc(Boolean success, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   Pageable pageable);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countByAction();

    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a WHERE a.entityType IS NOT NULL GROUP BY a.entityType ORDER BY COUNT(a) DESC")
    List<Object[]> countByEntityType();

    long countBySuccessFalse();
}
