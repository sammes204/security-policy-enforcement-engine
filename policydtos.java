package com.internship.tool.dto;
 
import com.internship.tool.entity.SecurityPolicy;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.time.Instant;
import java.time.LocalDate;
 
// ─── Request DTOs ──────────────────────────────────────────────
 
public class PolicyDtos {
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 255)
        private String name;
 
        @Size(max = 5000)
        private String description;
 
        @NotBlank(message = "Category is required")
        @Size(max = 100)
        private String category;
 
        private SecurityPolicy.Severity severity = SecurityPolicy.Severity.MEDIUM;
        private SecurityPolicy.Status status = SecurityPolicy.Status.DRAFT;
 
        @Min(0) @Max(100)
        private Integer riskScore;
 
        private String owner;
        private String targetSystems;
        private SecurityPolicy.EnforcementType enforcementType = SecurityPolicy.EnforcementType.ADVISORY;
        private LocalDate effectiveDate;
        private LocalDate reviewDate;
    }
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        @Size(max = 255)
        private String name;
 
        @Size(max = 5000)
        private String description;
 
        @Size(max = 100)
        private String category;
 
        private SecurityPolicy.Severity severity;
        private SecurityPolicy.Status status;
 
        @Min(0) @Max(100)
        private Integer riskScore;
 
        private String owner;
        private String targetSystems;
        private SecurityPolicy.EnforcementType enforcementType;
        private LocalDate effectiveDate;
        private LocalDate reviewDate;
    }
 
    // ─── Response DTO ──────────────────────────────────────────
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String category;
        private String severity;
        private String status;
        private Integer riskScore;
        private String owner;
        private String targetSystems;
        private String enforcementType;
        private String aiDescription;
        private String aiRecommendations;
        private LocalDate effectiveDate;
        private LocalDate reviewDate;
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
 
        public static Response from(SecurityPolicy p) {
            return Response.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .category(p.getCategory())
                .severity(p.getSeverity().name())
                .status(p.getStatus().name())
                .riskScore(p.getRiskScore())
                .owner(p.getOwner())
                .targetSystems(p.getTargetSystems())
                .enforcementType(p.getEnforcementType().name())
                .aiDescription(p.getAiDescription())
                .aiRecommendations(p.getAiRecommendations())
                .effectiveDate(p.getEffectiveDate())
                .reviewDate(p.getReviewDate())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .createdBy(p.getCreatedBy() != null ? p.getCreatedBy().getUsername() : null)
                .build();
        }
    }
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatsResponse {
        private long total;
        private long active;
        private long critical;
        private double avgRiskScore;
    }
}