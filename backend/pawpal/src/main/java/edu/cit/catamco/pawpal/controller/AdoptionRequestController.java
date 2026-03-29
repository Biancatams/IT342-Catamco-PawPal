package edu.cit.catamco.pawpal.controller;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.service.AdoptionRequestService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/adoption-requests")
@CrossOrigin(origins = "*")
public class AdoptionRequestController {

    private final AdoptionRequestService service;

    public AdoptionRequestController(AdoptionRequestService service) {
        this.service = service;
    }

    // POST /api/v1/adoption-requests — Submit a request
    @PostMapping
    public ResponseEntity<AuthResponse> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        AuthResponse res = service.submitRequest(userDetails.getUsername(), body);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }

    // GET /api/v1/adoption-requests/pet/{petId} — Get requests for a pet
    @GetMapping("/pet/{petId}")
    public ResponseEntity<AuthResponse> getForPet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long petId) {
        AuthResponse res = service.getRequestsForPet(userDetails.getUsername(), petId);
        return ResponseEntity.ok(res);
    }

    // GET /api/v1/adoption-requests/my — Get my requests (adopter)
    @GetMapping("/my")
    public ResponseEntity<AuthResponse> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse res = service.getMyRequests(userDetails.getUsername());
        return ResponseEntity.ok(res);
    }

    // PUT /api/v1/adoption-requests/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<AuthResponse> approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AuthResponse res = service.approveRequest(userDetails.getUsername(), id);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(res);
    }

    // PUT /api/v1/adoption-requests/{id}/decline
    @PutMapping("/{id}/decline")
    public ResponseEntity<AuthResponse> decline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AuthResponse res = service.declineRequest(userDetails.getUsername(), id);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(res);
    }
}