package edu.cit.catamco.pawpal.features.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.catamco.pawpal.dto.AuthResponse;
import edu.cit.catamco.pawpal.dto.RegisterRequest;
import edu.cit.catamco.pawpal.dto.LoginRequest;
import edu.cit.catamco.pawpal.dto.GoogleAuthRequest;
import edu.cit.catamco.pawpal.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    private Map<String, Object> getGoogleUserInfo(String token) throws Exception {
        System.out.println("GETTING GOOGLE USER INFO, token starts with: " + token.substring(0, Math.min(30, token.length())));

        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String padded = parts[1];
                while (padded.length() % 4 != 0) padded += "=";
                byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(padded);
                String payload = new String(decodedBytes);
                System.out.println("JWT PAYLOAD: " + payload);
                Map<String, Object> claims = new ObjectMapper().readValue(payload, Map.class);
                String email = (String) claims.get("sub");
                if (email != null && email.contains("@")) {
                    System.out.println("JWT EMAIL FOUND: " + email);
                    return Map.of("email", email, "name", email);
                }
            }
        } catch (Exception e) {
            System.out.println("JWT decode failed: " + e.getMessage());
        }

        try {
            URL url = new URL("https://oauth2.googleapis.com/tokeninfo?id_token=" + token);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.println("TOKENINFO RESPONSE CODE: " + responseCode);
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                return new ObjectMapper().readValue(sb.toString(), Map.class);
            }
        } catch (Exception e) {
            System.out.println("TOKENINFO FAILED: " + e.getMessage());
        }

        // Fallback: userinfo endpoint (web access tokens)
        try {
            URL url = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                return new ObjectMapper().readValue(sb.toString(), Map.class);
            }
        } catch (Exception e) {
            System.out.println("USERINFO FAILED: " + e.getMessage());
        }

        throw new RuntimeException("Could not verify Google token");
    }

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "VALID-001",
                            "message", "Passwords do not match"))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "DB-002",
                            "message", "Email already registered"))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(request.getRole().toUpperCase()))
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .data(Map.of(
                        "user", Map.of(
                                "id", user.getId(),
                                "fullName", user.getFullName(),
                                "email", user.getEmail(),
                                "role", user.getRole()
                        ),
                        "accessToken", token
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "AUTH-001",
                            "message", "Invalid email or password"))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .success(true)
                .data(Map.of(
                        "user", Map.of(
                                "id", user.getId(),
                                "fullName", user.getFullName(),
                                "email", user.getEmail(),
                                "role", user.getRole(),
                                "isBanned", user.isBanned()
                        ),
                        "accessToken", token
                ))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public AuthResponse googleRegister(GoogleAuthRequest request) {
        try {
            Map<String, Object> userInfo = getGoogleUserInfo(request.getToken());

            String email = (String) userInfo.get("email");
            String fullName = (String) userInfo.get("name");

            if (email == null) {
                return AuthResponse.builder()
                        .success(false)
                        .error(Map.of("code", "AUTH-002",
                                "message", "Could not retrieve email from Google."))
                        .timestamp(LocalDateTime.now().toString())
                        .build();
            }

            if (userRepository.existsByEmail(email)) {
                return AuthResponse.builder()
                        .success(false)
                        .error(Map.of("code", "DB-002",
                                "message", "Email already registered. Please login instead."))
                        .timestamp(LocalDateTime.now().toString())
                        .build();
            }

            if (request.getRole() == null || request.getRole().isBlank()) {
                return AuthResponse.builder()
                        .success(false)
                        .error(Map.of("code", "VALID-002",
                                "message", "Please select a role."))
                        .timestamp(LocalDateTime.now().toString())
                        .build();
            }

            User user = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(null)
                    .role(User.Role.valueOf(request.getRole().toUpperCase()))
                    .build();

            userRepository.save(user);
            String token = jwtUtil.generateToken(user.getEmail());

            return AuthResponse.builder()
                    .success(true)
                    .data(Map.of(
                            "user", Map.of(
                                    "id", user.getId(),
                                    "fullName", user.getFullName(),
                                    "email", user.getEmail(),
                                    "role", user.getRole()
                            ),
                            "accessToken", token
                    ))
                    .timestamp(LocalDateTime.now().toString())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "AUTH-003",
                            "message", "Google authentication failed."))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }
    }

    public AuthResponse googleLogin(GoogleAuthRequest request) {
        try {
            Map<String, Object> userInfo = getGoogleUserInfo(request.getToken());

            String email = (String) userInfo.get("email");

            if (email == null) {
                return AuthResponse.builder()
                        .success(false)
                        .error(Map.of("code", "AUTH-002",
                                "message", "Could not retrieve email from Google."))
                        .timestamp(LocalDateTime.now().toString())
                        .build();
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return AuthResponse.builder()
                        .success(false)
                        .error(Map.of("code", "AUTH-004",
                                "message", "No account found. Please register first."))
                        .timestamp(LocalDateTime.now().toString())
                        .build();
            }

            String token = jwtUtil.generateToken(user.getEmail());

            return AuthResponse.builder()
                    .success(true)
                    .data(Map.of(
                            "user", Map.of(
                                    "id", user.getId(),
                                    "fullName", user.getFullName(),
                                    "email", user.getEmail(),
                                    "role", user.getRole(),
                                    "isBanned", user.isBanned()
                            ),
                            "accessToken", token
                    ))
                    .timestamp(LocalDateTime.now().toString())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return AuthResponse.builder()
                    .success(false)
                    .error(Map.of("code", "AUTH-003",
                            "message", "Google authentication failed."))
                    .timestamp(LocalDateTime.now().toString())
                    .build();
        }
    }
}