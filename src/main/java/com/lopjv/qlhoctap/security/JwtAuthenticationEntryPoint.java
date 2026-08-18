package com.lopjv.qlhoctap.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        logger.error("Lỗi xác thực - Unauthorized: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\", \"timestamp\": \"%s\"}",
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "Bạn cần đăng nhập để truy cập tài nguyên này.",
                request.getServletPath(),
                LocalDateTime.now().toString()
        );

        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }
}
