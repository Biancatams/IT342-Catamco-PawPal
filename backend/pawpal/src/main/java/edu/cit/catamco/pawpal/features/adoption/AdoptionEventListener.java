package edu.cit.catamco.pawpal.features.adoption;

import edu.cit.catamco.pawpal.features.adoption.AdoptionRequest;
import edu.cit.catamco.pawpal.features.adoption.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.features.pets.PetRepository;

public interface AdoptionEventListener {
    void onAdoptionApproved(
            AdoptionRequest approvedRequest,
            AdoptionRequestRepository requestRepository,
            PetRepository petRepository
    );
}
