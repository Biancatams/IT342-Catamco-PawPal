package edu.cit.catamco.pawpal.facade;

import edu.cit.catamco.pawpal.dto.AuthResponse;
import java.util.Map;

public interface AdoptionFacade {
    AuthResponse submitRequest(String adopterEmail, Map<String, String> body);
    AuthResponse getRequestsForPet(String ownerEmail, Long petId);
    AuthResponse getMyRequests(String adopterEmail);
    AuthResponse approveRequest(String ownerEmail, Long requestId);
    AuthResponse declineRequest(String ownerEmail, Long requestId, Map<String, String> body);
}
