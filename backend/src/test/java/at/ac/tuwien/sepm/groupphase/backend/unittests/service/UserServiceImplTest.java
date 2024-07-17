package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private ApplicationUserRepository applicationUserRepository;

    @MockBean
    private RenterRepository renterRepository;

    @MockBean
    private PlzRepository plzRepository;

    @Test
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void signUp_givenValidInput_whenValidSignUp_thenUserToken() {
        Long id = -1L;
        var dto = SignUpDto.builder()
            .username("Test SignUp")
            .email("testSignup@email.com")
            .originalPassword("password")
            .repeatedPassword("password")
            .build();

        ApplicationUser existingUser = ApplicationUser.builder()
            .id(id)
            .password(passwordEncoder.encode(dto.getOriginalPassword()))
            .email(dto.getEmail())
            .isLocked(false)
            .build();

        when(applicationUserRepository.findApplicationUserByEmail(dto.getEmail()))
            .thenReturn(Optional.empty()) // Initial call returns empty to indicate user does not exist
            .thenAnswer(invocation -> Optional.of(existingUser)); // Subsequent call returns the existing user
        when(applicationUserRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, ApplicationUser.class);
            arg.setId(id);
            arg.setCreatedAt(LocalDateTime.now());
            return arg;
        });
        when(renterRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Renter.class);
            arg.setId(id);
            arg.setCreatedAt(LocalDateTime.now());
            return arg;
        });

        var token = userService.signUp(dto);

        assertNotNull(token);
    }

    @Test
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void updateLocation_givenCorrectData_whenUpdated_thenUserDtoHasNewData() {
        Long id = -1L;
        var currentUser = ApplicationUser.builder()
            .id(id)
            .name("test_lender_2")
            .email("lender@email.com")
            .loginAttempts(0)
            .isLocked(false)
            .build();

        UserDto updatedUser = UserDto.builder()
            .name("test_lender_2")
            .email("lender@email.com")
            .plz(PlzDto.builder().plz("3902").ort("Vitis").build())
            .state(AustriaState.NOE)
            .loginAttempts(0)
            .isLocked(false)
            .renterEmail("renter@email.com")
            .renterPhone("+43 1231212 132")
            .isLender(true)
            .lenderEmail("lender@email.com")
            .lenderPhone("0660 18298993")
            .lenderDescription("Beschreibung 1")
            .coordinates(CoordinateDto.builder().lat(BigDecimal.ONE).lng(BigDecimal.TEN).build())
            .build();

        when(applicationUserRepository.findById(id)).thenReturn(Optional.of(currentUser));
        when(applicationUserRepository.findApplicationUserById(id)).thenReturn(Optional.of(currentUser));
        when(applicationUserRepository.findApplicationUserByEmail(currentUser.getEmail())).thenReturn(
            Optional.of(ApplicationUser.builder().id(id).email(currentUser.getEmail()).build()));
        when(plzRepository.existsById("3902")).thenReturn(true);
        when(plzRepository.getReferenceById("3902")).thenReturn(Plz.builder().plz("3902").build());
        when(applicationUserRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, ApplicationUser.class);
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        var result = userService.updateUser(id, updatedUser);

        assertAll(
            () -> assertEquals(updatedUser.getName(), result.getName()),
            () -> assertEquals(updatedUser.getPlz(), result.getPlz()),
            () -> assertEquals(updatedUser.getState(), result.getState()),
            () -> assertEquals(updatedUser.getCoordinates().getLat(), result.getCoordinates().getLat()),
            () -> assertEquals(updatedUser.getCoordinates().getLng(), result.getCoordinates().getLng())
        );

    }
}
