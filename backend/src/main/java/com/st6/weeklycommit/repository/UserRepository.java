package com.st6.weeklycommit.repository;

import com.st6.weeklycommit.entity.User;
import com.st6.weeklycommit.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByManagerId(UUID managerId);
}
