package edu.cit.catamco.pawpal.service;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.entity.User;
import edu.cit.catamco.pawpal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final String uploadDir = "uploads/profiles/";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse getMe(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return error("USER-001", "User not found.");

        return AuthResponse.builder()
                .success(true)
                .data(toUserMap(user))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse updateMe(String email, Map<String, String> body, MultipartFile image) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return error("USER-001", "User not found.");

        if (body.get("fullName") != null && !body.get("fullName").isBlank()) {
            user.setFullName(body.get("fullName"));
        }
        if (body.get("phoneNumber") != null) {
            user.setPhoneNumber(body.get("phoneNumber"));
        }

        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                user.setProfileImageUrl("/uploads/profiles/" + filename);
            } catch (IOException e) {
                return error("FILE-001", "Failed to upload profile image.");
            }
        }

        userRepository.save(user);

        return AuthResponse.builder()
                .success(true)
                .data(toUserMap(user))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private Map<String, Object> toUserMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        map.put("profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
        map.put("role", user.getRole());
        map.put("createdAt", user.getCreatedAt().toString());
        return map;
    }

    private AuthResponse error(String code, String message) {
        return AuthResponse.builder()
                .success(false)
                .error(Map.of("code", code, "message", message))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}