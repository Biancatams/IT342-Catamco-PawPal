package edu.cit.catamco.pawpal.controller;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.service.UserService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/v1/users/me
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse res = userService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(res);
    }

    // PUT /api/v1/users/me — supports optional profile image upload
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Map<String, String> body,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        AuthResponse res = userService.updateMe(userDetails.getUsername(), body, image);
        return ResponseEntity
                .status(res.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(res);
    }
}