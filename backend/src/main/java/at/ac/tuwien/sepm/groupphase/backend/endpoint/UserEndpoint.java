package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.ChangeUserPasswordDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserInfoDto;
import at.ac.tuwien.sepm.groupphase.backend.service.UserDeletionService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserEndpoint {

    private final UserService userService;

    private final UserDeletionService userDeletionService;

    @Secured("ROLE_USER")
    @GetMapping(value = "/{id}")
    @Operation(summary = "Returns the user with the given id")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@PathVariable("id") Long id) {

        log.info("GET /api/v1/user/{}", id);
        return userService.getUserById(id);
    }

    @Secured("ROLE_USER")
    @GetMapping()
    @Operation(summary = "Returns the user with the email")
    @ResponseStatus(HttpStatus.OK)
    public Long getUserId(@RequestParam("email") String email) {

        log.info("GET /api/v1/user?email=" + email);
        return userService.getUserIdByEmail(email);
    }

    @Secured("ROLE_USER")
    @GetMapping("info")
    @Operation(summary = "Returns short info of the calling user")
    @ResponseStatus(HttpStatus.OK)
    public UserInfoDto getInfoOfCallingUser() {
        return userService.getUserInfoOfCallingUser();
    }

    @Secured("ROLE_USER")
    @PutMapping(value = "/{id}/change-password")
    @Operation(summary = "Change a user Password", security = @SecurityRequirement(name = "apiKey"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUserPassword(@RequestBody ChangeUserPasswordDto dto, @PathVariable("id") Long id) {

        log.info("PUT /api/v1/user/{}/change-password", id);
        userService.changeUserPassword(id, dto);
    }

    @Secured("ROLE_USER")
    @PutMapping(value = "/{id}")
    @Operation(summary = "Update the Account Information of a User", security = @SecurityRequirement(name = "apiKey"))
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUserInformation(@RequestBody UserDto dto, @PathVariable("id") Long id) {
        log.info("PUT /api/v1/user/{}", id);
        return userService.updateUser(id, dto);
    }


    @Secured("ROLE_USER")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("id") long id) {
        log.info("DELETE /api/v1/user/{}", id);

        userDeletionService.deleteUser(id);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping(value = "/all")
    @Operation(summary = "Returns all users")
    @ResponseStatus(HttpStatus.OK)
    public PageableDto<UserDto> getAllUsers(@RequestParam("locked") boolean locked,
                                            @PositiveOrZero @RequestParam("page") int page,
                                            @PositiveOrZero @RequestParam("pageSize") int pageSize) {
        log.info("GET /api/v1/user/locked/all");
        return userService.getAllUsers(locked, page, pageSize);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/lock/{id}")
    @Operation(summary = "Locks a user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void lockUser(@PathVariable("id") Long id) {
        log.info("PUT /api/v1/user/lock/{}", id);
        userService.lockUser(id);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/unlock/{id}")
    @Operation(summary = "Unlocks a user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockUser(@PathVariable("id") Long id) {
        log.info("PUT /api/v1/user/unlock/{}", id);
        userService.unlockUser(id);
    }

}
