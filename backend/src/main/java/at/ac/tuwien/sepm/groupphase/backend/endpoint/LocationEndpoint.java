package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationForLenderSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationTagCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.LocationSortingCriterion;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
@Validated
public class LocationEndpoint {

    private final LocationService locationService;

    @PermitAll
    @GetMapping(value = "/search")
    @Operation(summary = "Search for search-string in location and lender names", security = @SecurityRequirement(name = "apiKey"))
    public PageableDto<LocationDto> searchByName(
        @RequestParam("q") String searchStr,
        @PositiveOrZero
        @RequestParam("page") int page,
        @PositiveOrZero
        @RequestParam("pageSize") int pageSize,
        @RequestParam("sort") LocationSortingCriterion sortingCriterion) {

        log.info("GET /api/v1/locations");
        return locationService.searchByName(searchStr, page, pageSize, sortingCriterion);
    }

    @PermitAll
    @PostMapping(value = "/search")
    @Operation(summary = "Search with filter Params in location (and lenders)", security = @SecurityRequirement(name = "apiKey"))
    public PageableDto<LocationDto> filter(
        @Valid @RequestBody LocationFilterDto locationFilterDto,
        @PositiveOrZero
        @RequestParam("page") int page,
        @PositiveOrZero
        @RequestParam("pageSize") int pageSize,
        @RequestParam("sort") LocationSortingCriterion sortingCriterion) {

        log.info("GET /api/v1/locations");
        return locationService.filter(locationFilterDto, page, pageSize, sortingCriterion);
    }

    @PermitAll
    @GetMapping(value = "/search/lender")
    @Operation(summary = "Search for location by id of lender", security = @SecurityRequirement(name = "apiKey"))
    public PageableDto<LocationDto> searchByLender(@Valid LocationForLenderSearchDto searchDto) {
        return locationService.searchByLender(searchDto);
    }

    @Secured("ROLE_LENDER")
    @PostMapping
    @Operation(summary = "Create a new location", security = @SecurityRequirement(name = "apiKey"))
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDto createLocation(@RequestBody LocationCreationDto locationDto) {

        log.info("POST /api/v1/locations");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return locationService.createLocation(locationDto, authentication.getName());
    }

    @PermitAll
    @GetMapping(value = "/{id}")
    @Operation(summary = "Returns the location with the given id")
    @ResponseStatus(HttpStatus.OK)
    public LocationDto getLocation(@PathVariable("id") Long id) {

        log.info("GET /api/v1/locations/{}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return locationService.getLocationById(id, authentication.getName());
    }

    @Secured("ROLE_LENDER")
    @PutMapping(value = "/{id}")
    @Operation(summary = "Update an existing location", security = @SecurityRequirement(name = "apiKey"))
    public LocationDto updateLocation(@RequestBody LocationDto locationDto, @PathVariable("id") Long id) {

        log.info("PUT /api/v1/locations/{}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return locationService.updateLocation(id, authentication.getName(), locationDto);
    }

    @Secured("ROLE_LENDER")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an existing location", security = @SecurityRequirement(name = "apiKey"))
    public void removeLocation(@PathVariable("id") Long id) {

        log.info("DELETE /api/v1/locations/{}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        locationService.removeLocation(id, authentication.getName());
    }

    @PermitAll
    @GetMapping(value = "/tags")
    @Operation(summary = "Returns all tags")
    @ResponseStatus(HttpStatus.OK)
    public LocationTagCollectionDto getTags() {

        log.info("GET /api/v1/locations/tags");
        return locationService.getAllTags();
    }


}
