package edu.cit.catamco.pawpal.features.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.catamco.pawpal.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse successResponse;
    private AuthResponse failResponse;

    @BeforeEach
    void setUp() {
        successResponse = AuthResponse.builder()
                .success(true)
                .data("OK")
                .build();

        failResponse = AuthResponse.builder()
                .success(false)
                .error("Failed")
                .build();
    }

    // Test 1: Successful registration - POST /api/v1/auth/register
    @Test
    public void testRegisterSuccess() throws Exception {
        Mockito.when(authService.register(Mockito.any()))
                .thenReturn(successResponse);

        Map<String, String> body = new HashMap<>();
        body.put("fullName", "Test User");
        body.put("email", "newuser@pawpal.com");
        body.put("password", "Test@1234");
        body.put("confirmPassword", "Test@1234");
        body.put("role", "ADOPTER");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    // Test 2: Register with duplicate email - POST /api/v1/auth/register
    @Test
    public void testRegisterDuplicateEmail() throws Exception {
        Mockito.when(authService.register(Mockito.any()))
                .thenReturn(failResponse);

        Map<String, String> body = new HashMap<>();
        body.put("fullName", "Duplicate User");
        body.put("email", "existing@pawpal.com");
        body.put("password", "Test@1234");
        body.put("confirmPassword", "Test@1234");
        body.put("role", "ADOPTER");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // Test 3: Successful login - POST /api/v1/auth/login
    @Test
    public void testLoginSuccess() throws Exception {
        Mockito.when(authService.login(Mockito.any()))
                .thenReturn(successResponse);

        Map<String, String> body = new HashMap<>();
        body.put("email", "user@pawpal.com");
        body.put("password", "Test@1234");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    // Test 4: Login with wrong password - POST /api/v1/auth/login
    @Test
    public void testLoginWrongPassword() throws Exception {
        Mockito.when(authService.login(Mockito.any()))
                .thenReturn(failResponse);

        Map<String, String> body = new HashMap<>();
        body.put("email", "user@pawpal.com");
        body.put("password", "WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // Test 5: Login with non-existent user - POST /api/v1/auth/login
    @Test
    public void testLoginNonExistentUser() throws Exception {
        Mockito.when(authService.login(Mockito.any()))
                .thenReturn(failResponse);

        Map<String, String> body = new HashMap<>();
        body.put("email", "ghost@pawpal.com");
        body.put("password", "Test@1234");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
