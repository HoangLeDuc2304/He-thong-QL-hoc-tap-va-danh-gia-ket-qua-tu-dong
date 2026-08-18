package com.lopjv.qlhoctap.config;

import com.lopjv.qlhoctap.entity.Role;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.repository.RoleRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ROLE_ADMIN", "Quản trị hệ thống");
        createRoleIfNotExists("ROLE_TEACHER", "Giảng viên");
        createRoleIfNotExists("ROLE_STUDENT", "Sinh viên");

        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("Administrator")
                    .email("admin@qlhoctap.local")
                    .isActive(true)
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(admin);
        }

        userRepository.findAll().stream()
                .filter(user -> "HASH_BCRYPT".equals(user.getPassword()))
                .forEach(user -> {
                    user.setPassword(passwordEncoder.encode("123456"));
                    userRepository.save(user);
                });
    }

    private void createRoleIfNotExists(String roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
        }
    }
}
