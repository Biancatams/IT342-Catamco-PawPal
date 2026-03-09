package edu.cit.catamco.pawpal.controller;

import edu.cit.catamco.pawpal.dto.*;
import edu.cit.catamco.pawpal.service.AuthService;
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
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(response.isSuccess() ?
                        HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity
                .status(response.isSuccess() ?
                        HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}