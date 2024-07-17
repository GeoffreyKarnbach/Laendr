package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationForLenderSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationTagCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.LocationSortingCriterion;

public interface LocationService {

    /**
     * Finds all locations that contain the given string in either their name or their owner's name,
     * sorted by the given criterion.
     *
     * @param name     String to search for
     * @param page     Page to request
     * @param pageSize Size of the page to request
     * @param sortingCriterion Criterion to sort results by
     * @return Found locations as pageable
     */
    PageableDto<LocationDto> searchByName(String name, int page, int pageSize, LocationSortingCriterion sortingCriterion);

    /**
     * Finds all locations that match the given Filter-params,
     * sorted by the given criterion.
     *
     * @param locationFilterDto     Params to search for
     * @param page     Page to request
     * @param pageSize Size of the page to request
     * @param sortingCriterion Criterion to sort results by
     * @return Found locations as pageable
     */
    PageableDto<LocationDto> filter(LocationFilterDto locationFilterDto, int page, int pageSize, LocationSortingCriterion sortingCriterion);

    /**
     * Finds all locations that are owned by a lender with the given id,
     * sorted by a given criterion.
     *
     * @param searchDto Includes id of lender, paging information,
     *                  flag whether the include removed locations in the result
     *                  and a sorting criterion for the results.
     * @return  Found locations as pageable
     */
    PageableDto<LocationDto> searchByLender(LocationForLenderSearchDto searchDto);


    /**
     * Creates a new location.
     *
     * @param locationDto Location to create
     * @param user        User creating the location
     * @return Created location with id
     */
    LocationDto createLocation(LocationCreationDto locationDto, String user);

    /**
     * Returns the location with the given id.
     *
     * @param id ID of the location to return
     * @param user User requesting the location
     * @return LocationDto Location with the given id
     */
    LocationDto getLocationById(Long id, String user);

    /**
     * Updates the location with the given id (only the owner can update the location).
     *
     * @param id ID of the location to update
     * @param locationDto Location to update
     * @param user User updating the location
     * @return LocationDto Location with the given id
     */
    LocationDto updateLocation(Long id, String user, LocationDto locationDto);

    /**
     * Removes the location from the system (only hides location to the outside).
     * Action can only be performed by the owner.
     *
     * @param id ID of the location to remove
     * @param user User updating the location
     * @return LocationDto The removed location
     */
    LocationDto removeLocation(Long id, String user);

    /**
     * Removes all locations that are not deleted and that belong to the given user.
     *
     * @param email email address of given user
     */
    void removeAllExistingLocationsForLender(String email);

    /**
     * Returns all tags that are currently existing in the database.
     *
     * @return LocationTagCollectionDto Collection of all tags
     */
    LocationTagCollectionDto getAllTags();
}
