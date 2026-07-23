package com.ailisting.repository;

import com.ailisting.model.entity.AiGenerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiGenerationLogRepository extends JpaRepository<AiGenerationLog, Long> {

    Page<AiGenerationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AiGenerationLog> findByUserIdAndModelUsedOrderByCreatedAtDesc(
            Long userId, String modelUsed, Pageable pageable);

    @Query("SELECT l FROM AiGenerationLog l WHERE l.user.id = :userId AND l.createdAt BETWEEN :start AND :end ORDER BY l.createdAt DESC")
    Page<AiGenerationLog> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT COUNT(l) FROM AiGenerationLog l WHERE l.user.id = :userId AND l.createdAt >= :since")
    long countByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(l) FROM AiGenerationLog l WHERE l.user.id = :userId AND l.status = 'SUCCESS' AND l.createdAt >= :since")
    long countSuccessfulByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(l.totalTokens), 0) FROM AiGenerationLog l WHERE l.user.id = :userId AND l.createdAt >= :since")
    long sumTokensByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT l.modelUsed, COUNT(l) FROM AiGenerationLog l WHERE l.user.id = :userId GROUP BY l.modelUsed")
    List<Object[]> countByModelForUser(@Param("userId") Long userId);

    // ===========================
    // Global queries (admin)
    // ===========================

    @Query("SELECT COUNT(l) FROM AiGenerationLog l WHERE l.createdAt >= :since")
    long countAllSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(l) FROM AiGenerationLog l WHERE l.status = 'SUCCESS' AND l.createdAt >= :since")
    long countSuccessfulAllSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(l.totalTokens), 0) FROM AiGenerationLog l WHERE l.createdAt >= :since")
    long sumTokensAllSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(AVG(l.generationTimeMs), 0) FROM AiGenerationLog l WHERE l.status = 'SUCCESS'")
    double avgGenerationTimeAll();

    @Query("SELECT l.platform, COUNT(l) FROM AiGenerationLog l WHERE l.platform IS NOT NULL GROUP BY l.platform")
    List<Object[]> countByPlatformAll();

    @Query("SELECT FUNCTION('DATE', l.createdAt) as day, COUNT(l) FROM AiGenerationLog l WHERE l.createdAt >= :since GROUP BY FUNCTION('DATE', l.createdAt) ORDER BY day")
    List<Object[]> countByDaySince(@Param("since") LocalDateTime since);
}