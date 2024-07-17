package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.ChangeUserPasswordDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LoginDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserInfoDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static at.ac.tuwien.sepm.groupphase.backend.basetest.TestData.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserEndpointTest {

    private static final String USER_BASE_URI = BASE_URI + "/user";
    private static final String USER_CHANGE_PASSWORD_URI = "/change-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private ApplicationUserRepository userRepository;

    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private LenderRepository lenderRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    @Transactional
    @Sql("/sql/user/account_data.sql")
    @Sql("/sql/plz.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateDto_givenData_whenInvalidData_thenValidationError() throws Exception {

        Long id = -1L;
        UserDto userDto = UserDto.builder()
            .name("")
            .email("lender@email.com")
            .plz(PlzDto.builder().plz("1010").ort("Wien").build())
            .state(AustriaState.W)
            .loginAttempts(0)
            .isLocked(false)
            .renterEmail("renter@email.com")
            .renterPhone("+43 1231212 132")
            .isLender(true)
            .lenderEmail("lender@email.com")
            .lenderPhone("0660 546045")
            .lenderDescription("Beschreibung 1")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(put(USER_BASE_URI + "/" + id)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        List<String> errorMessages = new ArrayList<>();
        result.getErrors().forEach(error -> errorMessages.add(error.getMessage()));

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value()),
            () -> assertEquals(result.getErrors().size(), 1),
            () -> assertTrue(errorMessages.contains("Username darf nicht leer sein"))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/user/account_data.sql")
    @Sql("/sql/plz.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateUser_givenData_whenValidData_thenUserDtoWithId() throws Exception {
        Long id = -1L;
        UserDto userDto = UserDto.builder()
            .name("test_lender_2")
            .email("lender@email.com")
            .plz(PlzDto.builder().plz("1010").ort("Wien").build())
            .state(AustriaState.W)
            .loginAttempts(0)
            .isLocked(false)
            .renterEmail("renter@email.com")
            .renterPhone("+43 1231212 132")
            .isLender(true)
            .lenderEmail("lender@email.com")
            .lenderPhone("0660 546045")
            .lenderDescription("Beschreibung 1")
            .coordinates(CoordinateDto.builder().lat(BigDecimal.ONE).lng(BigDecimal.TEN).build())
            .build();

        MvcResult mvcResult = this.mockMvc.perform(put(USER_BASE_URI + "/" + id)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), UserDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertNotNull(result.getEmail()),
            () -> assertEquals(result.getName(), userDto.getName()),
            () -> assertEquals(result.getLenderDescription(), userDto.getLenderDescription()),
            () -> assertEquals(result.getPlz().getPlz(), userDto.getPlz().getPlz()),
            () -> assertEquals(result.getState(), userDto.getState()),
            () -> assertEquals(result.getCoordinates().getLat(), userDto.getCoordinates().getLat()),
            () -> assertEquals(result.getCoordinates().getLng(), userDto.getCoordinates().getLng())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/user/account_data.sql")
    @Sql("/sql/plz.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void changePassword_givenData_whenInvalidData_thenValidationError() throws Exception {

        Long id = -1L;
        ChangeUserPasswordDto dto = ChangeUserPasswordDto.builder()
            .currentPassword("password")
            .newPassword("1234")
            .repeatedPassword("4321")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(put(USER_BASE_URI + "/" + id + USER_CHANGE_PASSWORD_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        List<String> errorMessages = new ArrayList<>();
        result.getErrors().forEach(error -> errorMessages.add(error.getMessage()));

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value()),
            () -> assertEquals(result.getErrors().size(), 1),
            () -> assertTrue(errorMessages.contains("Passwörter stimmen nicht überein."))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/user/account_data.sql")
    @Sql("/sql/plz.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void changePassword_givenData_whenValidData_thenPasswordIsChanged() throws Exception {
        Long id = -1L;
        ChangeUserPasswordDto dto = ChangeUserPasswordDto.builder()
            .currentPassword("password")
            .newPassword("1234")
            .repeatedPassword("1234")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(put(USER_BASE_URI + "/" + id + USER_CHANGE_PASSWORD_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertAll(
            () -> assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus())
        );
    }

    @Test
    @Sql("/sql/user/delete_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void deleteUser_givenUserData_whenDeletionSuccessful_thenPersonalDataIsDeletedAndDeletionFlagsAreSet() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(
                delete(USER_BASE_URI + "/-1")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("deleteme@email.local", LENDER_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

        var user = userRepository.findById(-1L).get();
        var renter = renterRepository.findById(-1L).get();
        var lender = lenderRepository.findById(-1L).get();
        var transactionAsRenter = transactionRepository.findById(-1L).get();
        var ownedLocation = locationRepository.findById(-1L).get();

        assertAll(
            () -> assertNotEquals("deleteme@email.local", user.getEmail()),
            () -> assertNotEquals("Delete Me", user.getEmail()),
            () -> assertTrue(user.isDeleted()),

            () -> assertNotEquals("deleteme@email.local", renter.getEmail()),
            () -> assertNotEquals("+12 34 56 78 90", renter.getPhone()),
            () -> assertTrue(renter.isDeleted()),

            () -> assertNotEquals("deleteme@email.local", lender.getEmail()),
            () -> assertNotEquals("+12 34 56 78 90", lender.getPhone()),
            () -> assertNotEquals("I lend and will be deleted", lender.getDescription()),
            () -> assertTrue(lender.isDeleted()),

            () -> assertTrue(transactionAsRenter.getCancelled()),
            () -> assertEquals(CancelReason.USER_REMOVED, transactionAsRenter.getCancelReason()),

            () -> assertTrue(ownedLocation.isRemoved())
        );
    }

    @Test
    @Sql("/sql/user/delete_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void deleteUser_givenUserData_whenUserNotAuthorized_thenThrowConflict() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(
                delete(USER_BASE_URI + "/-1")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("wrongperson@email.local", LENDER_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/user/account_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getAllUsers_givenCalled_whenAdmin_thenGetAllUsers() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(
                get(USER_BASE_URI + "/all")
                    .param("locked", "false")
                    .param("page", "0")
                    .param("pageSize", "5")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<UserDto> resultList = result.getResult().stream().map(item -> objectMapper.convertValue(item, UserDto.class)).toList();

        List<String> emails = new ArrayList<>();
        for (UserDto user : resultList) {
            emails.add(user.getEmail());
        }

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(3, emails.size()),
            () -> assertTrue(emails.contains("lender@email.com")),
            () -> assertTrue(emails.contains("renter@email.com")),
            () -> assertTrue(emails.contains("renter2@email.com"))
        );
    }

    @Test
    @Sql("/sql/user/account_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getAllUsers_givenCalled_whenNotAdmin_thenForbidden() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(
                get(USER_BASE_URI + "/all")
                    .param("locked", "false")
                    .param("page", "0")
                    .param("pageSize", "5")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/user/account_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void lockUser_givenExistingId_whenAdminCalling_thenUserLocked() throws Exception {
        Long userId = -1L;

        MvcResult mvcResult = this.mockMvc.perform(
                put(USER_BASE_URI + "/lock/" + userId)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

        MvcResult mvcResult2 = this.mockMvc.perform(
            post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginDto("lender@email.com", "password")))
        ).andDo(print())
            .andReturn();

        MockHttpServletResponse response2 = mvcResult2.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response2.getStatus());
    }

    @Test
    @Sql("/sql/user/account_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void lockUser_givenExistingId_whenNotAdminCalling_thenForbidden() throws Exception {
        Long userId = -1L;

        MvcResult mvcResult = this.mockMvc.perform(
                put(USER_BASE_URI + "/lock/" + userId)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/user/account_locked.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void unlockUser_givenExistingId_whenAdminCalling_thenUserUnlocked() throws Exception {
        Long userId = -1L;

        // Test if login not possible
        MvcResult mvcResult = this.mockMvc.perform(
                post("/api/v1/authentication")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new LoginDto("renter@email.com", "password")))
            ).andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        // Unlock user
        MvcResult mvcResult2 = this.mockMvc.perform(
                put(USER_BASE_URI + "/unlock/" + userId)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response2 = mvcResult2.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response2.getStatus());

        // Test if login possible
        MvcResult mvcResult3 = this.mockMvc.perform(
                post("/api/v1/authentication")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new LoginDto("renter@email.com", "password")))
            ).andDo(print())
            .andReturn();

        MockHttpServletResponse response3 = mvcResult3.getResponse();
        assertEquals(HttpStatus.OK.value(), response3.getStatus());
    }

    @Test
    @Sql("/sql/user/account_locked.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void unlockUser_givenExistingId_whenNotAdminCalling_thenForbidden() throws Exception {
        Long userId = -1L;

        // Unlock user
        MvcResult mvcResult2 = this.mockMvc.perform(
                put(USER_BASE_URI + "/unlock/" + userId)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response2 = mvcResult2.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response2.getStatus());
    }



    @Test
    @Sql("/sql/user/account_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getInfoOfCallingUser_given_whenUserExists_thenUserInfo() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(
                get(USER_BASE_URI + "/info")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        UserInfoDto userInfo = objectMapper.readValue(response.getContentAsString(),
            UserInfoDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(-1L, userInfo.getId()),
            () -> assertNull(userInfo.getCoordinates().getLng()),
            () -> assertNull(userInfo.getCoordinates().getLat())
        );
    }
}
