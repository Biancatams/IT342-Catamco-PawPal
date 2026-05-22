package edu.cit.catamco.pawpal.features.auth;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.features.adoption.AdoptionRequestRepository;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AdoptionRequestRepository adoptionRequestRepository;
    private final edu.cit.catamco.pawpal.features.pets.PetRepository petRepository;

    public ReportController(ReportRepository reportRepository,
                            UserRepository userRepository,
                            AdoptionRequestRepository adoptionRequestRepository,
                            edu.cit.catamco.pawpal.features.pets.PetRepository petRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.petRepository = petRepository;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> submitReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        User reporter = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (reporter == null) return ResponseEntity.status(401).build();

        Long reportedUserId = Long.valueOf(body.get("reportedUserId").toString());
        User reportedUser = userRepository.findById(reportedUserId).orElse(null);
        if (reportedUser == null) return ResponseEntity.badRequest().build();

        String reason = body.get("reason").toString();

        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReason(reason);

        if (body.containsKey("adoptionRequestId") && body.get("adoptionRequestId") != null) {
            Long arId = Long.valueOf(body.get("adoptionRequestId").toString());
            adoptionRequestRepository.findById(arId).ifPresent(report::setAdoptionRequest);
        }

        reportRepository.save(report);
        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Report submitted."))
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    @GetMapping("/all")
    public ResponseEntity<AuthResponse> getAllReports() {
        List<Map<String, Object>> result = reportRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", r.getId());
                    map.put("reporterName", r.getReporter().getFullName());
                    map.put("reporterEmail", r.getReporter().getEmail());
                    map.put("reporterProfileImage", r.getReporter().getProfileImageUrl());
                    map.put("reportedUserId", r.getReportedUser().getId());
                    map.put("reportedUserName", r.getReportedUser().getFullName());
                    map.put("reportedUserEmail", r.getReportedUser().getEmail());
                    map.put("reportedUserProfileImage", r.getReportedUser().getProfileImageUrl());
                    map.put("reportedUserBanned", r.getReportedUser().isBanned());
                    map.put("reason", r.getReason());
                    map.put("status", r.getStatus());
                    map.put("createdAt", r.getCreatedAt().toString());
                    map.put("reporterRole", r.getReporter().getRole());
                    map.put("reportedUserRole", r.getReportedUser().getRole());
                    if (r.getAdoptionRequest() != null) {
                        var ar = r.getAdoptionRequest();
                        map.put("adoptionRequest", new java.util.LinkedHashMap<String, Object>() {{
                            put("id", ar.getId());
                            put("adopterName", ar.getAdopterName());
                            put("contactInfo", ar.getContactInfo());
                            put("reason", ar.getReason());
                            put("noteToOwner", ar.getNoteToOwner() != null ? ar.getNoteToOwner() : "");
                            put("status", ar.getStatus());
                            put("createdAt", ar.getCreatedAt().toString());
                        }});
                    } else {
                        map.put("adoptionRequest", null);
                    }
                    if (r.getReportedUser().getRole() == edu.cit.catamco.pawpal.features.auth.User.Role.PET_OWNER) {
                        List<Map<String, Object>> petList = petRepository.findByOwner(r.getReportedUser())
                                .stream().map(p -> {
                                    Map<String, Object> pm = new java.util.LinkedHashMap<>();
                                    pm.put("id", p.getId());
                                    pm.put("name", p.getName());
                                    pm.put("type", p.getType() != null ? p.getType().toString() : "");
                                    pm.put("breed", p.getBreed());
                                    pm.put("age", p.getAge());
                                    pm.put("gender", p.getGender() != null ? p.getGender().toString() : "");
                                    pm.put("description", p.getDescription());
                                    pm.put("location", p.getLocation());
                                    pm.put("imageUrl", p.getImageUrl());
                                    pm.put("status", p.getStatus() != null ? p.getStatus().toString() : "");
                                    pm.put("characteristics", p.getCharacteristics());
                                    pm.put("vaccinated", p.getVaccinated());
                                    pm.put("neutered", p.getNeutered());
                                    pm.put("microchipped", p.getMicrochipped());
                                    pm.put("healthChecked", p.getHealthChecked());
                                    pm.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
                                    return pm;
                                }).collect(java.util.stream.Collectors.toList());
                        map.put("reportedUserPets", petList);
                    } else {
                        map.put("reportedUserPets", null);
                    }
                    return map;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .data(result)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AuthResponse> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Report report = reportRepository.findById(id).orElse(null);
        if (report == null) return ResponseEntity.notFound().build();

        String status = body.get("status").toString();
        report.setStatus(status);
        reportRepository.save(report);

        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Status updated to " + status))
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<AuthResponse> resolveReport(@PathVariable Long id) {
        Report report = reportRepository.findById(id).orElse(null);
        if (report == null) return ResponseEntity.notFound().build();
        report.setStatus("RESOLVED");
        reportRepository.save(report);
        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Report resolved."))
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
    }
}