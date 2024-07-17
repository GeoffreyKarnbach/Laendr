package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCountDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewDto;
import at.ac.tuwien.sepm.groupphase.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/reviews")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ReviewEndpoint {

    private final ReviewService reviewService;

    @PermitAll
    @GetMapping(value = "/search")
    @Operation(summary = "Search for reviews by id of location")
    public PageableDto<ReviewDto> searchByLocationId(
        @RequestParam("id") long locationId,
        @PositiveOrZero
        @RequestParam("page") int page,
        @PositiveOrZero
        @RequestParam("pageSize") int pageSize
    ) {
        return reviewService.getAllByLocationId(locationId, page, pageSize);
    }

    @Secured("ROLE_RENTER")
    @PostMapping("/location")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a review for a location", security = @SecurityRequirement(name = "apiKey"))
    public ReviewDto createLocationReview(@Valid @RequestBody ReviewCreationDto creationDto) {
        return reviewService.createLocationReview(creationDto);
    }

    @Secured("ROLE_LENDER")
    @PostMapping("/renter")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a review for a renter", security = @SecurityRequirement(name = "apiKey"))
    public ReviewDto createRenterReview(@Valid @RequestBody ReviewCreationDto creationDto) {
        return reviewService.createRenterReview(creationDto);
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/count")
    @Operation(summary = "Get review count", security = @SecurityRequirement(name = "apiKey"))
    public ReviewCountDto getReviewCount() {
        return reviewService.getReviewCount();
    }
}
