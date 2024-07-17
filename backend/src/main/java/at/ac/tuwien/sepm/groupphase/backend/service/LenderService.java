package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.LenderViewDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;

public interface LenderService {

    /**
     * Searches for a Lender by the given id.
     *
     * @param id                    The unique identifier of the lender
     * @return                      A Dto of the lender containing all values for a detailed view
     * @throws NotFoundException    If no lender with the given id was found in the database
     */
    LenderViewDto getById(long id);

    /**
     * Attempts to add the lender role to the user with the given ID.
     *
     * @param userId ID of user to add lender role to
     * @return a new JWT iff the modified user is the same as the active user, otherwise null
     */
    String addLenderRole(long userId);

}
