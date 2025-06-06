package com.notesapp.notesellingplateform.service;

import com.notesapp.notesellingplateform.entity.User;
import com.notesapp.notesellingplateform.repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //constructor
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User register(String name, String email, String rawPassword) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepo.save(u);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Optional<User> findById(Long id){
        return userRepo.findById(id);
    }

    // (Later) methods to get/update profile, fetch wallet balance, etc.
}
