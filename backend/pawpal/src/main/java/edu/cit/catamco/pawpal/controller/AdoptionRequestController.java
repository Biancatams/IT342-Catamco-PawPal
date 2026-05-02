package edu.cit.catamco.pawpal.controller;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.facade.AdoptionFacade;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/adoption-requests")
@CrossOrigin(origins = "*")
public class AdoptionRequestController {

    private final AdoptionFacade adoptionFacade;

    public AdoptionRequestController(AdoptionFacade adoptionFacade) {
        this.adoptionFacade = adoptionFacade;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        AuthResponse res = adoptionFacade.submitRequest(userDetails.getUsername(), body);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<AuthResponse> getForPet(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long petId) {
        AuthResponse res = adoptionFacade.getRequestsForPet(userDetails.getUsername(), petId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/my")
    public ResponseEntity<AuthResponse> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse res = adoptionFacade.getMyRequests(userDetails.getUsername());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<AuthResponse> approve(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AuthResponse res = adoptionFacade.approveRequest(userDetails.getUsername(), id);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(res);
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<AuthResponse> decline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        AuthResponse res = adoptionFacade.declineRequest(userDetails.getUsername(), id, body);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(res);
    }
}
