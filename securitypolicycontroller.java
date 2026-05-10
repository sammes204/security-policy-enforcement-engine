package com.internship.tool.controller;
 
import com.internship.tool.config.AiServiceClient;
import com.internship.tool.dto.PolicyDtos;
import com.internship.tool.entity.SecurityPolicy;
import com.internship.tool.repository.SecurityPolicyRepository;
import com.internship.tool.service.SecurityPolicyService;
import com.opencsv.CSVWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
import java.io.IOException;
import java.util.Map;
 
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Security Policies", description = "CRUD and AI operations for security policies")
@SecurityRequirement(name = "bearerAuth")
public class SecurityPolicyController {
 
    private final SecurityPolicyService service;
    private final SecurityPolicyRepository repo;
    private final AiServiceClient aiClient;
 
    @GetMapping
    @Operation(summary = "List all policies (paginated)")
    public ResponseEntity<Page<PolicyDtos.Response>> getAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sort,
        @RequestParam(defaultValue = "desc") String dir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        return ResponseEntity.ok(service.getAll(pageable));
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<PolicyDtos.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
 
    @GetMapping("/search")
    @Operation(summary = "Full-text search policies")
    public ResponseEntity<Page<PolicyDtos.Response>> search(
        @RequestParam String q,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.search(q, PageRequest.of(page, size)));
    }
 
    @GetMapping("/filter")
    @Operation(summary = "Filter policies by status and/or severity")
    public ResponseEntity<Page<PolicyDtos.Response>> filter(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String severity,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.filter(status, severity, PageRequest.of(page, size)));
    }
 
    @GetMapping("/stats")
    @Operation(summary = "Dashboard KPI stats")
    public ResponseEntity<PolicyDtos.StatsResponse> stats() {
        return ResponseEntity.ok(service.getStats());
    }
 
    @PostMapping
    @Operation(summary = "Create a new security policy")
    public ResponseEntity<PolicyDtos.Response> create(@Valid @RequestBody PolicyDtos.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }
 
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing policy")
    public ResponseEntity<PolicyDtos.Response> update(
        @PathVariable Long id,
        @Valid @RequestBody PolicyDtos.UpdateRequest req
    ) {
        return ResponseEntity.ok(service.update(id, req));
    }
 
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a policy")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
 
    @PostMapping("/{id}/ai-recommend")
    @Operation(summary = "Get AI recommendations for a policy")
    public ResponseEntity<Map<String, Object>> recommend(@PathVariable Long id) {
        var policy = repo.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new com.internship.tool.exception.ResourceNotFoundException("SecurityPolicy", id));
        var result = aiClient.recommend(policy.getName(), policy.getDescription(), policy.getSeverity().name());
        if (result == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "AI service unavailable", "is_fallback", true));
        }
        // persist recommendations
        policy.setAiRecommendations(result.toString());
        repo.save(policy);
        return ResponseEntity.ok(result);
    }
 
    @PostMapping("/{id}/ai-report")
    @Operation(summary = "Generate AI compliance report for a policy")
    public ResponseEntity<Map<String, Object>> generateReport(@PathVariable Long id) {
        var policy = repo.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new com.internship.tool.exception.ResourceNotFoundException("SecurityPolicy", id));
        var result = aiClient.generateReport(
            policy.getId(), policy.getName(), policy.getDescription(),
            policy.getCategory(), policy.getSeverity().name(), policy.getStatus().name()
        );
        if (result == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "AI service unavailable", "is_fallback", true));
        }
        return ResponseEntity.ok(result);
    }
 
    @GetMapping("/export")
    @Operation(summary = "Export all policies as CSV")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=policies.csv");
        try (var writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[]{"ID","Name","Category","Severity","Status","Risk Score","Owner","Created At"});
            repo.findByIsDeletedFalse(Pageable.unpaged()).forEach(p ->
                writer.writeNext(new String[]{
                    String.valueOf(p.getId()), p.getName(), p.getCategory(),
                    p.getSeverity().name(), p.getStatus().name(),
                    p.getRiskScore() != null ? String.valueOf(p.getRiskScore()) : "",
                    p.getOwner() != null ? p.getOwner() : "",
                    p.getCreatedAt().toString()
                })
            );
        }
    }
}