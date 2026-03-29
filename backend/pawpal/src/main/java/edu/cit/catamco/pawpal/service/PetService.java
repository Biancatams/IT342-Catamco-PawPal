package edu.cit.catamco.pawpal.service;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.dto.PetRequest;
import edu.cit.catamco.pawpal.entity.Pet;
import edu.cit.catamco.pawpal.entity.User;
import edu.cit.catamco.pawpal.repository.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.repository.PetRepository;
import edu.cit.catamco.pawpal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import edu.cit.catamco.pawpal.repository.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.entity.AdoptionRequest;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AdoptionRequestRepository adoptionRequestRepository;

    // Folder where uploaded images will be saved
    private final String uploadDir = "uploads/pets/";

    public PetService(PetRepository petRepository, UserRepository userRepository, AdoptionRequestRepository adoptionRequestRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.adoptionRequestRepository = adoptionRequestRepository;
    }

    // ── Post a Pet ──────────────────────────────────────────────────────────
    public AuthResponse createPet(String email, PetRequest request, MultipartFile image) {
        User owner = userRepository.findByEmail(email).orElse(null);
        if (owner == null) {
            return errorResponse("USER-001", "User not found.");
        }

        if (owner.getRole() != User.Role.PET_OWNER) {
            return errorResponse("AUTH-003", "Only Pet Owners can post pets.");
        }

        // Save image
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                imageUrl = "/uploads/pets/" + filename;
            } catch (IOException e) {
                return errorResponse("FILE-001", "Failed to upload image.");
            }
        }

        Pet pet = new Pet();
        pet.setOwner(owner);
        pet.setName(request.getName());
        pet.setType(Pet.PetType.valueOf(request.getType().toUpperCase()));
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        if (request.getGender() != null && !request.getGender().isBlank()) {
            pet.setGender(Pet.Gender.valueOf(request.getGender().toUpperCase()));
        }
        pet.setDescription(request.getDescription());
        pet.setLocation(request.getLocation());
        pet.setLatitude(request.getLatitude());
        pet.setLongitude(request.getLongitude());
        pet.setCharacteristics(request.getCharacteristics());
        pet.setImageUrl(imageUrl);
        pet.setStatus(Pet.PetStatus.AVAILABLE);

        petRepository.save(pet);

        return AuthResponse.builder()
                .success(true)
                .data(Map.of(
                        "id", pet.getId(),
                        "name", pet.getName(),
                        "imageUrl", pet.getImageUrl() != null ? pet.getImageUrl() : "",
                        "status", pet.getStatus(),
                        "createdAt", pet.getCreatedAt().toString()
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Get All Available Pets ───────────────────────────────────────────────
    public AuthResponse getAllPets() {
        List<Pet> pets = petRepository.findByStatus(Pet.PetStatus.AVAILABLE);
        List<Map<String, Object>> petList = pets.stream().map(this::toPetSummary).toList();
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("pets", petList))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Get Pet by ID ────────────────────────────────────────────────────────
    public AuthResponse getPetById(Long id) {
        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) {
            return errorResponse("DB-001", "Pet not found.");
        }
        return AuthResponse.builder()
                .success(true)
                .data(toPetDetail(pet))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Get My Pets (Owner Dashboard) ────────────────────────────────────────
    public AuthResponse getMyPets(String email) {
        User owner = userRepository.findByEmail(email).orElse(null);
        if (owner == null) return errorResponse("USER-001", "User not found.");

        List<Pet> pets = petRepository.findByOwner(owner);
        List<Map<String, Object>> petList = pets.stream().map(this::toPetSummary).toList();
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("pets", petList))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Delete Pet ───────────────────────────────────────────────────────────
    public AuthResponse deletePet(String email, Long id) {
        User owner = userRepository.findByEmail(email).orElse(null);
        Pet pet = petRepository.findById(id).orElse(null);

        if (pet == null) return errorResponse("DB-001", "Pet not found.");
        if (!pet.getOwner().getEmail().equals(email)) {
            return errorResponse("AUTH-003", "You can only delete your own pets.");
        }

        petRepository.delete(pet);
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Pet listing deleted successfully."))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Map<String, Object> toPetSummary(Pet pet) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pet.getId());
        map.put("name", pet.getName());
        map.put("type", pet.getType());
        map.put("breed", pet.getBreed() != null ? pet.getBreed() : "");
        map.put("age", pet.getAge());
        map.put("gender", pet.getGender() != null ? pet.getGender() : "");
        map.put("location", pet.getLocation());
        map.put("imageUrl", pet.getImageUrl() != null ? pet.getImageUrl() : "");
        map.put("status", pet.getStatus());
        map.put("description", pet.getDescription() != null ? pet.getDescription() : "");
        map.put("owner", Map.of(
                "id", pet.getOwner().getId(),
                "fullName", pet.getOwner().getFullName()
        ));
        // ← COUNT PENDING REQUESTS
        long pendingCount = adoptionRequestRepository
                .findByPetAndStatus(pet, AdoptionRequest.RequestStatus.PENDING)
                .size();
        map.put("requestCount", pendingCount);
        map.put("createdAt", pet.getCreatedAt().toString());
        return map;
    }

    private Map<String, Object> toPetDetail(Pet pet) {
        Map<String, Object> map = new LinkedHashMap<>(toPetSummary(pet));
        map.put("description", pet.getDescription() != null ? pet.getDescription() : "");
        map.put("latitude", pet.getLatitude());
        map.put("longitude", pet.getLongitude());
        map.put("characteristics", pet.getCharacteristics() != null ? pet.getCharacteristics() : List.of());
        return map;
    }

    private AuthResponse errorResponse(String code, String message) {
        return AuthResponse.builder()
                .success(false)
                .error(Map.of("code", code, "message", message))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse updatePet(String email, Long id, PetRequest request, MultipartFile image) {
        User owner = userRepository.findByEmail(email).orElse(null);
        if (owner == null) return errorResponse("USER-001", "User not found.");

        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) return errorResponse("DB-001", "Pet not found.");

        if (!pet.getOwner().getEmail().equals(email))
            return errorResponse("AUTH-003", "You can only edit your own pets.");

        // Update image only if new one provided
        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                pet.setImageUrl("/uploads/pets/" + filename);
            } catch (IOException e) {
                return errorResponse("FILE-001", "Failed to upload image.");
            }
        }

        pet.setName(request.getName());
        pet.setType(Pet.PetType.valueOf(request.getType().toUpperCase()));
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        if (request.getGender() != null && !request.getGender().isBlank()) {
            pet.setGender(Pet.Gender.valueOf(request.getGender().toUpperCase()));
        }
        pet.setDescription(request.getDescription());
        pet.setLocation(request.getLocation());
        if (request.getLatitude() != null) pet.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) pet.setLongitude(request.getLongitude());
        if (request.getCharacteristics() != null) pet.setCharacteristics(request.getCharacteristics());

        petRepository.save(pet);

        return AuthResponse.builder()
                .success(true)
                .data(toPetDetail(pet))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}