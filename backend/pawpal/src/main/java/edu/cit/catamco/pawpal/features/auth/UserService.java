package edu.cit.catamco.pawpal.features.auth;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.features.verification.VerificationRepository;
import edu.cit.catamco.pawpal.features.verification.VerificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final String uploadDir = "uploads/profiles/";

    public UserService(UserRepository userRepository,
                       VerificationRepository verificationRepository) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
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

        if (body.get("fullName") != null && !body.get("fullName").isBlank())
            user.setFullName(body.get("fullName"));
        if (body.get("phoneNumber") != null)
            user.setPhoneNumber(body.get("phoneNumber"));
        if (body.get("address") != null)
            user.setAddress(body.get("address"));

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

    public AuthResponse getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = users.stream()
                .filter(u -> u.getRole() != User.Role.ADMIN)
                .map(u -> {
                    Map<String, Object> map = toUserMap(u);
                    boolean isVerified = verificationRepository
                            .findTopByUserOrderByCreatedAtDesc(u)
                            .map(v -> v.getStatus() == VerificationRequest.Status.APPROVED)
                            .orElse(false);
                    map.put("isVerified", isVerified);
                    return map;
                })
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .success(true)
                .data(result)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse banUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return error("USER-001", "User not found.");
        user.setBanned(true);
        userRepository.save(user);
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "User banned."))
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
        map.put("address", user.getAddress() != null ? user.getAddress() : "");
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        map.put("isBanned", user.isBanned());
        return map;
    }

    private AuthResponse error(String code, String message) {
        return AuthResponse.builder()
                .success(false)
                .error(Map.of("code", code, "message", message))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse unbanUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return error("USER-001", "User not found.");
        user.setBanned(false);
        userRepository.save(user);
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "User unbanned."))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}