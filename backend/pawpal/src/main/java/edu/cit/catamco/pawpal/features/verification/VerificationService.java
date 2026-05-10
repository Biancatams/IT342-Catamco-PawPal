package edu.cit.catamco.pawpal.features.verification;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.features.auth.User;
import edu.cit.catamco.pawpal.features.auth.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final String uploadDir = "uploads/verifications/";

    public VerificationService(VerificationRepository verificationRepository,
                               UserRepository userRepository,
                               EmailService emailService) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public AuthResponse submitVerification(String email, String reason, MultipartFile idImage,
                                           String fullName, String phoneNumber, String location) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return error("USER-001", "User not found.");

        Optional<VerificationRequest> latest = verificationRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latest.isPresent() && latest.get().getStatus() == VerificationRequest.Status.PENDING) {
            return error("VERIF-001", "You already have a pending verification request.");
        }

        String imageUrl = null;
        if (idImage != null && !idImage.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                String filename = UUID.randomUUID() + "_" + idImage.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(idImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                imageUrl = "/uploads/verifications/" + filename;
            } catch (IOException e) {
                return error("FILE-001", "Failed to upload ID image.");
            }
        } else {
            return error("VERIF-002", "ID image is required.");
        }

        VerificationRequest request = new VerificationRequest();
        request.setUser(user);
        request.setReason(reason);
        request.setIdImageUrl(imageUrl);
        verificationRepository.save(request);

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if ((user.getPhoneNumber() == null || user.getPhoneNumber().isBlank())
                && phoneNumber != null && !phoneNumber.isBlank()) {
            user.setPhoneNumber(phoneNumber);
        }
        if ((user.getAddress() == null || user.getAddress().isBlank())
                && location != null && !location.isBlank()) {
            user.setAddress(location);
        }
        userRepository.save(user);

        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Verification request submitted successfully."))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse getMyVerification(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return error("USER-001", "User not found.");

        Optional<VerificationRequest> latest = verificationRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latest.isEmpty()) {
            return AuthResponse.builder()
                    .success(true)
                    .data(Map.of("status", "NONE"))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        return AuthResponse.builder()
                .success(true)
                .data(toVerifMap(latest.get()))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse getAllVerifications() {
        List<VerificationRequest> all = verificationRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> list = new ArrayList<>();
        for (VerificationRequest v : all) list.add(toVerifMap(v));
        return AuthResponse.builder()
                .success(true)
                .data(list)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse approveVerification(Long id) {
        VerificationRequest request = verificationRepository.findById(id).orElse(null);
        if (request == null) return error("VERIF-003", "Verification request not found.");

        request.setStatus(VerificationRequest.Status.APPROVED);
        verificationRepository.save(request);

        try {
            emailService.sendVerificationApproved(
                    request.getUser().getEmail(),
                    request.getUser().getFullName()
            );
        } catch (Exception ignored) {}

        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Verification approved."))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse rejectVerification(Long id, String adminComment) {
        VerificationRequest request = verificationRepository.findById(id).orElse(null);
        if (request == null) return error("VERIF-003", "Verification request not found.");

        request.setStatus(VerificationRequest.Status.REJECTED);
        request.setAdminComment(adminComment);
        verificationRepository.save(request);

        try {
            emailService.sendVerificationRejected(
                    request.getUser().getEmail(),
                    request.getUser().getFullName(),
                    adminComment
            );
        } catch (Exception ignored) {}

        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Verification rejected."))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private Map<String, Object> toVerifMap(VerificationRequest v) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", v.getId());
        map.put("status", v.getStatus().name());
        map.put("idImageUrl", v.getIdImageUrl());
        map.put("reason", v.getReason() != null ? v.getReason() : "");
        map.put("adminComment", v.getAdminComment() != null ? v.getAdminComment() : "");
        map.put("createdAt", v.getCreatedAt().toString());
        map.put("updatedAt", v.getUpdatedAt().toString());
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", v.getUser().getId());
        userMap.put("fullName", v.getUser().getFullName());
        userMap.put("email", v.getUser().getEmail());
        userMap.put("role", v.getUser().getRole());
        userMap.put("profileImageUrl", v.getUser().getProfileImageUrl() != null ? v.getUser().getProfileImageUrl() : "");
        userMap.put("phoneNumber", v.getUser().getPhoneNumber() != null ? v.getUser().getPhoneNumber() : "");
        userMap.put("address", v.getUser().getAddress() != null ? v.getUser().getAddress() : "");
        map.put("user", userMap);
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