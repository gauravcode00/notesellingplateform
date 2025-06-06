package com.notesapp.notesellingplateform.controller;

import com.notesapp.notesellingplateform.entity.User;
import com.notesapp.notesellingplateform.repository.UserRepository;
import com.notesapp.notesellingplateform.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public Long userId;
        public String email;
        public String name;

        public LoginResponse(String token, User u) {
            this.token = token;
            this.userId = u.getId();
            this.email = u.getEmail();
            this.name = u.getName();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.email == null || req.password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        Optional<User> userOpt = userRepo.findByEmail(req.email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate JWT
        String jwt = jwtUtil.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(new LoginResponse(jwt, user));
    }
}
