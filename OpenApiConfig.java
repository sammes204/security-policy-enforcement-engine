package com.internship.tool.config;
 
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
 
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Tool-60: Security Policy Enforcement Engine",
        version = "1.0.0",
        description = "AI-powered security policy management API. Use /api/auth/login to get a JWT, then click Authorize."
    ),
    servers = @Server(url = "http://localhost:8080")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {}