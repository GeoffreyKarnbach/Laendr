package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCountDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.AccessForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;

public interface ReviewService {

    /**
     * Search for reviews of the location. Returned reviews are defined by the page parameters.
     *
     * @param locationId ID of the location to get reviews
     * @param page       The page of the request
     * @param pageSize   The amount of reviews in a page
     * @return Reviews of the location stored in a page
     */
    PageableDto<ReviewDto> getAllByLocationId(long locationId, int page, int pageSize);

    /**
     * Creates a new review for a location and updates reputation for location and owner of location.
     *
     * @param reviewCreationDto the rating, comment and associated transaction for the new review
     * @return the created review
     * @throws NotFoundException        if transaction for given id is not found
     * @throws AccessForbiddenException if calling user is not the renter in the transaction
     * @throws ConflictException        if a review for location in context of transaction already exists
     */
    ReviewDto createLocationReview(ReviewCreationDto reviewCreationDto);

    /**
     * Creates a new review for a renter and updates reputation for the renter.
     *
     * @param reviewCreationDto the rating, comment and associated transaction for the new review
     * @return the created review
     * @throws NotFoundException        if transaction for given id is not found
     * @throws AccessForbiddenException if calling user is not the lender in the transaction
     * @throws ConflictException        if a review for renter in context of transaction already exists
     */
    ReviewDto createRenterReview(ReviewCreationDto reviewCreationDto);

    /**
     * Counts the amount of reviews the user has written and the amount of transactions written by the user.
     * The user needs to be logged in for this function.
     *
     * @return a dto with both the count of reviews and completed transactions
     */
    ReviewCountDto getReviewCount();
}
