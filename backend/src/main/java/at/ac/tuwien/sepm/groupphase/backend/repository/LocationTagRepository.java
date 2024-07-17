package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.LocationTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationTagRepository extends JpaRepository<LocationTag, Long> {

    /**
     * Returns a location tag by its name.
     *
     * @param tagName the name of the location tag
     * @return the location tag
     */
    Optional<LocationTag> findByName(String tagName);
}
