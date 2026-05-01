package com.securitypolicy.ai;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class AiServiceClient {
    private final WebClient webClient;

    public AiServiceClient(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(Objects.requireNonNullElse(baseUrl, "http://localhost:5000"))
                .build();
    }

    public Map<String, Object> describe(String input) {
        return post("/describe", input);
    }

    public Map<String, Object> recommend(String input) {
        return post("/recommend", input);
    }

    public Map<String, Object> generateReport(String input) {
        return post("/generate-report", input);
    }

    public Map<String, Object> scan(String input) {
        return post("/scan", input);
    }

    public Map<String, Object> report(String input) {
        return post("/report", input);
    }

    public Map<String, Object> askAi(String question) {
        return post("/ask-ai", "question", question);
    }

    public Map<String, Object> health() {
        Map<String, Object> response = webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(error -> Mono.just(Map.of(
                        "success", false,
                        "error", Map.of(
                                "message", "AI service unavailable",
                                "code", 502
                        )
                )))
                .block();

        if (response == null) {
            return Map.of(
                    "success", false,
                    "error", Map.of(
                            "message", "AI service returned an empty response",
                            "code", 502
                    )
            );
        }
        return response;
    }

    private Map<String, Object> post(String path, String input) {
        return post(path, "input", input);
    }

    private Map<String, Object> post(String path, String field, String value) {
        Map<String, String> body = Map.of(field, Objects.requireNonNullElse(value, ""));
        Map<String, Object> response = webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(error -> Mono.just(Map.of(
                        "success", false,
                        "error", Map.of(
                                "message", "AI service unavailable",
                                "code", 502
                        )
                )))
                .block();

        if (response == null) {
            return Map.of(
                    "success", false,
                    "error", Map.of(
                            "message", "AI service returned an empty response",
                            "code", 502
                    )
            );
        }
        return response;
    }
}
