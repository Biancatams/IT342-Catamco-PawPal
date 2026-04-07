package edu.cit.catamco.pawpal.observer;

import edu.cit.catamco.pawpal.entity.AdoptionRequest;
import edu.cit.catamco.pawpal.entity.Pet;
import edu.cit.catamco.pawpal.repository.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.repository.PetRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdoptionApprovalHandler implements AdoptionEventListener {

    @Override
    public void onAdoptionApproved(
            AdoptionRequest approvedRequest,
            AdoptionRequestRepository requestRepository,
            PetRepository petRepository) {

        // 1. Mark the pet as ADOPTED
        Pet pet = approvedRequest.getPet();
        pet.setStatus(Pet.PetStatus.ADOPTED);
        petRepository.save(pet);

        // 2. Auto-decline all other pending requests for this pet
        List<AdoptionRequest> others = requestRepository.findByPetAndStatus(
                pet, AdoptionRequest.RequestStatus.PENDING);

        for (AdoptionRequest other : others) {
            other.setStatus(AdoptionRequest.RequestStatus.DECLINED);
            other.setDeclineReason("Another adopter was selected for this pet.");
            requestRepository.save(other);
        }
    }
}
