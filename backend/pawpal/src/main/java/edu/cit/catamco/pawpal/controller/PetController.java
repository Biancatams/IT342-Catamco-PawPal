package edu.cit.catamco.pawpal.controller;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.dto.PetRequest;
import edu.cit.catamco.pawpal.service.PetService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/pets")
@CrossOrigin(origins = "*")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // POST /api/v1/pets — Post a pet (multipart form)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> createPet(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("data") PetRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        AuthResponse response = petService.createPet(userDetails.getUsername(), request, image);
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // GET /api/v1/pets — Get all available pets
    @GetMapping
    public ResponseEntity<AuthResponse> getAllPets() {
        AuthResponse response = petService.getAllPets();
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/pets/{id} — Get pet by ID
    @GetMapping("/{id}")
    public ResponseEntity<AuthResponse> getPetById(@PathVariable Long id) {
        AuthResponse response = petService.getPetById(id);
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .body(response);
    }

    // GET /api/v1/pets/my — Get owner's own pets
    @GetMapping("/my")
    public ResponseEntity<AuthResponse> getMyPets(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse response = petService.getMyPets(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/pets/{id} — Delete a pet
    @DeleteMapping("/{id}")
    public ResponseEntity<AuthResponse> deletePet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AuthResponse response = petService.deletePet(userDetails.getUsername(), id);
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // PUT /api/v1/pets/{id} — Update a pet
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> updatePet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestPart("data") PetRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        AuthResponse response = petService.updatePet(userDetails.getUsername(), id, request, image);
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // GET /api/v1/pets/admin/under-review
    @GetMapping("/admin/under-review")
    public ResponseEntity<AuthResponse> getPetsUnderReview(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse response = petService.getPetsUnderReview(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/pets/admin/all
    @GetMapping("/admin/all")
    public ResponseEntity<AuthResponse> getAllPetsAdmin(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse response = petService.getAllPetsAdmin(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/pets/admin/{id}/approve
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<AuthResponse> approveListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AuthResponse response = petService.approveListing(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/pets/admin/{id}/reject
    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<AuthResponse> rejectListing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        AuthResponse response = petService.rejectListing(
                userDetails.getUsername(), id, body.getOrDefault("reason", ""));
        return ResponseEntity.ok(response);
    }
}