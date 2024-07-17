package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.ChangeUserPasswordDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LoginDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserInfoDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Admin;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.UserMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.AdminRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.UserValidator;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ApplicationUserRepository applicationUserRepository;
    private final LenderRepository lenderRepository;
    private final RenterRepository renterRepository;
    private final ReputationRenterRepository reputationRenterRepository;
    private final ReputationService reputationService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final UserValidator userValidator;

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            var applicationUser = findApplicationUserByEmail(email);

            var grantedAuthorities = new ArrayList<String>();
            grantedAuthorities.add(AppRole.ROLE_USER.name());
            if (applicationUser.getAdmin() != null) {
                grantedAuthorities.add(AppRole.ROLE_ADMIN.name());
            }
            if (applicationUser.getRenter() != null) {
                grantedAuthorities.add(AppRole.ROLE_RENTER.name());
            }
            if (applicationUser.getLender() != null) {
                grantedAuthorities.add(AppRole.ROLE_LENDER.name());
            }

            var createdAuthorities = AuthorityUtils.createAuthorityList(grantedAuthorities.toArray(new String[0]));
            return new User(applicationUser.getEmail(), applicationUser.getPassword(), applicationUser.isLocked(), !applicationUser.isLocked(),
                !applicationUser.isLocked(), !applicationUser.isLocked(), createdAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public ApplicationUser findApplicationUserByEmail(String email) {
        Optional<ApplicationUser> applicationUser = applicationUserRepository.findApplicationUserByEmail(email);

        if (applicationUser.isPresent()) {
            return applicationUser.get();
        }
        throw new NotFoundException(String.format("User mit email address %s konnte nicht gefunden werden!", email));
    }

    @Override
    public ApplicationUser findApplicationUserByUsername(String username) {
        Optional<ApplicationUser> applicationUser = applicationUserRepository.findApplicationUserByEmail(username);

        if (applicationUser.isPresent()) {
            return applicationUser.get();
        }
        throw new NotFoundException(String.format("User mit Username %s konnte nicht gefunden werden!", username));

    }

    @Override
    public ApplicationUser findApplicationUserById(Long id) {
        Optional<ApplicationUser> applicationUser = applicationUserRepository.findApplicationUserById(id);

        if (applicationUser.isPresent()) {
            return applicationUser.get();
        }
        throw new NotFoundException(String.format("User mit ID %s konnte nicht gefunden werden!", id));

    }

    @Override
    public String login(LoginDto loginDto) {
        UserDetails userDetails = loadUserByUsername(loginDto.getEmail());
        if (userDetails != null
            && userDetails.isAccountNonExpired()
            && userDetails.isAccountNonLocked()
            && userDetails.isCredentialsNonExpired()
        ) {
            if (passwordEncoder.matches(loginDto.getPassword(), userDetails.getPassword())) {
                List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
                return jwtTokenizer.getAuthToken(userDetails.getUsername(), roles);
            } else {
                ApplicationUser user = applicationUserRepository.findApplicationUserByEmail(loginDto.getEmail()).get();
                increaseLoginAttempts(user);
            }
        }

        throw new BadCredentialsException("Username oder Passwort ist nicht korrekt oder User ist gesperrt!");
    }

    @Override
    public String regenerateToken(String email) throws BadCredentialsException {
        var userDetails = loadUserByUsername(email);
        if (userDetails != null) {
            var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            return jwtTokenizer.getAuthToken(userDetails.getUsername(), roles);
        } else {
            throw new BadCredentialsException("Username is incorrect");
        }
    }

    @Override
    public boolean increaseLoginAttempts(ApplicationUser user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);
        if (user.getLoginAttempts() > 5) {
            user.setLocked(true);
        }
        applicationUserRepository.save(user);
        return user.isLocked();
    }

    @Override
    public String signUp(SignUpDto signUpDto) {
        userValidator.validateSignUpUser(signUpDto);

        var entity = ApplicationUser.builder()
            .name(signUpDto.getUsername())
            .email(signUpDto.getEmail())
            .password(passwordEncoder.encode(signUpDto.getOriginalPassword()))
            .isLocked(false)
            .isDeleted(false)
            .loginAttempts(0)
            .build();
        var createdAccount = applicationUserRepository.save(entity);

        renterRepository.save(Renter.builder()
            .id(createdAccount.getId())
            .reputation(reputationRenterRepository.save(reputationService.newRenterReputationEntity()))
            .isDeleted(false)
            .build()
        );

        return login(new LoginDto(signUpDto.getEmail(), signUpDto.getOriginalPassword()));
    }

    @Override
    public Long getUserIdByEmail(String email) {
        return findApplicationUserByEmail(email).getId();
    }

    @Override
    public UserDto getUserById(Long id) {
        ApplicationUser requestedUser = this.findApplicationUserById(id);
        userValidator.validateUserRequest(requestedUser);

        // Map the ApplicationUser to UserDto
        UserDto userDto = userMapper.entityToDto(requestedUser);

        // If the user is a renter, merge the renter information into UserDto
        Optional<Renter> renter = renterRepository.findById(id);
        if (renter.isPresent()) {
            userDto = userMapper.mergeRenterInfoIntoDto(userDto, renter.get());
        }

        // If the user is a lender, merge the lender information into UserDto
        Optional<Lender> lender = lenderRepository.findById(requestedUser.getId());
        if (lender.isPresent()) {
            userDto = userMapper.mergeLenderInfoIntoDto(userDto, lender.get());
        }

        userDto.setLocked(requestedUser.isLocked());

        return userDto;
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {

        if (userDto.getPlz() != null) {
            if (userDto.getPlz().getPlz().isBlank() && userDto.getPlz().getOrt().isBlank()) {
                userDto.setPlz(null);
            }
        }
        userValidator.validateUpdateUser(id, userDto);

        ApplicationUser updatedUser = findApplicationUserById(id);
        // Map the UserDto to ApplicationUser
        updatedUser = userMapper.mapUpdateToAppUser(updatedUser, userDto);
        updatedUser = applicationUserRepository.save(updatedUser);

        // If the user is a renter, merge the renter information into UserDto
        Optional<Renter> renterToUpdate = renterRepository.findById(id);
        if (renterToUpdate.isPresent()) {
            Renter updatedRenter = renterToUpdate.get();
            updatedRenter = userMapper.mapUpdateToRenter(updatedRenter, userDto);
            renterRepository.save(updatedRenter);
        }

        // If the user is a lender, merge the lender information into UserDto
        Optional<Lender> lenderToUpdate = lenderRepository.findById(updatedUser.getId());
        if (lenderToUpdate.isPresent()) {
            Lender updatedLender = lenderToUpdate.get();
            updatedLender = userMapper.mapUpdateToLender(updatedLender, userDto);
            lenderRepository.save(updatedLender);
        }

        return userDto;
    }

    @Override
    public void changeUserPassword(Long id, ChangeUserPasswordDto dto) {
        ApplicationUser requestedUser = this.findApplicationUserById(id);

        if (!dto.getCurrentPassword().isBlank() && passwordEncoder.matches(dto.getCurrentPassword(), requestedUser.getPassword())) {
            userValidator.validateChangePassword(requestedUser, dto);
        } else {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("Aktuelles Passwort stimmt nicht überein!")
                    .errors(null)
                    .build()
            );
        }

        requestedUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        applicationUserRepository.save(requestedUser);
    }

    @Override
    public PageableDto<UserDto> getAllUsers(boolean locked, int page, int pageSize) {
        Page<ApplicationUser> result;
        if (locked) {
            result = applicationUserRepository.findAllLockedUsers(PageRequest.of(page, pageSize));
        } else {
            result = applicationUserRepository.findAllUsers(PageRequest.of(page, pageSize));
        }
        var users = result.stream()
            .map(userMapper::entityToDto)
            .toList();
        return new PageableDto<>(result.getTotalElements(), result.getTotalPages(), result.getNumberOfElements(), users);
    }

    @Override
    public void lockUser(Long id) {
        ApplicationUser requestedUser = this.findApplicationUserById(id);

        if (requestedUser.isLocked()) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("User ist bereits gesperrt!")
                    .errors(null)
                    .build()
            );
        }

        requestedUser.setLocked(true);
        applicationUserRepository.save(requestedUser);
    }

    @Override
    public void unlockUser(Long id) {
        ApplicationUser requestedUser = this.findApplicationUserById(id);

        if (!requestedUser.isLocked()) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("User ist bereits entsperrt!")
                    .errors(null)
                    .build()
            );
        }

        requestedUser.setLocked(false);
        applicationUserRepository.save(requestedUser);
    }

    @Override
    public String getAdminEmail() {
        List<Admin> admins = adminRepository.findAll();
        if (admins.size() == 0) {
            // Default email address
            return "admin@email.com";
        }
        Long adminId = admins.get(0).getId();
        return findApplicationUserById(adminId).getEmail();
    }

    @Override
    public UserInfoDto getUserInfoOfCallingUser() {
        var activeUser = UserUtil.getActiveUser();
        if (activeUser == null) {
            throw new BadCredentialsException("Es ist kein User angemeldet.");
        }
        var user = findApplicationUserByEmail(activeUser.getEmail());

        return UserInfoDto.builder()
            .id(user.getId())
            .coordinates(
                CoordinateDto.builder()
                    .lat(user.getCoordLat())
                    .lng(user.getCoordLng())
                    .build()
            )
            .build();
    }
}
