package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.LoginDto;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication")
@Slf4j
@RequiredArgsConstructor
public class LoginEndpoint {

    private final UserService userService;

    @PermitAll
    @PostMapping
    public String login(@Valid @RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }

    @PermitAll
    @GetMapping(value = "/admin-email")
    public String getAdminEmail() {
        return userService.getAdminEmail();
    }
}
