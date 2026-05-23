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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
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

    private Map<String, Object> getGoogleUserInfo(String accessToken) throws Exception {
        URL url = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Failed to verify Google token, status: " + status);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(sb.toString(), Map.class);
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