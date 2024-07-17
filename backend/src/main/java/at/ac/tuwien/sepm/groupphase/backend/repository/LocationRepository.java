package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long>, LocationRepositoryCustom {

    /**
     * Find the user email of a location owner.
     *
     * @param locationId location associated with the owner
     * @return the user email of the owner
     */
    @Query("select l.owner.owner.email from Location l where l.id = :locId ")
    Optional<String> findOwnerEmailById(@Param("locId") Long locationId);

    /**
     * Finds all Location entities that contain the given string in either their name or their owner's name.
     *
     * @param search   String to search in names
     * @param pageable Page request to limit work load
     * @return Requested page of the found locations
     */
    @Query("select l from Location l join l.owner.owner o where upper(l.name) like concat('%', upper(:search), '%') or upper(o.name) like concat('%', upper(:search), '%') and removed = false")
    Page<Location> findAllByNameOrOwnerNameContaining(@Param("search") String search, Pageable pageable);

    /**
     * Finds all Location entities that are not deleted and belong to the given user.
     *
     * @param userEmail email address of given user
     * @return stream of matching Locations
     */
    @Query("select l from Location l where l.removed = false and :userEmail = l.owner.owner.email")
    Stream<Location> findAllExistingForLender(@Param("userEmail") String userEmail);
}
