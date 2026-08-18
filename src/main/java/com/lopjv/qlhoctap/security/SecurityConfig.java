package com.lopjv.qlhoctap.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // Vô hiệu hóa CSRF do sử dụng xác thực qua JWT Stateless
                .csrf(AbstractHttpConfigurer::disable)
                // Cấu hình CORS cho phép ứng dụng Frontend gọi API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Xử lý ngoại lệ xác thực không hợp lệ (trả về HTTP 401 Unauthorized JSON)
                .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // Quản lý Session phía Server theo chế độ STATELESS
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Cấu hình Phân quyền chi tiết (Role-based Authorization)
                .authorizeHttpRequests(authorize -> authorize
                // Các API xác thực không yêu cầu Token
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Cho phép đọc công khai danh sách khóa học trong demo/dev
                .requestMatchers(HttpMethod.GET, "/api/v1/courses", "/api/v1/courses/**").permitAll()
                // Cho phép xem file bài nộp UML đã upload (link mở tab mới không kèm token)
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                // Cho phép cả TEACHER kích hoạt AI chấm bài nộp UML của sinh viên
                .requestMatchers(HttpMethod.POST, "/api/v1/student/uml-submissions/*/analyze").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                // Chỉ dành cho vai trò ADMIN
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Dành cho TEACHER hoặc ADMIN
                .requestMatchers("/api/v1/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                // Dành cho STUDENT hoặc ADMIN
                .requestMatchers("/api/v1/student/**").hasAnyRole("STUDENT", "ADMIN")
                // Các request khác bắt buộc phải đăng nhập
                .anyRequest().authenticated()
                )
                // Cấu hình AuthenticationProvider với CustomUserDetailsService & BCrypt
                .authenticationProvider(daoAuthenticationProvider())
                // Đăng ký JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfiguration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword != null && encodedPassword.startsWith("$2")) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }
                return encodedPassword != null && encodedPassword.contentEquals(rawPassword);
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
