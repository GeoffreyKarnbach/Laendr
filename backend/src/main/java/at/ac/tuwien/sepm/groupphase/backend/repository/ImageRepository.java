package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Find all images belonging to a location.
     *
     * @param locationId the id of the location
     * @return a list of all images belonging to the location
     */
    List<Image> findByLocationId(Long locationId);

    /**
     * Find all images belonging to a location ordered by position.
     *
     * @param locationId the id of the location
     * @return a list of all images belonging to the location ordered by position
     */
    List<Image> findByLocationIdOrderByPositionAsc(Long locationId);
}
