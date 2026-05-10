package com.internship.tool.entity;
 
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
 
import java.time.Instant;
import java.util.Collection;
import java.util.List;
 
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true, length = 100)
    private String username;
 
    @Column(nullable = false, unique = true, length = 255)
    private String email;
 
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;
 
    @Column(nullable = false)
    private Boolean active = true;
 
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
 
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
 
    public enum Role {
        ADMIN, ANALYST, USER
    }
}
 