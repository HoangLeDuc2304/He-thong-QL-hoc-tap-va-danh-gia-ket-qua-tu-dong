package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phản hồi khi đăng nhập thành công.
 * Chứa JWT Token và thông tin cơ bản của người dùng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String username;
    private String fullName;
    private String role;
}
