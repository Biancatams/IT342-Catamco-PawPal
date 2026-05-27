package edu.cit.catamco.pawpal.features.adoption;

import edu.cit.catamco.pawpal.features.adoption.AdoptionRequest;
import edu.cit.catamco.pawpal.features.pets.Pet;
import edu.cit.catamco.pawpal.features.adoption.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.features.pets.PetRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdoptionApprovalHandler implements AdoptionEventListener {

    @Override
    public void onAdoptionApproved(
            AdoptionRequest approvedRequest,
            AdoptionRequestRepository requestRepository,
            PetRepository petRepository) {

        Pet pet = approvedRequest.getPet();
        pet.setStatus(Pet.PetStatus.ADOPTED);
        petRepository.save(pet);

        List<AdoptionRequest> others = requestRepository.findByPetAndStatus(
                pet, AdoptionRequest.RequestStatus.PENDING);

        for (AdoptionRequest other : others) {
            other.setStatus(AdoptionRequest.RequestStatus.DECLINED);
            other.setDeclineReason("Another adopter was selected for this pet.");
            requestRepository.save(other);
        }
    }
}
