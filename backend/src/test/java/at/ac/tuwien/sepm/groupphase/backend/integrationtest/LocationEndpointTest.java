package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDetailDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LocationEndpointTest implements TestData {

    private static final String LOCATION_BASE_URI = BASE_URI + "/locations";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Test
    @Transactional
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void searchByName_givenData_whenAllParams_thenWrappedLocationDtos() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/search")
                .queryParam("q", "")
                .queryParam("page", "0")
                .queryParam("pageSize", "1")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<LocationDto> locationDtos = ((List) result.getResult()).stream().map(item -> objectMapper.convertValue(item, LocationDto.class)).toList();

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getResultCount());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, locationDtos.size());
        assertEquals("test_location_1", locationDtos.get(0).getName());
    }

    @Test
    public void searchByName_givenData_whenWrongParams_thenBadRequest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/search")
                .queryParam("q", "")
                .queryParam("page", "0")
                .queryParam("pageSize", "-1")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void filter_givenData_whenAllParams_thenWrappedLocationDtos() throws Exception {
        LocationFilterDto dto = new LocationFilterDto();
        dto.setSearchString("location");
        dto.setState(AustriaState.NOE);
        dto.setPlz(new PlzDto("3101", "Wien1", null));
        dto.setAddress("address_1");
        dto.setPriceFrom(BigDecimal.valueOf(100));
        dto.setPriceTo(BigDecimal.valueOf(200));
        dto.setTimeFrom(LocalDate.of(3000, 1, 1));
        dto.setTimeTo(LocalDate.of(3025, 1, 1));


        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .queryParam("page", "0")
                .queryParam("pageSize", "1")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<LocationDto> locationDtos = ((List) result.getResult()).stream().map(item -> objectMapper.convertValue(item, LocationDto.class)).toList();

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getResultCount());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, locationDtos.size());
        assertEquals("test_location_1", locationDtos.get(0).getName());
    }

    @Test
    @Sql("/sql/location/tag_location_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void filter_givenData_whenTagsGiven_thenWrappedLocationDtos() throws Exception {
        LocationFilterDto dto = new LocationFilterDto();
        dto.setTags(new String[]{"Tag 1", "Tag 2"});

        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .queryParam("page", "0")
                .queryParam("pageSize", "2")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<LocationDto> locationDtos = ((List) result.getResult()).stream().map(item -> objectMapper.convertValue(item, LocationDto.class)).toList();
        List<Long> locationIds = locationDtos.stream().map(LocationDto::getId).toList();

        assertAll(
            () -> assertEquals(2, result.getTotalResults()),
            () -> assertEquals(2, result.getResultCount()),
            () -> assertEquals(1, result.getTotalPages()),
            () -> assertEquals(2, locationDtos.size()),
            () -> assertTrue(locationIds.contains(-1L)),
            () -> assertTrue(locationIds.contains(-3L))
        );
    }

    @Test
    @Sql("/sql/location/tag_location_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void filter_givenData_whenTagsNotExist_thenWrappedLocationDtos() throws Exception {
        LocationFilterDto dto = new LocationFilterDto();
        dto.setTags(new String[]{"Tag 1", "Tag 4"});

        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .queryParam("page", "0")
                .queryParam("pageSize", "2")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);
        List<String> errorMessages = new ArrayList<>();

        for (var error : result.getErrors()) {
            errorMessages.add(error.getMessage());
        }

        assertAll(
            () -> assertEquals(1, result.getErrors().size()),
            () -> assertTrue(errorMessages.contains("Tag 'Tag 4' existiert nicht."))
        );
    }

    @Test
    public void filter_givenData_whenWrongParams_thenBadRequest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("WRONG_BODY"))
                .queryParam("page", "0")
                .queryParam("pageSize", "1")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    @Transactional
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void searchByLender_givenData_whenAllParams_thenWrappedLocationDtos() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/search/lender")
                .queryParam("id", "1")
                .queryParam("includeRemovedLocations", "false")
                .queryParam("page", "0")
                .queryParam("pageSize", "1")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<LocationDto> locationDtos = ((List) result.getResult()).stream().map(item -> objectMapper.convertValue(item, LocationDto.class)).toList();

        assertAll(
            () -> assertEquals(1, result.getTotalResults()),
            () -> assertEquals(1, result.getResultCount()),
            () -> assertEquals(1, result.getTotalPages()),
            () -> assertEquals(1, locationDtos.size())
        );
    }

    @Test
    public void searchByLender_givenData_whenWrongParams_thenBadRequest() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/search")
                .queryParam("id", "")
                .queryParam("page", "0")
                .queryParam("pageSize", "-1")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void createLocation_givenData_whenInvalidData_thenValidationError() throws Exception {

        LocationCreationDto locationCreationDto = LocationCreationDto.builder()
            .name("    ")
            .description("")
            .plz(PlzDto.builder().plz("-1000").build())
            .state(AustriaState.W)
            .address("")
            .size(new BigDecimal(1000000000))
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationCreationDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        List<String> errorMessages = new ArrayList<>();
        result.getErrors().forEach(error -> errorMessages.add(error.getMessage()));

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value()),
            () -> assertEquals(result.getErrors().size(), 4),
            () -> assertTrue(errorMessages.contains("Name darf nicht leer sein.")),
            () -> assertTrue(errorMessages.contains("Addresse darf nicht leer sein.")),
            () -> assertTrue(errorMessages.contains("Größe darf höchstens 100000 betragen.")),
            () -> assertTrue(errorMessages.contains("PLZ muss existieren."))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/plz.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createLocation_givenData_whenValidData_thenLocationDtoWithId() throws Exception {
        LocationCreationDto locationCreationDto = LocationCreationDto.builder()
            .name("Test location name - Integration Test 1")
            .description("Test location name - Integration Test 1")
            .plz(PlzDto.builder().plz("1010").build())
            .state(AustriaState.W)
            .address("Test location address - Integration Test 1")
            .size(new BigDecimal(10000))
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationCreationDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), LocationDto.class);

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.CREATED.value()),
            () -> assertNotNull(result.getId()),
            () -> assertEquals(result.getName(), locationCreationDto.getName()),
            () -> assertEquals(result.getDescription(), locationCreationDto.getDescription()),
            () -> assertEquals(result.getPlz().getPlz(), locationCreationDto.getPlz().getPlz()),
            () -> assertEquals(result.getState(), locationCreationDto.getState()),
            () -> assertEquals(result.getAddress(), locationCreationDto.getAddress()),
            () -> assertEquals(result.getSize(), locationCreationDto.getSize())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/plz.sql")
    @Sql("/sql/tag/tag_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createLocation_givenDataAndTags_whenValidData_thenLocationDtoWithId() throws Exception {
        LocationCreationDto locationCreationDto = LocationCreationDto.builder()
            .name("Test location Tags")
            .description("Test location name - Tags")
            .plz(PlzDto.builder().plz("1010").build())
            .state(AustriaState.W)
            .address("Test location address Tags")
            .size(new BigDecimal(10000))
            .tags(List.of("Tag 1", "Tag 2"))
            .coord(null)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationCreationDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), LocationDto.class);

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.CREATED.value()),
            () -> assertNotNull(result.getId()),
            () -> assertEquals(result.getName(), locationCreationDto.getName()),
            () -> assertEquals(result.getDescription(), locationCreationDto.getDescription()),
            () -> assertEquals(result.getPlz().getPlz(), locationCreationDto.getPlz().getPlz()),
            () -> assertEquals(result.getState(), locationCreationDto.getState()),
            () -> assertEquals(result.getAddress(), locationCreationDto.getAddress()),
            () -> assertEquals(result.getSize(), locationCreationDto.getSize())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/plz.sql")
    @Sql("/sql/tag/tag_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createLocation_givenData_whenNonExistentTags_thenValidationError() throws Exception {
        LocationCreationDto locationCreationDto = LocationCreationDto.builder()
            .name("Test location Tags")
            .description("Test location name - Tags")
            .plz(PlzDto.builder().plz("1010").build())
            .state(AustriaState.W)
            .address("Test location address Tags")
            .size(new BigDecimal(10000))
            .tags(List.of("Tag 4", "Tag 5"))
            .coord(null)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationCreationDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        List<String> errorMessages = new ArrayList<>();
        for (ValidationErrorDetailDto error : result.getErrors()) {
            errorMessages.add(error.getMessage());
        }

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value()),
            () -> assertTrue(errorMessages.contains("Tag 'Tag 4' existiert nicht.")),
            () -> assertTrue(errorMessages.contains("Tag 'Tag 5' existiert nicht."))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createLocation_givenData_whenValidDataButRenter_thenValidationError() throws Exception {
        LocationCreationDto locationCreationDto = LocationCreationDto.builder()
            .name("Test location name - Integration Test 1")
            .description("Test location name - Integration Test 1")
            .plz(PlzDto.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("Test location address - Integration Test 1")
            .size(new BigDecimal(10000))
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationCreationDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.FORBIDDEN.value())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getLocation_givenId_whenInvalid_thenNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/-1"))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.NOT_FOUND.value()),
            () -> assertTrue(response.getContentAsString().matches("Location with id -1 not found"))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getLocation_givenId_whenValidAndFromOwner_thenLocationDtoCorrectContentAndOwnerFlagTrue() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        var result = objectMapper.readValue(response.getContentAsString(), LocationDto.class);

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.OK.value()),
            () -> assertEquals(result.getId(), 1L),
            () -> assertEquals(result.getName(), "test_location_1_name"),
            () -> assertEquals(result.getDescription(), "test_location_1_description"),
            () -> assertEquals(result.getPlz(), null),
            () -> assertEquals(result.getState(), AustriaState.W),
            () -> assertEquals(result.getAddress(), "test_location_1_address"),
            () -> assertTrue(result.getSize().compareTo(new BigDecimal(1000)) == 0),
            () -> assertTrue(result.isCallerIsOwner())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getLocation_givenId_whenValidAndNotFromOwner_thenLocationDtoCorrectContentAndOwnerFlagFalse() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        var result = objectMapper.readValue(response.getContentAsString(), LocationDto.class);

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.OK.value()),
            () -> assertEquals(result.getId(), 1L),
            () -> assertEquals(result.getName(), "test_location_1_name"),
            () -> assertEquals(result.getDescription(), "test_location_1_description"),
            () -> assertEquals(result.getPlz(), null),
            () -> assertEquals(result.getState(), AustriaState.W),
            () -> assertEquals(result.getAddress(), "test_location_1_address"),
            () -> assertTrue(result.getSize().compareTo(new BigDecimal(1000)) == 0),
            () -> assertFalse(result.isCallerIsOwner())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/plz.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateLocation_givenValidData_whenUpdated_thenUpdatedValuesInLocationDto() throws Exception {

        Long locationId = 1L;

        LocationDto newLocationValues = LocationDto.builder()
            .id(locationId)
            .name("test_location_1_name_updated")
            .description("test_location_1_description_updated")
            .plz(PlzDto.builder().plz("3100").build())
            .state(AustriaState.NOE)
            .address("test_location_1_address_updated")
            .size(new BigDecimal(2000))
            .build();

        MvcResult mvcResult = this.mockMvc.perform(put(LOCATION_BASE_URI + "/" + locationId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLocationValues))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), LocationDto.class);

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.OK.value()),
            () -> assertEquals(result.getId(), locationId),
            () -> assertEquals(result.getName(), newLocationValues.getName()),
            () -> assertEquals(result.getDescription(), newLocationValues.getDescription()),
            () -> assertEquals(result.getPlz(), newLocationValues.getPlz()),
            () -> assertEquals(result.getState(), newLocationValues.getState()),
            () -> assertEquals(result.getAddress(), newLocationValues.getAddress()),
            () -> assertTrue(result.getSize().compareTo(newLocationValues.getSize()) == 0)
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateLocation_givenValidDataButRequestNotFromOwner_whenUpdated_thenValidationError() throws Exception {
        long locationId = 1L;

        LocationDto newLocationValues = LocationDto.builder()
            .id(locationId)
            .name("test_location_1_name_updated")
            .description("test_location_1_description_updated")
            .plz(PlzDto.builder().plz("2222").build())
            .state(AustriaState.NOE)
            .address("test_location_1_address_updated")
            .size(new BigDecimal(2000))
            .build();

        MvcResult mvcResult = this.mockMvc.perform(put(LOCATION_BASE_URI + "/" + locationId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER2_USER, LENDER2_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLocationValues))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value()),
            () -> assertTrue(result.getMessage().matches("User with id -\\d is not the owner of the location to update \\(id= \\d\\)"))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateLocation_givenDtoButInvalidId_whenUpdated_thenNotFound() throws Exception {
        LocationDto newLocationValues = LocationDto.builder()
            .name("test_location_1_name_updated")
            .description("test_location_1_description_updated")
            .plz(PlzDto.builder().plz("2222").build())
            .state(AustriaState.NOE)
            .address("test_location_1_address_updated")
            .size(new BigDecimal(2000))
            .build();

        long locationId = -1L;

        MvcResult mvcResult = this.mockMvc.perform(put(LOCATION_BASE_URI + "/" + locationId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLocationValues))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.NOT_FOUND.value()),
            () -> assertTrue(response.getContentAsString().matches("Location with id -\\d not found"))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/plz.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateLocation_givenInvalidDto_whenUpdated_thenValidationError() throws Exception {
        LocationDto newLocationValues = LocationDto.builder()
            .name("    ")
            .description("")
            .plz(PlzDto.builder().plz("-1000").build())
            .state(AustriaState.W)
            .address("")
            .size(new BigDecimal(1000000000))
            .build();

        long locationId = 1L;

        MvcResult mvcResult = this.mockMvc.perform(put(LOCATION_BASE_URI + "/" + locationId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLocationValues))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        List<String> errorMessages = new ArrayList<>();
        result.getErrors().forEach(error -> errorMessages.add(error.getMessage()));

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value()),
            () -> assertEquals(result.getErrors().size(), 4),
            () -> assertTrue(errorMessages.contains("Name darf nicht leer sein.")),
            () -> assertTrue(errorMessages.contains("Addresse darf nicht leer sein.")),
            () -> assertTrue(errorMessages.contains("Größe darf höchstens 100000 betragen.")),
            () -> assertTrue(errorMessages.contains("PLZ muss existieren."))
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void removeLocation_givenCorrectIDFromCorrectUser_thenReturnRemovedLocation() throws Exception {
        var locationId = 1L;
        MvcResult mvcResult = this.mockMvc.perform(delete(LOCATION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(response.getStatus(), HttpStatus.NO_CONTENT.value());

        MvcResult mvcResultGet = this.mockMvc.perform(get(LOCATION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

        MockHttpServletResponse responseGet = mvcResultGet.getResponse();

        assertAll(
            () -> assertEquals(responseGet.getStatus(), HttpStatus.NOT_FOUND.value()),
            () -> assertTrue(responseGet.getContentAsString().matches("Location with id 1 not found"))
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void removeLocation_givenCorrectData_thenLocationAndTransactionNotFoundWhenSearched() throws Exception {
        var locationId = 1L;
        MvcResult mvcResult = this.mockMvc.perform(delete(LOCATION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(response.getStatus(), HttpStatus.NO_CONTENT.value());

        MvcResult mvcResult1 = this.mockMvc.perform(get(LOCATION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

        MockHttpServletResponse response1 = mvcResult1.getResponse();

        assertAll(
            () -> assertEquals(response1.getStatus(), HttpStatus.NOT_FOUND.value()),
            () -> assertTrue(response1.getContentAsString().matches("Location with id 1 not found"))
        );

        MvcResult mvcResult2 = this.mockMvc.perform(get("/transactions/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

        MockHttpServletResponse response2 = mvcResult2.getResponse();

        assertEquals(response2.getStatus(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void removeLocation_givenWrongUser_thenReturns_ValidationError() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(LOCATION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, LENDER_ROLES)))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value()),
            () -> assertTrue(response.getContentAsString().contains("Benutzer mit der id 2 ist nicht der Besitzer der zu löschenden Location (id= 1)"))
        );
    }

}
