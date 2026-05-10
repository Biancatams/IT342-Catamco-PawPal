package edu.cit.catamco.pawpal.features.verification;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/verification")
@CrossOrigin(origins = "*")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "location", required = false) String location,
            @RequestPart(value = "idImage") MultipartFile idImage) {
        AuthResponse res = verificationService.submitVerification(
                userDetails.getUsername(), reason, idImage, fullName, phoneNumber, location);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(res);
    }

    @GetMapping("/my")
    public ResponseEntity<AuthResponse> getMy(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse res = verificationService.getMyVerification(userDetails.getUsername());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/all")
    public ResponseEntity<AuthResponse> getAll() {
        AuthResponse res = verificationService.getAllVerifications();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<AuthResponse> approve(@PathVariable Long id) {
        AuthResponse res = verificationService.approveVerification(id);
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND).body(res);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<AuthResponse> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        AuthResponse res = verificationService.rejectVerification(id, body.get("adminComment"));
        return ResponseEntity.status(res.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND).body(res);
    }
}