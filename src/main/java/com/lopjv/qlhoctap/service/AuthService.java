package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.AuthResponse;
import com.lopjv.qlhoctap.dto.LoginRequest;
import com.lopjv.qlhoctap.dto.RegisterRequest;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.enums.UserRole;
import com.lopjv.qlhoctap.repository.UserRepository;
import com.lopjv.qlhoctap.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy người dùng với email: " + loginRequest.getEmail()));

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng: " + registerRequest.getEmail());
        }

        UserRole userRole = determineUserRole(registerRequest.getRole());

        User newUser = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(userRole)
                .isActive(true)
                .build();

        userRepository.save(newUser);

        LoginRequest autoLoginRequest = new LoginRequest(
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );

        return login(autoLoginRequest);
    }
    
    private UserRole determineUserRole(String roleString) {
        if (roleString == null || roleString.isBlank()) {
            return UserRole.STUDENT;
        }

        try {
            return UserRole.valueOf(roleString.toUpperCase().trim());
        } catch (IllegalArgumentException exception) {
            return UserRole.STUDENT;
        }
    }
}
