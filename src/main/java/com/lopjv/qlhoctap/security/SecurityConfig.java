package com.lopjv.qlhoctap.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                // Tắt CSRF vì ứng dụng dùng JWT (stateless), không dùng cookie session
                .csrf(AbstractHttpConfigurer::disable)

                // Xử lý lỗi xác thực (trả về 401 khi không có hoặc token không hợp lệ)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Cấu hình session: STATELESS — không tạo HttpSession
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Cấu hình phân quyền theo URL pattern
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép tất cả truy cập các API xác thực (đăng nhập, đăng ký)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Chỉ ADMIN mới được truy cập các API quản trị
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Chỉ TEACHER mới được truy cập các API dành cho giáo viên
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")

                        // Chỉ STUDENT mới được truy cập các API dành cho sinh viên
                        .requestMatchers("/api/student/**").hasRole("STUDENT")

                        // Tất cả các request còn lại đều yêu cầu xác thực
                        .anyRequest().authenticated()
                )

                // Cấu hình AuthenticationProvider sử dụng CustomUserDetailsService
                .authenticationProvider(daoAuthenticationProvider())

                // Thêm JwtAuthenticationFilter vào trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
