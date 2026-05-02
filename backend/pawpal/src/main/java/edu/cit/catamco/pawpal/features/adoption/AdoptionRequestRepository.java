package edu.cit.catamco.pawpal.features.adoption;

import edu.cit.catamco.pawpal.features.adoption.AdoptionRequest;
import edu.cit.catamco.pawpal.features.pets.Pet;
import edu.cit.catamco.pawpal.features.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {
    List<AdoptionRequest> findByPet(Pet pet);
    List<AdoptionRequest> findByAdopter(User adopter);
    boolean existsByPetAndAdopterAndStatus(Pet pet, User adopter, AdoptionRequest.RequestStatus status);
    List<AdoptionRequest> findByPetAndStatus(Pet pet, AdoptionRequest.RequestStatus status);
}