package edu.cit.catamco.pawpal.features.adoption;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdoptionRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdoptionFacade adoptionFacade;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse successResponse;
    private AuthResponse failResponse;

    @BeforeEach
    void setUp() {
        successResponse = AuthResponse.builder()
                .success(true)
                .data("Success")
                .build();

        failResponse = AuthResponse.builder()
                .success(false)
                .error("Failed")
                .build();
    }

    // Test 1: Submit adoption request - POST /api/v1/adoption-requests
    @Test
    @WithMockUser(username = "adopter@test.com", roles = {"ADOPTER"})
    public void testSubmitAdoptionRequest_Success() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("petId", "1");
        body.put("message", "I want to adopt this pet.");

        Mockito.when(adoptionFacade.submitRequest(Mockito.anyString(), Mockito.anyMap()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/adoption-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    // Test 2: Get my adoption requests - GET /api/v1/adoption-requests/my
    @Test
    @WithMockUser(username = "adopter@test.com", roles = {"ADOPTER"})
    public void testGetMyRequests_Success() throws Exception {
        Mockito.when(adoptionFacade.getMyRequests(Mockito.anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/adoption-requests/my"))
                .andExpect(status().isOk());
    }

    // Test 3: Get requests for a specific pet - GET /api/v1/adoption-requests/pet/{petId}
    @Test
    @WithMockUser(username = "owner@test.com", roles = {"PET_OWNER"})
    public void testGetRequestsForPet_Success() throws Exception {
        Mockito.when(adoptionFacade.getRequestsForPet(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/adoption-requests/pet/1"))
                .andExpect(status().isOk());
    }

    // Test 4: Approve adoption request - PUT /api/v1/adoption-requests/{id}/approve
    @Test
    @WithMockUser(username = "owner@test.com", roles = {"PET_OWNER"})
    public void testApproveRequest_Success() throws Exception {
        Mockito.when(adoptionFacade.approveRequest(Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(successResponse);

        mockMvc.perform(put("/api/v1/adoption-requests/1/approve"))
                .andExpect(status().isOk());
    }

    // Test 5: Decline adoption request - PUT /api/v1/adoption-requests/{id}/decline
    @Test
    @WithMockUser(username = "owner@test.com", roles = {"PET_OWNER"})
    public void testDeclineRequest_Success() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("reason", "Already adopted.");

        Mockito.when(adoptionFacade.declineRequest(Mockito.anyString(), Mockito.anyLong(), Mockito.anyMap()))
                .thenReturn(successResponse);

        mockMvc.perform(put("/api/v1/adoption-requests/1/decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}
