package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.service.PlzService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/plz")
@Slf4j
@RequiredArgsConstructor
public class PlzEndpoint {

    private final PlzService plzService;

    @PermitAll
    @GetMapping
    public List<PlzDto> findPlzSuggestions(@RequestParam("plz") String plzQuery) {
        return plzService.findPlzSuggestions(plzQuery);
    }
}