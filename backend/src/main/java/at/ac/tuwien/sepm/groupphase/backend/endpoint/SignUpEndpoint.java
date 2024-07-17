package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/signup")
@Slf4j
@RequiredArgsConstructor
public class SignUpEndpoint {

    private final UserService userService;

    @PermitAll
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String signup(@Valid @RequestBody SignUpDto signUpDto) {
        return userService.signUp(signUpDto);
    }


}
