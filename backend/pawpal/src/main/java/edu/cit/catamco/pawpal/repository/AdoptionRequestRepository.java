package edu.cit.catamco.pawpal.repository;

import edu.cit.catamco.pawpal.entity.AdoptionRequest;
import edu.cit.catamco.pawpal.entity.Pet;
import edu.cit.catamco.pawpal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {
    List<AdoptionRequest> findByPet(Pet pet);
    List<AdoptionRequest> findByAdopter(User adopter);
    boolean existsByPetAndAdopterAndStatus(Pet pet, User adopter, AdoptionRequest.RequestStatus status);
    List<AdoptionRequest> findByPetAndStatus(Pet pet, AdoptionRequest.RequestStatus status);
}