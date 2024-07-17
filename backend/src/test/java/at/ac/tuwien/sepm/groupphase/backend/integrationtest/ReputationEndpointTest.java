package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationSummaryDto;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static at.ac.tuwien.sepm.groupphase.backend.basetest.TestData.ADMIN_ROLES;
import static at.ac.tuwien.sepm.groupphase.backend.basetest.TestData.BASE_URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ReputationEndpointTest {

    private static final String REPUTATION_BASE_URI = BASE_URI + "/ratings";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void getLenderReputations_givenNothing_whenUserNotAdmin_thenDontAllowAccess() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(REPUTATION_BASE_URI + "/summary/lenders"))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getLenderReputations_givenReputationData_whenUserAdmin_thenReturnSuccessfully() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(REPUTATION_BASE_URI + "/summary/lenders")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("TEST_admin", ADMIN_ROLES)))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<ReputationSummaryDto> summaryDtos = result.getResult().stream().map(s -> objectMapper.convertValue(s, ReputationSummaryDto.class)).toList();

        assertThat(summaryDtos).anySatisfy(s -> assertThat(s.getSubject()).isEqualTo("TEST_lender"));
    }

    @Test
    public void getRenterReputations_givenNothing_whenUserNotAdmin_thenDontAllowAccess() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(REPUTATION_BASE_URI + "/summary/renters"))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getRenterReputations_givenReputationData_whenUserAdmin_thenReturnSuccessfully() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(REPUTATION_BASE_URI + "/summary/renters")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("TEST_admin", ADMIN_ROLES)))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<ReputationSummaryDto> summaryDtos = result.getResult().stream().map(s -> objectMapper.convertValue(s, ReputationSummaryDto.class)).toList();

        assertThat(summaryDtos).anySatisfy(s -> assertThat(s.getSubject()).isEqualTo("TEST_renter"));
    }

    @Test
    public void getLocationReputations_givenNothing_whenUserNotAdmin_thenDontAllowAccess() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(REPUTATION_BASE_URI + "/summary/locations"))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getLocationReputations_givenReputationData_whenUserAdmin_thenReturnSuccessfully() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(REPUTATION_BASE_URI + "/summary/locations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("TEST_admin", ADMIN_ROLES)))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<ReputationSummaryDto> summaryDtos = result.getResult().stream().map(s -> objectMapper.convertValue(s, ReputationSummaryDto.class)).toList();

        assertThat(summaryDtos).anySatisfy(s -> assertThat(s.getSubject()).isEqualTo("TEST_location"));
    }

}
