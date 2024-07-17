package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.service.TimeslotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/timeslots")
@Slf4j
@RequiredArgsConstructor
public class TimeslotEndpoint {

    private final TimeslotService timeslotService;

    @PermitAll
    @GetMapping
    @Operation(summary = "Get list of timeslots for a given location and day")
    public List<TimeslotDto> findForLocationAndDay(@Valid TimeslotSearchDto searchDto) {
        return timeslotService.getTimeslotsForLocationStartingOnCertainDay(searchDto);
    }

    @Secured("ROLE_LENDER")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a timeslot with the given id", security = @SecurityRequirement(name = "apiKey"))
    public void delete(@PathVariable("id") Long id) {
        timeslotService.deleteTimeslot(id);
    }

    @Secured("ROLE_LENDER")
    @PutMapping("{id}")
    @Operation(summary = "Update a timeslot with the given id", security = @SecurityRequirement(name = "apiKey"))
    public TimeslotDto update(@PathVariable Long id, @Valid @RequestBody TimeslotDto timeslotDto) {
        timeslotDto.setId(id);
        return timeslotService.updateTimeslot(timeslotDto);
    }

    @Secured("ROLE_LENDER")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new timeslot", security = @SecurityRequirement(name = "apiKey"))
    public TimeslotDto add(@Valid @RequestBody TimeslotDto timeslotDto) {
        return timeslotService.createTimeslot(timeslotDto);
    }
}
