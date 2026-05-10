package edu.cit.catamco.pawpal.features.pets;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.catamco.pawpal.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetService petService;

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
                .error("Not found")
                .build();
    }

    // Test 1: Get all pets authenticated - GET /api/v1/pets
    @Test
    @WithMockUser(username = "user@pawpal.com", roles = {"ADOPTER"})
    public void testGetAllPetsPublic() throws Exception {
        Mockito.when(petService.getAllPets())
                .thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/pets"))
                .andExpect(status().isOk());
    }

    // Test 2: Get all pets unauthenticated - should return 403
    @Test
    public void testGetAllPetsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/pets"))
                .andExpect(status().isForbidden());
    }

    // Test 3: Get pet by ID (exists) - GET /api/v1/pets/{id}
    @Test
    @WithMockUser(username = "user@pawpal.com", roles = {"ADOPTER"})
    public void testGetPetByIdExists() throws Exception {
        Mockito.when(petService.getPetById(Mockito.anyLong()))
                .thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/pets/1"))
                .andExpect(status().isOk());
    }

    // Test 4: Get pet by ID (not found) - GET /api/v1/pets/{id}
    @Test
    @WithMockUser(username = "user@pawpal.com", roles = {"ADOPTER"})
    public void testGetPetByIdNotFound() throws Exception {
        Mockito.when(petService.getPetById(Mockito.anyLong()))
                .thenReturn(failResponse);

        mockMvc.perform(get("/api/v1/pets/99999"))
                .andExpect(status().isNotFound());
    }

    // Test 5: Get my pets as owner - GET /api/v1/pets/my
    @Test
    @WithMockUser(username = "owner@pawpal.com", roles = {"PET_OWNER"})
    public void testGetMyPetsAuthenticated() throws Exception {
        Mockito.when(petService.getMyPets(Mockito.anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/pets/my"))
                .andExpect(status().isOk());
    }
}
