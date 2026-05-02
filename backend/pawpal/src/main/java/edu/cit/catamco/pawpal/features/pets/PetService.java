package edu.cit.catamco.pawpal.features.pets;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.dto.PetRequest;
import edu.cit.catamco.pawpal.features.pets.Pet;
import edu.cit.catamco.pawpal.features.auth.User;
import edu.cit.catamco.pawpal.features.adoption.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.features.pets.PetRepository;
import edu.cit.catamco.pawpal.features.auth.UserRepository;
import edu.cit.catamco.pawpal.features.adoption.AdoptionRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AdoptionRequestRepository adoptionRequestRepository;

    private final String uploadDir = "uploads/pets/";

    public PetService(PetRepository petRepository, UserRepository userRepository,
                      AdoptionRequestRepository adoptionRequestRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.adoptionRequestRepository = adoptionRequestRepository;
    }

    public AuthResponse createPet(String email, PetRequest request, MultipartFile image) {
        User owner = userRepository.findByEmail(email).orElse(null);
        if (owner == null) return errorResponse("USER-001", "User not found.");
        if (owner.getRole() != User.Role.PET_OWNER)
            return errorResponse("AUTH-003", "Only Pet Owners can post pets.");

        String imageUrl = saveImage(image);
        if (imageUrl == null && image != null && !image.isEmpty())
            return errorResponse("FILE-001", "Failed to upload image.");

        Pet pet = new Pet();
        applyRequest(pet, request);
        pet.setOwner(owner);
        pet.setImageUrl(imageUrl);
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

    public AuthResponse getAllPets() {
        List<Pet> pets = petRepository.findByStatus(Pet.PetStatus.AVAILABLE);
        List<Map<String, Object>> petList = pets.stream().map(this::toPetSummary).toList();
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("pets", petList))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse getPetById(Long id) {
        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) return errorResponse("DB-001", "Pet not found.");
        return AuthResponse.builder()
                .success(true)
                .data(toPetDetail(pet))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

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

    public AuthResponse deletePet(String email, Long id) {
        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) return errorResponse("DB-001", "Pet not found.");
        if (!pet.getOwner().getEmail().equals(email))
            return errorResponse("AUTH-003", "You can only delete your own pets.");
        petRepository.delete(pet);
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Pet listing deleted successfully."))
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

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            if (imageUrl == null) return errorResponse("FILE-001", "Failed to upload image.");
            pet.setImageUrl(imageUrl);
        }

        applyRequest(pet, request);
        petRepository.save(pet);

        return AuthResponse.builder()
                .success(true)
                .data(toPetDetail(pet))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private void applyRequest(Pet pet, PetRequest request) {
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
        pet.setVaccinated(Boolean.TRUE.equals(request.getVaccinated()));
        pet.setNeutered(Boolean.TRUE.equals(request.getNeutered()));
        pet.setMicrochipped(Boolean.TRUE.equals(request.getMicrochipped()));
        pet.setHealthChecked(Boolean.TRUE.equals(request.getHealthChecked()));
    }

    private String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), uploadPath.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/pets/" + filename;
        } catch (IOException e) {
            return null;
        }
    }

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
        map.put("adminNote", pet.getAdminNote() != null ? pet.getAdminNote() : "");
        map.put("description", pet.getDescription() != null ? pet.getDescription() : "");
        map.put("owner", Map.of("id", pet.getOwner().getId(), "fullName", pet.getOwner().getFullName()));
        long pendingCount = adoptionRequestRepository
                .findByPetAndStatus(pet, AdoptionRequest.RequestStatus.PENDING).size();
        map.put("requestCount", pendingCount);
        map.put("createdAt", pet.getCreatedAt().toString());
        return map;
    }

    private Map<String, Object> toPetDetail(Pet pet) {
        Map<String, Object> map = new LinkedHashMap<>(toPetSummary(pet));
        map.put("latitude", pet.getLatitude());
        map.put("longitude", pet.getLongitude());
        map.put("characteristics", pet.getCharacteristics() != null ? pet.getCharacteristics() : List.of());
        map.put("vaccinated", Boolean.TRUE.equals(pet.getVaccinated()));
        map.put("neutered", Boolean.TRUE.equals(pet.getNeutered()));
        map.put("microchipped", Boolean.TRUE.equals(pet.getMicrochipped()));
        map.put("healthChecked", Boolean.TRUE.equals(pet.getHealthChecked()));
        return map;
    }

    private AuthResponse errorResponse(String code, String message) {
        return AuthResponse.builder()
                .success(false)
                .error(Map.of("code", code, "message", message))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse getPetsUnderReview(String email) {
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN)
            return errorResponse("AUTH-003", "Access denied.");
        List<Pet> pets = petRepository.findByStatus(Pet.PetStatus.UNDER_REVIEW);
        List<Map<String, Object>> petList = pets.stream().map(this::toPetSummary).toList();
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("pets", petList))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse getAllPetsAdmin(String email) {
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN)
            return errorResponse("AUTH-003", "Access denied.");
        List<Pet> pets = petRepository.findAll();
        List<Map<String, Object>> petList = pets.stream().map(this::toPetSummary).toList();
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("pets", petList))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse approveListing(String email, Long petId) {
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN)
            return errorResponse("AUTH-003", "Access denied.");
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) return errorResponse("DB-001", "Pet not found.");
        pet.setStatus(Pet.PetStatus.AVAILABLE);
        pet.setAdminNote(null);
        petRepository.save(pet);
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Listing approved.", "petId", petId))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse rejectListing(String email, Long petId, String reason) {
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN)
            return errorResponse("AUTH-003", "Access denied.");
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) return errorResponse("DB-001", "Pet not found.");
        pet.setStatus(Pet.PetStatus.REJECTED);
        pet.setAdminNote(reason);
        petRepository.save(pet);
        return AuthResponse.builder()
                .success(true)
                .data(Map.of("message", "Listing rejected.", "petId", petId))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}