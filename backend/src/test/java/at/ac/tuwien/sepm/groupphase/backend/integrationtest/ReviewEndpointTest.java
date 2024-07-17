package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCountDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ReviewEndpointTest implements TestData {

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

    private static final String REVIEW_BASE_URI = BASE_URI + "/reviews";

    private final ReviewCreationDto creationDto = ReviewCreationDto.builder()
        .transactionId(-1L).rating(3).comment("Comment").build();

    @Test
    @Sql("/sql/review/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void searchByLocationId_givenCorrectRequest_thenReturnsPageableWithReview() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(REVIEW_BASE_URI + "/search")
                .queryParam("page", "0")
                .queryParam("pageSize", "5")
                .queryParam("id", "-1")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<ReviewDto> reviewDtos = ((List) result.getResult()).stream().map(item -> objectMapper.convertValue(item, ReviewDto.class)).toList();

        assertAll(
            () -> assertEquals(result.getTotalResults(), 1),
            () -> assertEquals(result.getResultCount(), 1),
            () -> assertEquals(reviewDtos.size(), 1),
            () -> assertEquals(reviewDtos.get(0).getRating(), 2),
            () -> assertNotNull(reviewDtos.get(0).getCreatedAt()),
            () -> assertEquals(reviewDtos.get(0).getComment(), "comment")
        );
    }

    @Test
    @Sql("/sql/review/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void searchByLocationId_givenRequestForNonExistingLocation_thenReturnsEmptyList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(REVIEW_BASE_URI + "/search")
                .queryParam("page", "0")
                .queryParam("pageSize", "5")
                .queryParam("id", "-96723")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);

        assertAll(
            () -> assertEquals(result.getResultCount(), 0),
            () -> assertEquals(result.getTotalPages(), 0)
        );
    }

    @Test
    @Sql("/sql/review/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createLocationReview_givenValidCreationDto_whenReviewAlreadyExists_then409() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(REVIEW_BASE_URI + "/location")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("test3@mail.com", RENTER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorRestDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CONFLICT.value(), response.getStatus()),
            () -> assertEquals("Bewertung der Location im Kontext der Transaktion existiert bereits", result.getMessage())
        );
    }

    @Test
    @Sql("/sql/review/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createLocationReview_givenValidCreationDto_whenUserIsNotRenter_then403() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(REVIEW_BASE_URI + "/location")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("wrong_user@mail.com", RENTER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus()),
            () -> assertEquals("Benutzer ist nicht Mieter in der Transaktion.", response.getContentAsString())
        );
    }

    @Test
    @Sql("/sql/review/test_data.sql")
    @Transactional
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createRenterReview_givenValidCreationDto_whenCreating_then204ReturnCreatedReview() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(REVIEW_BASE_URI + "/renter")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("test1@mail.com", LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDto)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ReviewDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), response.getStatus()),
            () -> assertEquals(creationDto.getRating(), result.getRating()),
            () -> assertEquals(creationDto.getComment(), result.getComment())
        );
    }

    @Test
    @Sql("/sql/review/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void createRenterReview_givenValidCreationDto_whenTransactionDoesNotExist_then404() throws Exception {
        creationDto.setTransactionId(-5L);

        MvcResult mvcResult = mockMvc.perform(post(REVIEW_BASE_URI + "/renter")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("test1@mail.com", LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDto)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
            () -> assertEquals("Transaktion mit Id -5 existiert nicht.", response.getContentAsString())
        );
    }

    @Test
    @Sql("/sql/review/extended_review_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getReviewCount_whenUserIsValid_thenReturnCorrectValues() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(REVIEW_BASE_URI + "/count")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("test3@mail.com", RENTER_ROLES)))
            .andDo(print()).andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ReviewCountDto.class);

        assertAll(
            () -> assertEquals(1, result.getCompletedTransactions()),
            () -> assertEquals(1, result.getReviews())
        );
    }
}
