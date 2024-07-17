package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.TimeslotView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {
    /**
     * Find all timeslots of the given location, which start in the given time range.
     *
     * @param owningLocationId location for which timeslots are searched
     * @param startDateTime    start DateTime for which timeslots are searched
     * @param endDateTime      end DateTime for which timeslots are searched
     * @param callingUserEmail email of the user requesting the timeslots
     * @return all timeslots for the given location and time range
     */
    @Query("select t.id as id, t.start as start, t.end as end, t.price as price, t.priceHourly as priceHourly, t.used as used, "
        + "(select count(tr.id) > 0 from Transaction tr where tr.timeslot.id = t.id and (tr.cancelled = false or tr.cancelled = null) and t.used = false) as requested, "
        + "(select count(tr.id) > 0 from Transaction tr where tr.timeslot.id = t.id and (tr.cancelled = false or tr.cancelled = null)"
        + "and tr.renter.owner.email = :userEmail) as requestedByCallingUser from Timeslot t "
        + "where t.owningLocation.id = :locId and start >= :startDateTime and start <= :endDateTime and t.deleted = false order by t.start asc, t.end asc")
    List<TimeslotView> findTimeslotsForLocationAndDay(@Param("locId") Long owningLocationId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime, @Param("userEmail") String callingUserEmail);

    /**
     * Determine if timeslot is requested by a user.
     *
     * @param timeslotId id of timeslot in question
     * @return if the timeslot is requested
     */
    @Query("select count(tr.id) > 0 from Transaction tr where tr.timeslot.id = :id and (tr.cancelled = false or tr.cancelled is null)")
    boolean isTimeslotRequested(@Param("id") Long timeslotId);
}
