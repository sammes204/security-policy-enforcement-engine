package com.internship.tool.service;
 
import com.internship.tool.config.JwtUtil;
import com.internship.tool.dto.AuthDtos;
import com.internship.tool.entity.User;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
 
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
 
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        var user = userRepo.findByUsername(auth.getName()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return AuthDtos.AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .role(user.getRole().name())
            .expiresIn(jwtUtil.getExpiryMs())
            .build();
    }
 
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already taken");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already registered");
 
        var user = User.builder()
            .username(req.getUsername())
            .email(req.getEmail())
            .passwordHash(encoder.encode(req.getPassword()))
            .role(User.Role.USER)
            .active(true)
            .build();
        userRepo.save(user);
 
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return AuthDtos.AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .role(user.getRole().name())
            .expiresIn(jwtUtil.getExpiryMs())
            .build();
    }
}