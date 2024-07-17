package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDetailDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLender;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationRenter;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;

import java.time.LocalDateTime;

public interface ReputationService {

    /**
     * Updates the reputation for the lender with the given ID.
     * <i>To avoid errors, this function creates a reputation mapping for the lender
     * if it does not already exist. This should <b>not</b> be relied upon.</i>
     *
     * @param lenderId ID of the lender
     * @throws NotFoundException if the given ID does not point to a lender with associated reputation
     */
    void updateReputationForLender(long lenderId) throws NotFoundException;

    /**
     * Performs reputation update calculation for a given lender without updating actual reputation.
     * Returns details for steps of the calculation.
     *
     * @param lenderId ID of the lender
     * @return details of the reputation calculation process
     * @throws NotFoundException if then given ID does not point to a lender with associated reputation
     */
    ReputationDetailDto calculateReputationDetailsForLender(long lenderId) throws NotFoundException;

    /**
     * Updates the reputation for the location with the given ID.
     * <i>To avoid errors, this function creates a reputation mapping for the location
     * if it does not already exist. This should <b>not</b> be relied upon.</i>
     *
     * @param locationId ID of the location
     * @throws NotFoundException if the given ID does not point to a location with associated reputation
     */
    void updateReputationForLocation(long locationId) throws NotFoundException;

    /**
     * Performs reputation update calculation for a given location without updating actual reputation.
     * Returns details for steps of the calculation.
     *
     * @param locationId ID of the location
     * @return details of the reputation calculation process
     * @throws NotFoundException if then given ID does not point to a location with associated reputation
     */
    ReputationDetailDto calculateReputationDetailsForLocation(long locationId) throws NotFoundException;

    /**
     * Updates the reputation for the renter with the given ID.
     * <i>To avoid errors, this function creates a reputation mapping for the renter
     * if it does not already exist. This should <b>not</b> be relied upon.</i>
     *
     * @param renterId ID of the renter
     * @throws NotFoundException if the given ID does not point to a renter with associated reputation
     */
    void updateReputationForRenter(long renterId) throws NotFoundException;

    /**
     * Performs reputation update calculation for a given renter without updating actual reputation.
     * Returns details for steps of the calculation.
     *
     * @param renterId ID of the renter
     * @return details of the reputation calculation process
     * @throws NotFoundException if then given ID does not point to a renter with associated reputation
     */
    ReputationDetailDto calculateReputationDetailsForRenter(long renterId) throws NotFoundException;

    /**
     * Applies time decay to all reputations that were last updated before the cutoff.
     *
     * @param cutoff the cutoff date
     */
    void updateTimeDecayBefore(LocalDateTime cutoff);

    /**
     * Returns the reputation mapping for the given lender,creating it if it does not exist.
     *
     * @param lenderId ID of the given lender
     * @return reputation mapping for the given lender
     * @throws NotFoundException if the given ID does not point to a lender with associated reputation
     */
    ReputationLender getReputationForLender(long lenderId) throws NotFoundException;

    /**
     * Returns the reputation mapping for the given location, creating it if it does not exist.
     *
     * @param locationId ID of the given location
     * @return reputation mapping for the given location
     * @throws NotFoundException if the given ID does not point to a location with associated reputation
     */
    ReputationLocation getReputationForLocation(long locationId) throws NotFoundException;

    /**
     * Returns the reputation mapping for the given renter, creating it if it does not exist.
     *
     * @param renterId ID of the given renter
     * @return reputation mapping for the given renter
     * @throws NotFoundException if the given ID does not point to a renter with associated reputation
     */
    ReputationRenter getReputationForRenter(long renterId) throws NotFoundException;

    /**
     * Applies time decay to the given lender reputation immediately.
     *
     * @param lenderId ID of the lender to apply time decay to
     * @throws NotFoundException if the given ID does not point to a lender with associated reputation
     */
    void updateLenderTimeDecay(long lenderId) throws NotFoundException;

    /**
     * Applies time decay to the given location reputation immediately.
     *
     * @param locationId ID of the location to apply time decay to
     * @throws NotFoundException if the given ID does not point to a location with associated reputation
     */
    void updateLocationTimeDecay(long locationId) throws NotFoundException;

    /**
     * Applies time decay to the given renter reputation immediately.
     *
     * @param renterId ID of the renter to apply time decay to
     * @throws NotFoundException if the given ID does not point to a renter with associated reputation
     */
    void updateRenterTimeDecay(long renterId) throws NotFoundException;

    /**
     * Creates a new ReputationLender entity with neutral values.
     * <b>Does not save the entity in its corresponding repository!</b>
     *
     * @return a new ReputationLender entity
     */
    ReputationLender newLenderReputationEntity();

    /**
     * Creates a new ReputationLocation entity with neutral values.
     * <b>Does not save the entity in its corresponding repository!</b>
     *
     * @return a new ReputationLocation entity
     */
    ReputationLocation newLocationReputationEntity();

    /**
     * Creates a new ReputationRenter entity with neutral values.
     * <b>Does not save the entity in its corresponding repository!</b>
     *
     * @return a new ReputationRenter entity
     */
    ReputationRenter newRenterReputationEntity();

}
