package com.internship.tool.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.Instant;
 
@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;
 
    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;
 
    @Column(nullable = false, length = 20)
    private String action;
 
    @Column(nullable = false, length = 255)
    private String actor;
 
    @Column(name = "actor_ip", length = 45)
    private String actorIp;
 
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;
 
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;
 
    @Column(columnDefinition = "jsonb")
    private String diff;
 
    @Column(nullable = false, length = 20)
    private String status = "SUCCESS";
 
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
 
    @Column(name = "request_id", length = 64)
    private String requestId;
 
    @Column(name = "session_id", length = 64)
    private String sessionId;
 
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}