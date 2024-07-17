package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;

import java.util.List;

public interface TimeslotService {
    /**
     * Find all timeslots of the given location, which start on the given day.
     * If the given day is today, only timeslots starting today after now are returned.
     * If the callerIsLocationOwner is set to true and the requesting user really is the location owner,
     * the returned timeslots include whether a renter has requested them yet.
     *
     * @param searchDto location, day for which timeslots should be found and whether the calling user is the owner of the location
     * @return all timeslots for the given location and day
     * @throws ConflictException if the given location does not exist
     *                           or the user requests the requested status but is not the location owner
     * @throws ValidationException if the given search day is in the past
     */
    List<TimeslotDto> getTimeslotsForLocationStartingOnCertainDay(TimeslotSearchDto searchDto);

    /**
     * Sets the deleted flag of a timeslot if it exists and the user is the owner of the associated location.
     *
     * @param timeslotId timeslot to be deleted
     * @throws NotFoundException if the timeslot does not exist
     * @throws ConflictException if the calling user is not owner of the associated location
     */
    void deleteTimeslot(Long timeslotId);

    /**
     * Update a timeslot with the given values,
     * if the user is the owner of location associated with the timeslot.
     *
     * @param updatedTimeslot timeslot with updated values
     * @return persisted and updated timeslot
     * @throws NotFoundException if the timeslot does not exist
     * @throws ValidationException if the passed values for the timeslot are not allowed
     * @throws ConflictException if the calling user is not owner of the associated location,
     *                           or the state of the timeslot does not allow updates to it
     */
    TimeslotDto updateTimeslot(TimeslotDto updatedTimeslot);

    /**
     * Create a new timeslot for a location.
     *
     * @param timeslotDto the new timeslot to create
     * @return the new persisted timeslot
     *  @throws ValidationException if the passed values for the timeslot are not allowed
     *  @throws ConflictException if the calling user is not owner of the associated location
     */
    TimeslotDto createTimeslot(TimeslotDto timeslotDto);
}
