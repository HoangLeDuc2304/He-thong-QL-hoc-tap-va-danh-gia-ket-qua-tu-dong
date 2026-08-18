package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO phản hồi chứa thông tin JWT Token và thông tin cơ bản của người dùng sau khi xác thực thành công.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private String username;
    private List<String> roles;
}
