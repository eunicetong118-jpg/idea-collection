package com.ideacollection.service;

import com.ideacollection.dto.RegisterRequest;
import com.ideacollection.dto.LoginRequest;
import com.ideacollection.dto.AuthResponse;
import com.ideacollection.model.User;
import com.ideacollection.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = Keys.hmacShaKeyFor(
            "idea-collection-secret-key-must-be-at-least-256-bits-long-for-hs256".getBytes(StandardCharsets.UTF_8)
        );
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAdmin(false);

        userRepository.save(user);

        String token = generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.isAdmin());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.isAdmin());
    }

    private String generateToken(User user) {
        return Jwts.builder()
            .subject(user.getId())
            .claim("username", user.getUsername())
            .claim("isAdmin", user.isAdmin())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(secretKey)
            .compact();
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }
}