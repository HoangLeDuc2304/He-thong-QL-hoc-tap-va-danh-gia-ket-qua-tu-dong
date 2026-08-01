package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByRoleAndIsActive(UserRole role, Boolean isActive);
}
