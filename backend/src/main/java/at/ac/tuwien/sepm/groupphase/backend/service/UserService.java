package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.ChangeUserPasswordDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LoginDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserInfoDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Find an application user based on the email address.
     *
     * @param email the email address
     * @return a application user
     */
    ApplicationUser findApplicationUserByEmail(String email);

    /**
     * Find an application user based on the username.
     *
     * @param username the email address
     * @return a application user
     */
    ApplicationUser findApplicationUserByUsername(String username);

    /**
     * Find an application user based on the id.
     *
     * @param id the id
     * @return an application user
     */
    ApplicationUser findApplicationUserById(Long id);

    /**
     * Log in a user.
     *
     * @param loginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(LoginDto loginDto);

    /**
     * Generates a new token for the given user.
     *
     * @param email email of the given user
     * @return the JWT, if successful
     * @throws BadCredentialsException if credentials are bad
     */
    String regenerateToken(String email);

    /**
     * Lock a user.
     *
     * @param user attempted user
     * @return user locked after increase
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    boolean increaseLoginAttempts(ApplicationUser user);

    /**
     * Sign Up a user.
     *
     * @param signUpDto signUp credentials
     * @return the JWT
     */
    String signUp(SignUpDto signUpDto);

    /**
     * Returns the user id for the given email.
     *
     * @param email email of the location to return
     * @return UserDto Location with the given id
     */
    Long getUserIdByEmail(String email);

    /**
     * Returns the user with the given id.
     *
     * @param id ID of the user to return
     * @return UserDto User with the given id
     */
    UserDto getUserById(Long id);

    /**
     * Updates the User with the given id (only the user and admin can update the User information).
     *
     * @param id      ID of the ApplicationUser to update
     * @param userDto User to update
     * @return UserDto user with the given id
     */
    UserDto updateUser(Long id, UserDto userDto);

    /**
     * Changes the User Password to a new Password.
     *
     * @param id  ID of the ApplicationUser to update
     * @param dto Password Confirmation and New Passwords
     */
    void changeUserPassword(Long id, ChangeUserPasswordDto dto);

    /**
     * Returns all users in a pageable format.
     *
     * @param locked   whether to return only locked users
     * @param page     the page number
     * @param pageSize the size of the page
     * @return UserDto[] all users
     */
    PageableDto<UserDto> getAllUsers(boolean locked, int page, int pageSize);

    /**
     * Locks the given user.
     *
     * @param id the ID of the user to lock
     */
    void lockUser(Long id);

    /**
     * Unlocks the given user.
     *
     * @param id the ID of the user to unlock
     */
    void unlockUser(Long id);

    /**
     * Returns the email of the admin.
     *
     * @return the email of the admin
     */
    String getAdminEmail();

    /**
     * Returns short info of calling user.
     *
     * @return info of calling user
     */
    UserInfoDto getUserInfoOfCallingUser();
}
