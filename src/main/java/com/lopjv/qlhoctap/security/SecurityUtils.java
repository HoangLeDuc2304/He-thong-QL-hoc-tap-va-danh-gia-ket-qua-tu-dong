package com.lopjv.qlhoctap.security;

import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Tiện ích lấy thông tin người dùng đang đăng nhập từ JWT Security Context.
 * Sử dụng trong Controller để tránh nhận userId từ client (lỗ hổng bảo mật).
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Utility class — không khởi tạo
    }

    /**
     * Lấy username của người dùng đang đăng nhập từ JWT token.
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Không có phiên đăng nhập hợp lệ.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    /**
     * Lấy đối tượng User đang đăng nhập từ database, dựa trên username trong JWT.
     *
     * @param userRepository Repository để truy vấn user
     * @return User entity hiện tại
     */
    public static User getCurrentUser(UserRepository userRepository) {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }
}
