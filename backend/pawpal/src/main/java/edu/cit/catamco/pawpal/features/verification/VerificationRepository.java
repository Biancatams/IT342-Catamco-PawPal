package edu.cit.catamco.pawpal.features.verification;

import edu.cit.catamco.pawpal.features.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<VerificationRequest, Long> {
    List<VerificationRequest> findByUserOrderByCreatedAtDesc(User user);
    Optional<VerificationRequest> findTopByUserOrderByCreatedAtDesc(User user);
    List<VerificationRequest> findAllByOrderByCreatedAtDesc();
    List<VerificationRequest> findByStatusOrderByCreatedAtDesc(VerificationRequest.Status status);
}