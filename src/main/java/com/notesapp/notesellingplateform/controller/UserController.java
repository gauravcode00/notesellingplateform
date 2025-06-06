package com.notesapp.notesellingplateform.controller;

import com.notesapp.notesellingplateform.dto.UserDto;
import com.notesapp.notesellingplateform.entity.User;
import com.notesapp.notesellingplateform.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public static class UserRequest {
        public String name;
        public String email;
        public String passwordHash;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal User user) {
        // Map to DTO (never expose password hash!)
        UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getWalletBalance());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@AuthenticationPrincipal User user, @RequestBody UserDto dto) {
        user.setName(dto.getName());
        // ...save user
        userRepo.save(user);
        return ResponseEntity.ok(new UserDto(user));
    }


    @PostMapping
        public ResponseEntity<?> createUser(@RequestBody UserRequest req) {
        // 1) Validate required fields
        if (req.name == null || req.email == null || req.passwordHash == null) {
            return ResponseEntity.badRequest()
                    .body("Missing required field: name, email, and passwordHash are all required.");
        }

        // 2) Check for existing user with that email
        Optional<User> existing = userRepo.findByEmail(req.email);
        if (existing.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("A user with email '" + req.email + "' already exists.");
        }

        // 3) Build a new User entity
        User u = new User();
        u.setName(req.name);
        u.setEmail(req.email);
        u.setPasswordHash(passwordEncoder.encode(req.passwordHash));
        u.setWalletBalance(0.0);

        // 4) Save to the database
        User saved = userRepo.save(u);

        // 5) Return the created user (omit the passwordHash in the response body)
        //    We’ll create a lightweight DTO for the response:
        var responseDto = new Object() {
            public Long id                      = saved.getId();
            public String name                  = saved.getName();
            public String email                 = saved.getEmail();
            public Double walletBalance         = saved.getWalletBalance();
            public Instant createdAt            = saved.getCreatedAt();
        };

        return ResponseEntity.ok(responseDto);
    }
}
