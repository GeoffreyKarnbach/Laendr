package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom Repository for Location.
 */
public interface LocationRepositoryCustom {

    /**
     * Finds all Location entities that match the filter criteria.
     *
     * @param filterDto Parameters to search in query
     * @param pageable  Page request to limit work load
     * @return Requested page of the found locations
     */
    Page<Location> findAllByFilterParams(LocationFilterDto filterDto, Pageable pageable);

    /**
     * Finds all Location entities that match the filter criteria.
     *
     * <p>Specifically enables sorting according to price, which requires more complex query handling.
     *
     * @param filterDto Parameters to search in query
     * @param pageable  Page request to limit work load
     * @return Requested page of the found locations
     */
    Page<Location> findAllByFilterParamsSortByPrice(LocationFilterDto filterDto, Pageable pageable);

    /**
     * Finds all locations for an owner. To be used when not sorting by price criteria.
     * When sorting by price criteria, use findAllByOwnerIdSortByPrice instead.
     *
     * @param ownerId id of the owner for which the locations are to be found
     * @param includeRemoved whether to also return locations not currently listed
     * @param pageable page request to limit work load
     * @return requested page of the found locations
     */
    Page<Location> findAllByOwnerId(Long ownerId, boolean includeRemoved, Pageable pageable);

    /**
     * Finds all locations for an owner. To be used when sorting by price criteria.
     * When not sorting by price criteria, use findAllByOwnerId instead.
     *
     * @param ownerId id of the owner for which the locations are to be found
     * @param includeRemoved whether to also return locations not currently listed
     * @param pageable page request to limit work load
     * @return requested page of the found locations
     */
    Page<Location> findAllByOwnerIdSortByPrice(Long ownerId, boolean includeRemoved, Pageable pageable);
}
