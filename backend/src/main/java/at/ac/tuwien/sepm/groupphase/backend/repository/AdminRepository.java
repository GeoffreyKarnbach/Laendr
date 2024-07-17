package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Checks whether an admin exists with the given account email.
     *
     * @param email the email to check
     * @return whether the role assignment exists
     */
    boolean existsByOwnerEmail(String email);

}
