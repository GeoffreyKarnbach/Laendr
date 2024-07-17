package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationColumnDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDetailDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationSummaryDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SortDirectionDto;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationSummaryService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ratings")
@Slf4j
@RequiredArgsConstructor
public class ReputationEndpoint {

    private final ReputationService reputationService;
    private final ReputationSummaryService reputationSummaryService;

    @Secured("ROLE_ADMIN")
    @GetMapping("/summary/lenders")
    public PageableDto<ReputationSummaryDto> getLenderReputations(
        @RequestParam(value = "q", required = false, defaultValue = "") String search,
        @PositiveOrZero
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @Positive
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
        @RequestParam(value = "sortColumn", required = false, defaultValue = "SUBJECT_NAME") ReputationColumnDto sortColumn,
        @RequestParam(value = "sortDirection", required = false, defaultValue = "ASCENDING") SortDirectionDto sortDirection) {
        log.info("GET /api/v1/ratings/summary/lenders");

        return reputationSummaryService.getLenderReputations(search, page, pageSize, sortColumn, sortDirection);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/details/lender/{id}")
    public ReputationDetailDto getLenderDetails(@PathVariable("id") long lenderId) {
        log.info("GET /api/v1/ratings/details/lender/{}", lenderId);

        return reputationService.calculateReputationDetailsForLender(lenderId);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/summary/locations")
    public PageableDto<ReputationSummaryDto> getLocationReputations(
        @RequestParam(value = "q", required = false, defaultValue = "") String search,
        @PositiveOrZero
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @Positive
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
        @RequestParam(value = "sortColumn", required = false, defaultValue = "SUBJECT_NAME") ReputationColumnDto sortColumn,
        @RequestParam(value = "sortDirection", required = false, defaultValue = "ASCENDING") SortDirectionDto sortDirection) {
        log.info("GET /api/v1/ratings/summary/locations");

        return reputationSummaryService.getLocationReputations(search, page, pageSize, sortColumn, sortDirection);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/details/location/{id}")
    public ReputationDetailDto getLocationDetails(@PathVariable("id") long locationId) {
        log.info("GET /api/v1/ratings/details/location/{}", locationId);

        return reputationService.calculateReputationDetailsForLocation(locationId);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/summary/renters")
    public PageableDto<ReputationSummaryDto> getRenterReputations(
        @RequestParam(value = "q", required = false, defaultValue = "") String search,
        @PositiveOrZero
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @Positive
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
        @RequestParam(value = "sortColumn", required = false, defaultValue = "SUBJECT_NAME") ReputationColumnDto sortColumn,
        @RequestParam(value = "sortDirection", required = false, defaultValue = "ASCENDING") SortDirectionDto sortDirection) {
        log.info("GET /api/v1/ratings/summary/renters");

        return reputationSummaryService.getRenterReputations(search, page, pageSize, sortColumn, sortDirection);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/details/renter/{id}")
    public ReputationDetailDto getRenterDetails(@PathVariable("id") long renterId) {
        log.info("GET /api/v1/ratings/details/renter/{}", renterId);

        return reputationService.calculateReputationDetailsForRenter(renterId);
    }

    @Secured("ROLE_ADMIN")
    @PatchMapping("/lender/{id}/timedecay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLenderTimeDecay(@PathVariable("id") Long id) {
        log.info("PATCH /api/v1/ratings/lender/{}/timedecay", id);

        reputationService.updateLenderTimeDecay(id);
    }

    @Secured("ROLE_ADMIN")
    @PatchMapping("/location/{id}/timedecay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLocationTimeDecay(@PathVariable("id") Long id) {
        log.info("PATCH /api/v1/ratings/location/{}/timedecay", id);

        reputationService.updateLocationTimeDecay(id);
    }

    @Secured("ROLE_ADMIN")
    @PatchMapping("/renter/{id}/timedecay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRenterTimeDecay(@PathVariable("id") Long id) {
        log.info("PATCH /api/v1/ratings/renter/{}/timedecay", id);

        reputationService.updateRenterTimeDecay(id);
    }

}
