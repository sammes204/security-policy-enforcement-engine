package com.internship.tool.repository;
 
import com.internship.tool.entity.SecurityPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
@Repository
public interface SecurityPolicyRepository extends JpaRepository<SecurityPolicy, Long> {
 
    Page<SecurityPolicy> findByIsDeletedFalse(Pageable pageable);
 
    Optional<SecurityPolicy> findByIdAndIsDeletedFalse(Long id);
 
    @Query("""
        SELECT p FROM SecurityPolicy p
        WHERE p.isDeleted = false
          AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.category) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """)
    Page<SecurityPolicy> search(@Param("q") String query, Pageable pageable);
 
    @Query("""
        SELECT p FROM SecurityPolicy p
        WHERE p.isDeleted = false
          AND (:status IS NULL OR p.status = :status)
          AND (:severity IS NULL OR p.severity = :severity)
        """)
    Page<SecurityPolicy> filter(
        @Param("status") SecurityPolicy.Status status,
        @Param("severity") SecurityPolicy.Severity severity,
        Pageable pageable
    );
 
    @Query("SELECT COUNT(p) FROM SecurityPolicy p WHERE p.isDeleted = false")
    long countActive();
 
    @Query("SELECT COUNT(p) FROM SecurityPolicy p WHERE p.isDeleted = false AND p.status = 'ACTIVE'")
    long countByStatusActive();
 
    @Query("SELECT COUNT(p) FROM SecurityPolicy p WHERE p.isDeleted = false AND p.severity = 'CRITICAL'")
    long countCritical();
 
    @Query("SELECT AVG(p.riskScore) FROM SecurityPolicy p WHERE p.isDeleted = false AND p.riskScore IS NOT NULL")
    Double avgRiskScore();
}
 