package edu.cit.catamco.pawpal.features.auth;

import edu.cit.catamco.pawpal.features.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}