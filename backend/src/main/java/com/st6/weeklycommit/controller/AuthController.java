package com.st6.weeklycommit.controller;

import com.st6.weeklycommit.entity.User;
import com.st6.weeklycommit.entity.enums.UserRole;
import com.st6.weeklycommit.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public User register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");

        if (name == null || email == null || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email, and password are required");
        }
        if (password.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 4 characters");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(password);
        String role = body.getOrDefault("role", "EMPLOYEE");
        user.setRole(UserRole.valueOf(role));

        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return user;
    }

    @PostMapping("/invite")
    public User inviteEmployee(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String managerId = body.get("managerId");

        if (name == null || email == null || managerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email, and managerId are required");
        }
        UUID managerUuid = UUID.fromString(managerId);
        User manager = userRepository.findById(managerUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager not found"));
        if (manager.getRole() != UserRole.MANAGER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only managers can add employees");
        }

        var existing = userRepository.findByEmail(email.trim().toLowerCase());
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getManagerId() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee is already on a team");
            }
            user.setManagerId(managerUuid);
            return userRepository.save(user);
        }

        User employee = new User();
        employee.setId(UUID.randomUUID());
        employee.setName(name.trim());
        employee.setEmail(email.trim().toLowerCase());
        employee.setPassword("welcome1");
        employee.setRole(UserRole.EMPLOYEE);
        employee.setManagerId(managerUuid);

        return userRepository.save(employee);
    }
}
