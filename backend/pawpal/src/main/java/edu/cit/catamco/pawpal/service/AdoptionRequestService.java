package edu.cit.catamco.pawpal.service;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.entity.AdoptionRequest;
import edu.cit.catamco.pawpal.entity.Pet;
import edu.cit.catamco.pawpal.entity.User;
import edu.cit.catamco.pawpal.facade.AdoptionFacade;
import edu.cit.catamco.pawpal.observer.AdoptionEventListener;
import edu.cit.catamco.pawpal.repository.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.repository.PetRepository;
import edu.cit.catamco.pawpal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdoptionRequestService implements AdoptionFacade {

    private final AdoptionRequestRepository requestRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final List<AdoptionEventListener> listeners;

    public AdoptionRequestService(
            AdoptionRequestRepository requestRepository,
            PetRepository petRepository,
            UserRepository userRepository,
            List<AdoptionEventListener> listeners) {
        this.requestRepository = requestRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.listeners = listeners;
    }

    // ── Submit Adoption Request ──────────────────────────────────────────────
    @Override
    public AuthResponse submitRequest(String adopterEmail, Map<String, String> body) {
        User adopter = userRepository.findByEmail(adopterEmail).orElse(null);
        if (adopter == null) return error("USER-001", "User not found.");

        if (adopter.getRole() != User.Role.ADOPTER)
            return error("AUTH-003", "Only adopters can submit requests.");

        Long petId = Long.parseLong(body.get("petId"));
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) return error("DB-001", "Pet not found.");

        if (pet.getStatus() != Pet.PetStatus.AVAILABLE)
            return error("ADOPT-002", "This pet is no longer available.");

        if (requestRepository.existsByPetAndAdopterAndStatus(pet, adopter, AdoptionRequest.RequestStatus.PENDING))
            return error("ADOPT-001", "You already have a pending request for this pet.");

        AdoptionRequest req = new AdoptionRequest();
        req.setPet(pet);
        req.setAdopter(adopter);
        req.setAdopterName(body.getOrDefault("adopterName", adopter.getFullName()));
        req.setContactInfo(body.getOrDefault("contactInfo", ""));
        req.setReason(body.getOrDefault("reason", ""));
        req.setNoteToOwner(body.getOrDefault("noteToOwner", null));
        req.setStatus(AdoptionRequest.RequestStatus.PENDING);
        requestRepository.save(req);

        return AuthResponse.builder()
                .success(true)
                .data(Map.of(
                        "id", req.getId(),
                        "petId", petId,
                        "status", req.getStatus(),
                        "createdAt", req.getCreatedAt().toString()
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Get Requests for a Pet ───────────────────────────────────────────────
    @Override
    public AuthResponse getRequestsForPet(String ownerEmail, Long petId) {
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) return error("DB-001", "Pet not found.");

        if (!pet.getOwner().getEmail().equals(ownerEmail))
            return error("AUTH-003", "You can only view requests for your own pets.");

        List<AdoptionRequest> requests = requestRepository.findByPet(pet);
        List<Map<String, Object>> list = requests.stream().map(this::toRequestSummary).toList();

        return AuthResponse.builder()
                .success(true)
                .data(list)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Get My Requests (Adopter) ────────────────────────────────────────────
    @Override
    public AuthResponse getMyRequests(String adopterEmail) {
        User adopter = userRepository.findByEmail(adopterEmail).orElse(null);
        if (adopter == null) return error("USER-001", "User not found.");

        List<AdoptionRequest> requests = requestRepository.findByAdopter(adopter);
        List<Map<String, Object>> list = requests.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("pet", Map.of(
                    "id", r.getPet().getId(),
                    "name", r.getPet().getName(),
                    "breed", r.getPet().getBreed() != null ? r.getPet().getBreed() : "",
                    "age", r.getPet().getAge(),
                    "imageUrl", r.getPet().getImageUrl() != null ? r.getPet().getImageUrl() : ""
            ));
            User owner = r.getPet().getOwner();
            map.put("owner", Map.of(
                    "fullName", owner.getFullName(),
                    "email", owner.getEmail(),
                    "phoneNumber", owner.getPhoneNumber() != null ? owner.getPhoneNumber() : "",
                    "profileImageUrl", owner.getProfileImageUrl() != null ? owner.getProfileImageUrl() : ""
            ));
            map.put("status", r.getStatus());
            map.put("declineReason", r.getDeclineReason() != null ? r.getDeclineReason() : "");
            map.put("createdAt", r.getCreatedAt().toString());
            return map;
        }).toList();

        return AuthResponse.builder()
                .success(true)
                .data(list)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Approve Request (Observer triggered here) ────────────────────────────
    @Override
    public AuthResponse approveRequest(String ownerEmail, Long requestId) {
        AdoptionRequest req = requestRepository.findById(requestId).orElse(null);
        if (req == null) return error("DB-001", "Request not found.");

        if (!req.getPet().getOwner().getEmail().equals(ownerEmail))
            return error("AUTH-003", "You can only manage requests for your own pets.");

        req.setStatus(AdoptionRequest.RequestStatus.APPROVED);
        requestRepository.save(req);

        // Notify all registered observers about the approval event
        for (AdoptionEventListener listener : listeners) {
            listener.onAdoptionApproved(req, requestRepository, petRepository);
        }

        return AuthResponse.builder()
                .success(true)
                .data(Map.of(
                        "adoptionId", req.getId(),
                        "status", "APPROVED",
                        "petStatus", "ADOPTED"
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Decline Request ──────────────────────────────────────────────────────
    @Override
    public AuthResponse declineRequest(String ownerEmail, Long requestId, Map<String, String> body) {
        AdoptionRequest req = requestRepository.findById(requestId).orElse(null);
        if (req == null) return error("DB-001", "Request not found.");

        if (!req.getPet().getOwner().getEmail().equals(ownerEmail))
            return error("AUTH-003", "You can only manage requests for your own pets.");

        req.setStatus(AdoptionRequest.RequestStatus.DECLINED);
        req.setDeclineReason(body != null ? body.getOrDefault("declineReason", "") : "");
        requestRepository.save(req);

        return AuthResponse.builder()
                .success(true)
                .data(Map.of("adoptionId", req.getId(), "status", "DECLINED"))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Map<String, Object> toRequestSummary(AdoptionRequest r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("adopterName", r.getAdopterName());
        map.put("contactInfo", r.getContactInfo());
        map.put("reason", r.getReason());
        map.put("noteToOwner", r.getNoteToOwner() != null ? r.getNoteToOwner() : "");
        map.put("declineReason", r.getDeclineReason() != null ? r.getDeclineReason() : "");
        map.put("status", r.getStatus());
        map.put("adopter", Map.of(
                "id", r.getAdopter().getId(),
                "fullName", r.getAdopter().getFullName(),
                "profileImageUrl", r.getAdopter().getProfileImageUrl() != null
                        ? r.getAdopter().getProfileImageUrl() : ""
        ));
        map.put("createdAt", r.getCreatedAt().toString());
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
