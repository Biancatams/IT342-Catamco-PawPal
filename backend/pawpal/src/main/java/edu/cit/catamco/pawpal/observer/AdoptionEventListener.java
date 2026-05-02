package edu.cit.catamco.pawpal.observer;

import edu.cit.catamco.pawpal.entity.AdoptionRequest;
import edu.cit.catamco.pawpal.repository.AdoptionRequestRepository;
import edu.cit.catamco.pawpal.repository.PetRepository;

public interface AdoptionEventListener {
    void onAdoptionApproved(
            AdoptionRequest approvedRequest,
            AdoptionRequestRepository requestRepository,
            PetRepository petRepository
    );
}
