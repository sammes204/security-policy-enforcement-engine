package com.internship.tool.config;
 
import com.internship.tool.entity.*;
import com.internship.tool.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
 
import java.time.LocalDate;
import java.util.List;
 
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
 
    private final UserRepository userRepo;
    private final SecurityPolicyRepository policyRepo;
    private final PasswordEncoder encoder;
 
    @Override
    public void run(String... args) {
        seedUsers();
        seedPolicies();
    }
 
    private void seedUsers() {
        if (userRepo.count() > 0) return;
        log.info("Seeding users...");
        userRepo.saveAll(List.of(
            User.builder().username("admin").email("admin@tool60.com")
                .passwordHash(encoder.encode("Admin1234!")).role(User.Role.ADMIN).active(true).build(),
            User.builder().username("analyst").email("analyst@tool60.com")
                .passwordHash(encoder.encode("Analyst1234!")).role(User.Role.ANALYST).active(true).build(),
            User.builder().username("viewer").email("viewer@tool60.com")
                .passwordHash(encoder.encode("Viewer1234!")).role(User.Role.USER).active(true).build()
        ));
        log.info("Users seeded: admin / analyst / viewer");
    }
 
    private void seedPolicies() {
        if (policyRepo.count() > 0) return;
        log.info("Seeding 30 demo security policies...");
 
        var admin = userRepo.findByUsername("admin").orElse(null);
 
        var policies = List.of(
            makePolicy("MFA Enforcement", "All privileged accounts must use multi-factor authentication", "Access Control", SecurityPolicy.Severity.CRITICAL, SecurityPolicy.Status.ACTIVE, 92, "IT Security", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Data Encryption at Rest", "All PII data must be encrypted using AES-256", "Data Protection", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 85, "Data Team", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Password Complexity Policy", "Passwords must be at least 12 characters with mixed case, numbers, and symbols", "Access Control", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 78, "HR/IT", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Network Segmentation", "Production and development networks must be strictly segmented", "Network Security", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 80, "Network Ops", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("Patch Management SLA", "Critical CVEs must be patched within 72 hours of disclosure", "Vulnerability Mgmt", SecurityPolicy.Severity.CRITICAL, SecurityPolicy.Status.ACTIVE, 90, "SecOps", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Privileged Access Management", "All admin credentials must rotate every 30 days via PAM solution", "Access Control", SecurityPolicy.Severity.CRITICAL, SecurityPolicy.Status.ACTIVE, 95, "IT Security", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("SIEM Log Retention", "Security logs must be retained for minimum 13 months", "Logging & Monitoring", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 60, "SOC", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Endpoint Detection & Response", "All endpoints must have EDR agent installed and active", "Endpoint Security", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 82, "IT Security", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Vendor Risk Assessment", "All third-party vendors must complete annual security questionnaire", "Third Party Risk", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 55, "Procurement", SecurityPolicy.EnforcementType.ADVISORY, admin),
            makePolicy("Incident Response Plan", "IR playbooks must be reviewed and tested quarterly", "Incident Response", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 75, "CISO", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Cloud Storage ACL Review", "S3/Blob storage buckets must be audited monthly for public access", "Cloud Security", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 88, "Cloud Ops", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("API Rate Limiting", "All public APIs must implement rate limiting of max 1000 req/min per client", "Application Security", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 65, "Engineering", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Secure SDLC Training", "All developers must complete secure coding training annually", "Security Awareness", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 50, "Engineering Mgmt", SecurityPolicy.EnforcementType.ADVISORY, admin),
            makePolicy("DLP - Email Filtering", "Outbound email with PII must be blocked unless encrypted", "Data Loss Prevention", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 83, "Compliance", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("Physical Access Control", "Server rooms require biometric + badge dual authentication", "Physical Security", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 77, "Facilities", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Container Image Scanning", "All Docker images must pass Trivy scan before deployment", "DevSecOps", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.DRAFT, 70, "DevOps", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("Zero Trust Network Access", "Legacy VPN must be replaced with ZTNA by Q3 2026", "Network Security", SecurityPolicy.Severity.CRITICAL, SecurityPolicy.Status.DRAFT, 88, "Architecture", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Bug Bounty Program", "Critical vulnerabilities reported via HackerOne rewarded within 7 days", "Vulnerability Mgmt", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 45, "Security Team", SecurityPolicy.EnforcementType.ADVISORY, admin),
            makePolicy("Backup & Recovery Testing", "Full DR drill must be performed semi-annually", "Business Continuity", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 72, "IT Ops", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("GDPR Data Minimisation", "Collect only minimum necessary personal data for stated purpose", "Compliance", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 80, "DPO", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("TLS 1.3 Enforcement", "All internal and external services must use TLS 1.3 minimum", "Encryption", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 84, "Architecture", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("Secrets Management", "No credentials in source code; all secrets via Vault/KMS", "DevSecOps", SecurityPolicy.Severity.CRITICAL, SecurityPolicy.Status.ACTIVE, 93, "Engineering", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Mobile Device Management", "All corporate mobile devices must enroll in MDM within 24h of issue", "Endpoint Security", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 62, "IT Helpdesk", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Firewall Rule Review", "Firewall rules must be reviewed and re-certified quarterly", "Network Security", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.INACTIVE, 55, "Network Ops", SecurityPolicy.EnforcementType.ADVISORY, admin),
            makePolicy("Social Engineering Training", "Phishing simulation must be run monthly; <5% click rate target", "Security Awareness", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 58, "Security Team", SecurityPolicy.EnforcementType.ADVISORY, admin),
            makePolicy("SSH Key Rotation", "SSH keys for production servers must rotate every 90 days", "Access Control", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 76, "IT Security", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("DMARC Email Authentication", "All company domains must have DMARC p=reject configured", "Email Security", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.ACTIVE, 68, "IT Security", SecurityPolicy.EnforcementType.MANDATORY, admin),
            makePolicy("Software Bill of Materials", "SBOM must be generated for all production applications", "Supply Chain", SecurityPolicy.Severity.MEDIUM, SecurityPolicy.Status.DRAFT, 48, "Engineering", SecurityPolicy.EnforcementType.ADVISORY, admin),
            makePolicy("Insider Threat Monitoring", "UEBA must be active for all privileged user accounts", "Insider Threat", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 79, "SOC", SecurityPolicy.EnforcementType.AUTOMATED, admin),
            makePolicy("Annual Penetration Testing", "Full external pentest by accredited firm required annually", "Vulnerability Mgmt", SecurityPolicy.Severity.HIGH, SecurityPolicy.Status.ACTIVE, 82, "CISO", SecurityPolicy.EnforcementType.MANDATORY, admin)
        );
 
        policyRepo.saveAll(policies);
        log.info("30 demo security policies seeded.");
    }
 
    private SecurityPolicy makePolicy(String name, String description, String category,
                                       SecurityPolicy.Severity severity, SecurityPolicy.Status status,
                                       int riskScore, String owner, SecurityPolicy.EnforcementType type,
                                       User createdBy) {
        return SecurityPolicy.builder()
            .name(name).description(description).category(category)
            .severity(severity).status(status).riskScore(riskScore)
            .owner(owner).enforcementType(type)
            .isDeleted(false).createdBy(createdBy).updatedBy(createdBy)
            .effectiveDate(LocalDate.now().minusMonths(3))
            .reviewDate(LocalDate.now().plusMonths(9))
            .build();
    }
}