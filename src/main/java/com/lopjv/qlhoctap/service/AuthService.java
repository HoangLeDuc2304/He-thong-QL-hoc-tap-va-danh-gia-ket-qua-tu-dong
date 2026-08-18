package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.JwtAuthResponse;
import com.lopjv.qlhoctap.dto.LoginRequest;
import com.lopjv.qlhoctap.dto.RegisterRequest;
import com.lopjv.qlhoctap.entity.Role;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.repository.RoleRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import com.lopjv.qlhoctap.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public JwtAuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại trong hệ thống: " + registerRequest.getUsername());
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Địa chỉ Email đã tồn tại trong hệ thống: " + registerRequest.getEmail());
        }

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò ROLE_STUDENT trong hệ thống"));

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .isActive(true)
                .roles(Set.of(studentRole))
                .build();

        userRepository.save(user);
    }
}
