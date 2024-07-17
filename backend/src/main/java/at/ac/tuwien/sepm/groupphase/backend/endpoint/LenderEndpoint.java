package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.LenderViewDto;
import at.ac.tuwien.sepm.groupphase.backend.service.LenderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/lender")
@Slf4j
@Validated
@RequiredArgsConstructor
public class LenderEndpoint {

    private final LenderService lenderService;

    @PermitAll
    @GetMapping(value = "/{id}")
    @Operation(summary = "Returns the lender with the given id")
    @ResponseStatus(HttpStatus.OK)
    public LenderViewDto getById(@PathVariable long id) {
        log.info("GET /api/v1/lender/{}", id);

        return lenderService.getById(id);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/role/{userId}")
    @Operation(summary = "Returns a new JWT iff the given user is the same as the active user")
    @ResponseStatus(HttpStatus.CREATED)
    public String addLenderRole(@PathVariable long userId) {
        log.info("POST /api/v1/lender/role/{}", userId);

        return lenderService.addLenderRole(userId);
    }

}
