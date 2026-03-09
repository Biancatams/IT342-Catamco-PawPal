package edu.cit.catamco.pawpal.service;

import edu.cit.catamco.pawpal.dto.*;
import edu.cit.catamco.pawpal.entity.User;
import edu.cit.catamco.pawpal.repository.UserRepository;
import edu.cit.catamco.pawpal.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "VALID-001",
                            "message", "Passwords do not match"))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "DB-002",
                            "message", "Email already registered"))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(request.getRole().toUpperCase()))
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .data(Map.of(
                        "user", Map.of(
                                "id", user.getId(),
                                "fullName", user.getFullName(),
                                "email", user.getEmail(),
                                "role", user.getRole()
                        ),
                        "accessToken", token
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "AUTH-001",
                            "message", "Invalid email or password"))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .data(Map.of(
                        "user", Map.of(
                                "id", user.getId(),
                                "fullName", user.getFullName(),
                                "email", user.getEmail(),
                                "role", user.getRole()
                        ),
                        "accessToken", token
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}