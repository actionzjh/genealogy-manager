package com.genealogy.controller;

import com.genealogy.dto.AuthResponse;
import com.genealogy.dto.LoginRequest;
import com.genealogy.dto.RegisterRequest;
import com.genealogy.entity.User;
import com.genealogy.repository.UserRepository;
import com.genealogy.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 认证控制器 - 注册/登录
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "success", false, "message", "用户名已存在"));
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()
            && userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "success", false, "message", "邮箱已被注册"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName() != null && !request.getDisplayName().isEmpty()
                ? request.getDisplayName() : request.getUsername());
        user.setStatus("active");

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("code", 200, "success", true, "message", "注册成功"));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "success", false, "message", "用户名或密码错误"));
        }

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !user.getStatus().equals("active")) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "success", false, "message", "账户不存在或已禁用"));
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername(), user.getDisplayName()));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "success", false, "message", "未登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "success", false, "message", "用户不存在"));
        }
        user.setPasswordHash(null); // 不返回密码哈希
        return ResponseEntity.ok(user);
    }
}
