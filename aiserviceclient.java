package com.internship.tool.config;
 
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
 
import java.time.Duration;
import java.util.Map;
 
@Component
@Slf4j
public class AiServiceClient {
 
    private final RestTemplate restTemplate;
    private final String aiServiceUrl;
 
    public AiServiceClient(
        RestTemplateBuilder builder,
        @Value("${ai.service.url}") String aiServiceUrl,
        @Value("${ai.service.timeout-ms:10000}") int timeoutMs
    ) {
        this.restTemplate = builder
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .readTimeout(Duration.ofMillis(timeoutMs))
            .build();
        this.aiServiceUrl = aiServiceUrl;
    }
 
    @SuppressWarnings("unchecked")
    public Map<String, Object> describe(String name, String description, String category, String severity) {
        try {
            var body = Map.of(
                "name", name,
                "description", description != null ? description : "",
                "category", category,
                "severity", severity
            );
            var response = restTemplate.postForEntity(
                aiServiceUrl + "/describe", body, Map.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            log.error("AI /describe call failed: {}", e.getMessage());
            return null;
        }
    }
 
    @SuppressWarnings("unchecked")
    public Map<String, Object> recommend(String name, String description, String severity) {
        try {
            var body = Map.of(
                "name", name,
                "description", description != null ? description : "",
                "severity", severity
            );
            var response = restTemplate.postForEntity(
                aiServiceUrl + "/recommend", body, Map.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            log.error("AI /recommend call failed: {}", e.getMessage());
            return null;
        }
    }
 
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateReport(Long policyId, String name, String description,
                                               String category, String severity, String status) {
        try {
            var body = Map.of(
                "policy_id", policyId,
                "name", name,
                "description", description != null ? description : "",
                "category", category,
                "severity", severity,
                "status", status
            );
            var response = restTemplate.postForEntity(
                aiServiceUrl + "/generate-report", body, Map.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            log.error("AI /generate-report call failed: {}", e.getMessage());
            return null;
        }
    }
 
    public boolean isHealthy() {
        try {
            var response = restTemplate.getForEntity(aiServiceUrl + "/health", Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}