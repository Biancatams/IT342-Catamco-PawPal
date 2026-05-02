package edu.cit.catamco.pawpal.features.pets;

import edu.cit.catamco.pawpal.features.pets.Pet;
import edu.cit.catamco.pawpal.features.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwner(User owner);
    List<Pet> findByStatus(Pet.PetStatus status);
    @Query("SELECT p FROM Pet p WHERE p.status = :status AND CAST(p.type AS string) LIKE %:type%")
    List<Pet> findByStatusAndType(@Param("status") Pet.PetStatus status, @Param("type") String type);
}
