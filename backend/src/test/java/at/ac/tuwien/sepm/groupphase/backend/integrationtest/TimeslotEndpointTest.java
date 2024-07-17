package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TimeslotEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    private final String TIMESLOT_BASE_URI = "/api/v1/timeslots";
    private final String LENDER_USER = "test_lender@email.com";
    private final String LENDER_USER_2 = "test_lender2@email.com";
    private final List<String> LENDER_ROLES = new ArrayList<>() {
        {
            add("ROLE_LENDER");
            add("ROLE_USER");
        }
    };

    private final TimeslotDto DTO_FOR_UPDATE = TimeslotDto.builder()
        .start(LocalDateTime.of(2090,5,10,14,0))
        .end(LocalDateTime.of(2090,5,10,16,0))
        .price(BigDecimal.TEN)
        .build();

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findForLocationAndDay_givenInvalidLocationId_whenSearchingTimeslotsForLocation_then409() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get(TIMESLOT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .queryParam("locationId", "-3")
                .queryParam("day", "2023-10-10")
                .queryParam("callerIsLocationOwner", "false")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findForLocationAndDay_givenSearchDayInThePast_whenSearchingTimeslotsForDay_then422() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TIMESLOT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .queryParam("locationId", "-2")
                .queryParam("day", "2012-10-10")
                .queryParam("callerIsLocationOwner", "false")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findForLocationAndDay_givenSearchDayInTheFuture_whenSearchingTimeslotsForDay_thenFindAllTimeslotsForGivenDay() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TIMESLOT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .queryParam("locationId", "-2")
                .queryParam("day", "2090-05-13")
                .queryParam("callerIsLocationOwner", "true")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        List<TimeslotDto> timeslotDtos = Arrays.asList(objectMapper.readValue(response.getContentAsString(),
            TimeslotDto[].class));

        assertEquals(3, timeslotDtos.size());

        var timeslot = timeslotDtos.stream().sorted(new Comparator<>() {
            @Override
            public int compare(TimeslotDto o1, TimeslotDto o2) {
                if (Objects.equals(o1.getId(), o2.getId())) {
                    return 0;
                }
                return o1.getId() > o2.getId() ? -1 : 1;
            }
        }).toList().get(0);

        assertAll(
            () -> assertEquals(-1, timeslot.getId()),
            () -> assertEquals(LocalDateTime.of(2090, 5, 13, 14, 0), timeslot.getStart()),
            () -> assertEquals(LocalDateTime.of(2090, 5, 13, 16, 0), timeslot.getEnd()),
            () -> assertEquals(0, BigDecimal.valueOf(100).compareTo(timeslot.getPrice())),
            () -> assertEquals(0, BigDecimal.valueOf(50).compareTo(timeslot.getPriceHourly())),
            () -> assertEquals(false, timeslot.getIsUsed()),
            () -> assertEquals(true, timeslot.getIsRequested()),
            () -> assertEquals(false, timeslot.getIsRequestedByCallingUser())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void delete_givenInvalidId_whenDeleting_then404() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(TIMESLOT_BASE_URI + "/-10")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () ->  assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
            () -> assertEquals("Zeitfenster mit Id -10 existiert nicht", response.getContentAsString())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void delete_givenUserIsNotLocationOwner_whenDeleting_then409() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(TIMESLOT_BASE_URI + "/-2")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER_2, LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        assertAll(
            () ->  assertEquals(HttpStatus.CONFLICT.value(), response.getStatus()),
            () -> assertEquals("Validierung für das Löschen eines Zeitfensters fehlgeschlagen", result.getMessage()),
            () -> assertEquals("Benutzer ist nicht Eigentümer des Standorts", result.getErrors().get(0).getMessage())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void delete_givenValidIdAndUser_whenDeleting_then204() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(TIMESLOT_BASE_URI + "/-4")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void update_givenValidDataAndUser_whenUpdating_thenUpdatedDtoAnd200() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(put(TIMESLOT_BASE_URI + "/-4")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DTO_FOR_UPDATE)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), TimeslotDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(-4L, result.getId()),
            () -> assertEquals(DTO_FOR_UPDATE.getStart(), result.getStart()),
            () -> assertEquals(DTO_FOR_UPDATE.getEnd(), result.getEnd()),
            () -> assertEquals(0, DTO_FOR_UPDATE.getPrice().compareTo(result.getPrice())),
            () -> assertEquals(0, result.getPriceHourly().compareTo(BigDecimal.valueOf(5))),
            () -> assertEquals(false, result.getIsUsed()),
            () -> assertNull(result.getIsRequested())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void update_givenInvalidId_whenUpdating_then404() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(TIMESLOT_BASE_URI + "/-10")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DTO_FOR_UPDATE)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () ->  assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
            () -> assertEquals("Zeitfenster mit Id -10 existiert nicht", response.getContentAsString())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void update_givenStartDateTimeInThePast_whenUpdating_then422() throws Exception {
        var timeslotDto = TimeslotDto.builder()
            .start(LocalDateTime.of(2012,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(put(TIMESLOT_BASE_URI + "/-4")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeslotDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        assertAll(
            () ->  assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus()),
            () -> assertEquals("Validierung für das Aktualisieren eines Zeitfensters fehlgeschlagen", result.getMessage()),
            () -> assertEquals("Startzeitpunkt muss in der Zukunft sein", result.getErrors().get(0).getMessage())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void create_givenValidDataAndUser_whenCreating_thenCreatedDtoAnd201() throws Exception {
        var timeslotDto = TimeslotDto.builder()
            .start(LocalDateTime.of(2090,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .locationId(-2L)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TIMESLOT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeslotDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), TimeslotDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), response.getStatus()),
            () -> assertEquals(timeslotDto.getStart(), result.getStart()),
            () -> assertEquals(timeslotDto.getEnd(), result.getEnd()),
            () -> assertEquals(0, result.getPrice().compareTo(timeslotDto.getPrice())),
            () -> assertEquals(0, result.getPriceHourly().compareTo(BigDecimal.valueOf(5))),
            () -> assertEquals(false, result.getIsUsed()),
            () -> assertNull(result.getIsRequested())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void create_givenEndDateBeforeStartDate_whenCreating_then422() throws Exception {
        var timeslotDto = TimeslotDto.builder()
            .start(LocalDateTime.of(2091,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .locationId(-2L)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TIMESLOT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeslotDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        assertAll(
            () ->  assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus()),
            () -> assertEquals("Validierung für das Anlegen eines Zeitfensters fehlgeschlagen", result.getMessage()),
            () -> assertEquals("Endzeitpunkt muss nach Startzeitpunkt sein", result.getErrors().get(0).getMessage())
        );
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void create_givenNonExistingLocationId_whenCreating_then409() throws Exception {
        var timeslotDto = TimeslotDto.builder()
            .start(LocalDateTime.of(2090,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .locationId(-3L)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TIMESLOT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeslotDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        assertAll(
            () ->  assertEquals(HttpStatus.CONFLICT.value(), response.getStatus()),
            () -> assertEquals("Validierung für das Anlegen eines Zeitfensters fehlgeschlagen", result.getMessage()),
            () -> assertEquals("Angegebener Standort mit Id -3 existiert nicht", result.getErrors().get(0).getMessage())
        );
    }
}
