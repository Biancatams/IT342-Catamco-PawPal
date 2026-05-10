package edu.cit.catamco.pawpal.features.auth;

import edu.cit.catamco.pawpal.dto.*;
import edu.cit.catamco.pawpal.features.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<edu.cit.catamco.pawpal.dto.AuthResponse> register(
            @Valid @RequestBody edu.cit.catamco.pawpal.dto.RegisterRequest request) {
            edu.cit.catamco.pawpal.dto.AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(response.isSuccess() ?
                        HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<edu.cit.catamco.pawpal.dto.AuthResponse> login(
            @Valid @RequestBody edu.cit.catamco.pawpal.dto.LoginRequest request) {
        edu.cit.catamco.pawpal.dto.AuthResponse response = authService.login(request);
        return ResponseEntity
                .status(response.isSuccess() ?
                        HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @PostMapping("/google-register")
    public ResponseEntity<edu.cit.catamco.pawpal.dto.AuthResponse> googleRegister(
            @Valid @RequestBody edu.cit.catamco.pawpal.dto.GoogleAuthRequest request) {
        edu.cit.catamco.pawpal.dto.AuthResponse response = authService.googleRegister(request);
        return ResponseEntity
                .status(response.isSuccess() ?
                        HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PostMapping("/google-login")
    public ResponseEntity<edu.cit.catamco.pawpal.dto.AuthResponse> googleLogin(
            @Valid @RequestBody edu.cit.catamco.pawpal.dto.GoogleAuthRequest request) {
        edu.cit.catamco.pawpal.dto.AuthResponse response = authService.googleLogin(request);
        return ResponseEntity
                .status(response.isSuccess() ?
                        HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}