package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {

    Optional<ApplicationUser> findApplicationUserByEmail(String email);

    Optional<ApplicationUser> findApplicationUserById(Long id);

    @Query("SELECT u FROM ApplicationUser u where u.isDeleted = false")
    Page<ApplicationUser> findAllUsers(Pageable pageable);

    @Query("SELECT u FROM ApplicationUser u WHERE u.isLocked = true and u.isDeleted = false")
    Page<ApplicationUser> findAllLockedUsers(Pageable pageable);
}
