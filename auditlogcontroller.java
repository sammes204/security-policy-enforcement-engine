package com.internship.tool.controller;
 
import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "Immutable audit trail")
public class AuditLogController {
 
    private final AuditLogRepository auditRepo;
 
    @GetMapping
    @Operation(summary = "Get all audit log entries (paginated)")
    @Cacheable("auditLogs")
    public ResponseEntity<Page<AuditLog>> getAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            auditRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
        );
    }
 
    @GetMapping("/entity/{type}/{id}")
    @Operation(summary = "Get audit history for a specific entity")
    public ResponseEntity<Page<AuditLog>> byEntity(
        @PathVariable String type,
        @PathVariable String id,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            auditRepo.findByEntityTypeAndEntityId(type, id, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
        );
    }
}