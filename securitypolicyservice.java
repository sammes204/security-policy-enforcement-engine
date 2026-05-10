package com.internship.tool.service;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.config.AiServiceClient;
import com.internship.tool.dto.PolicyDtos;
import com.internship.tool.entity.*;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.Map;
 
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecurityPolicyService {
 
    private final SecurityPolicyRepository policyRepo;
    private final UserRepository userRepo;
    private final AuditLogRepository auditRepo;
    private final AiServiceClient aiClient;
    private final ObjectMapper objectMapper;
 
    @Cacheable(value = "policies", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PolicyDtos.Response> getAll(Pageable pageable) {
        return policyRepo.findByIsDeletedFalse(pageable)
            .map(PolicyDtos.Response::from);
    }
 
    @Cacheable(value = "policies", key = "#id")
    @Transactional(readOnly = true)
    public PolicyDtos.Response getById(Long id) {
        return policyRepo.findByIdAndIsDeletedFalse(id)
            .map(PolicyDtos.Response::from)
            .orElseThrow(() -> new ResourceNotFoundException("SecurityPolicy", id));
    }
 
    @Transactional(readOnly = true)
    public Page<PolicyDtos.Response> search(String q, Pageable pageable) {
        return policyRepo.search(q, pageable).map(PolicyDtos.Response::from);
    }
 
    @Transactional(readOnly = true)
    public Page<PolicyDtos.Response> filter(String status, String severity, Pageable pageable) {
        SecurityPolicy.Status s  = status   != null ? SecurityPolicy.Status.valueOf(status)   : null;
        SecurityPolicy.Severity sv = severity != null ? SecurityPolicy.Severity.valueOf(severity) : null;
        return policyRepo.filter(s, sv, pageable).map(PolicyDtos.Response::from);
    }
 
    @CacheEvict(value = {"policies", "stats"}, allEntries = true)
    public PolicyDtos.Response create(PolicyDtos.CreateRequest req) {
        var user = currentUser();
 
        var policy = SecurityPolicy.builder()
            .name(req.getName())
            .description(req.getDescription())
            .category(req.getCategory())
            .severity(req.getSeverity())
            .status(req.getStatus())
            .riskScore(req.getRiskScore())
            .owner(req.getOwner())
            .targetSystems(req.getTargetSystems())
            .enforcementType(req.getEnforcementType())
            .effectiveDate(req.getEffectiveDate())
            .reviewDate(req.getReviewDate())
            .createdBy(user)
            .updatedBy(user)
            .isDeleted(false)
            .build();
 
        var saved = policyRepo.save(policy);
        writeAudit("SecurityPolicy", saved.getId().toString(), "CREATE", null, toJson(saved));
        triggerAiEnrichment(saved);
        return PolicyDtos.Response.from(saved);
    }
 
    @CacheEvict(value = {"policies", "stats"}, allEntries = true)
    public PolicyDtos.Response update(Long id, PolicyDtos.UpdateRequest req) {
        var policy = policyRepo.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("SecurityPolicy", id));
 
        String oldJson = toJson(policy);
 
        if (req.getName()            != null) policy.setName(req.getName());
        if (req.getDescription()     != null) policy.setDescription(req.getDescription());
        if (req.getCategory()        != null) policy.setCategory(req.getCategory());
        if (req.getSeverity()        != null) policy.setSeverity(req.getSeverity());
        if (req.getStatus()          != null) policy.setStatus(req.getStatus());
        if (req.getRiskScore()       != null) policy.setRiskScore(req.getRiskScore());
        if (req.getOwner()           != null) policy.setOwner(req.getOwner());
        if (req.getTargetSystems()   != null) policy.setTargetSystems(req.getTargetSystems());
        if (req.getEnforcementType() != null) policy.setEnforcementType(req.getEnforcementType());
        if (req.getEffectiveDate()   != null) policy.setEffectiveDate(req.getEffectiveDate());
        if (req.getReviewDate()      != null) policy.setReviewDate(req.getReviewDate());
        policy.setUpdatedBy(currentUser());
 
        var saved = policyRepo.save(policy);
        writeAudit("SecurityPolicy", id.toString(), "UPDATE", oldJson, toJson(saved));
        return PolicyDtos.Response.from(saved);
    }
 
    @CacheEvict(value = {"policies", "stats"}, allEntries = true)
    public void delete(Long id) {
        var policy = policyRepo.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("SecurityPolicy", id));
        String oldJson = toJson(policy);
        policy.setIsDeleted(true);
        policy.setUpdatedBy(currentUser());
        policyRepo.save(policy);
        writeAudit("SecurityPolicy", id.toString(), "DELETE", oldJson, null);
    }
 
    @Cacheable("stats")
    @Transactional(readOnly = true)
    public PolicyDtos.StatsResponse getStats() {
        return PolicyDtos.StatsResponse.builder()
            .total(policyRepo.countActive())
            .active(policyRepo.countByStatusActive())
            .critical(policyRepo.countCritical())
            .avgRiskScore(policyRepo.avgRiskScore() != null ? policyRepo.avgRiskScore() : 0.0)
            .build();
    }
 
    @Async
    public void triggerAiEnrichment(SecurityPolicy policy) {
        try {
            var result = aiClient.describe(
                policy.getName(),
                policy.getDescription(),
                policy.getCategory(),
                policy.getSeverity().name()
            );
            if (result != null) {
                String aiDesc = (String) result.getOrDefault("description", "");
                policyRepo.findById(policy.getId()).ifPresent(p -> {
                    p.setAiDescription(aiDesc);
                    policyRepo.save(p);
                });
            }
        } catch (Exception e) {
            log.warn("Async AI enrichment failed for policy {}: {}", policy.getId(), e.getMessage());
        }
    }
 
    // ── Internal helpers ─────────────────────────────────────
 
    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElse(null);
    }
 
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
 
    private void writeAudit(String entityType, String entityId, String action,
                             String oldVal, String newVal) {
        try {
            String actor = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "system";
 
            auditRepo.save(AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actor(actor)
                .oldValue(oldVal)
                .newValue(newVal)
                .status("SUCCESS")
                .build());
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }
}