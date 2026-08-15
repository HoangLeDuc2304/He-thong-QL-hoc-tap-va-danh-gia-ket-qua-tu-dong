package com.lopjv.qlhoctap.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(request.email());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Email không tồn tại"));
        }

        UserEntity user = userOpt.get();
        if (!user.getPassword().equals(request.password())) {
            return ResponseEntity.status(401).body(Map.of("message", "Mật khẩu không đúng"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(403).body(Map.of("message", "Tài khoản bị khóa"));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        body.put("email", user.getEmail());
        body.put("fullName", user.getFullName());
        body.put("role", user.getRole().name());
        body.put("active", user.isActive());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại"));
        }

        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username đã tồn tại"));
        }

        Role role;
        try {
            role = Role.valueOf(request.role().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role không hợp lệ"));
        }

        UserEntity user = new UserEntity(
                request.username(),
                request.email(),
                request.password(),
                request.fullName(),
                role,
                true
        );

        UserEntity saved = userRepository.save(user);
        Map<String, Object> body = new HashMap<>();
        body.put("id", saved.getId());
        body.put("username", saved.getUsername());
        body.put("email", saved.getEmail());
        body.put("fullName", saved.getFullName());
        body.put("role", saved.getRole().name());
        body.put("active", saved.isActive());
        return ResponseEntity.ok(body);
    }
}
