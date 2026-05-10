package com.internship.tool.entity;
 
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
 
import java.time.Instant;
import java.time.LocalDate;
 
@Entity
@Table(name = "security_policies")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SecurityPolicy {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 255)
    private String name;
 
    @Column(columnDefinition = "TEXT")
    private String description;
 
    @Column(nullable = false, length = 100)
    private String category;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity = Severity.MEDIUM;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;
 
    @Column(name = "risk_score")
    private Integer riskScore;
 
    @Column(length = 255)
    private String owner;
 
    @Column(name = "target_systems", columnDefinition = "TEXT")
    private String targetSystems;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "enforcement_type", nullable = false, length = 50)
    private EnforcementType enforcementType = EnforcementType.ADVISORY;
 
    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;
 
    @Column(name = "ai_recommendations", columnDefinition = "TEXT")
    private String aiRecommendations;
 
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
 
    @Column(name = "effective_date")
    private LocalDate effectiveDate;
 
    @Column(name = "review_date")
    private LocalDate reviewDate;
 
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
 
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
 
    public enum Severity { CRITICAL, HIGH, MEDIUM, LOW, INFO }
    public enum Status   { DRAFT, ACTIVE, INACTIVE, ARCHIVED }
    public enum EnforcementType { MANDATORY, ADVISORY, AUTOMATED }
}