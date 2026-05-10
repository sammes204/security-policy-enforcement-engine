package com.internship.tool.dto;
 
import jakarta.validation.constraints.*;
import lombok.*;
 
public class AuthDtos {
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 100) private String username;
        @NotBlank @Email                    private String email;
        @NotBlank @Size(min = 8)            private String password;
    }
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
        private long expiresIn;
    }
}
 