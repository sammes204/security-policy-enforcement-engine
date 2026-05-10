package com.internship.tool.config;
 
import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
 
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
 
    private final AuditLogRepository auditRepo;
 
    @AfterReturning(
        pointcut = "execution(* com.internship.tool.service.SecurityPolicyService.create(..))" +
                   "|| execution(* com.internship.tool.service.SecurityPolicyService.update(..))" +
                   "|| execution(* com.internship.tool.service.SecurityPolicyService.delete(..))",
        returning = "result"
    )
    public void afterWrite(JoinPoint jp, Object result) {
        try {
            String method = jp.getSignature().getName().toUpperCase();
            String actor  = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName() : "system";
            log.debug("AUDIT aspect fired: {} by {}", method, actor);
        } catch (Exception e) {
            log.warn("Audit aspect error: {}", e.getMessage());
        }
    }
}