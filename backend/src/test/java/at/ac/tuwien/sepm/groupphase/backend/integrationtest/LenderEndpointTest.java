package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.LenderViewDto;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LenderEndpointTest implements TestData {

    private static final String LENDER_BASE_URI = BASE_URI + "/lender";

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

    @Autowired
    private ApplicationUserRepository userRepository;

    @Test
    @Sql("/sql/lender/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void searchById_givenData_thenWrappedLenderViewDtos() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LENDER_BASE_URI + "/-1"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), LenderViewDto.class);

        assertAll(
            () -> assertEquals(result.getId(), -1L),
            () -> assertEquals(result.getPhone(), "123456"),
            () -> assertEquals(result.getDescription(), "desc1"),
            () -> assertEquals(result.getEmail(), "email@email.com")
        );
    }

    @Test
    @Sql("/sql/lender/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void searchById_givenData_returnsNotFound() throws Exception {
        long id = -4;
        MvcResult mvcResult = this.mockMvc.perform(get(LENDER_BASE_URI + "/" + id))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
            () -> assertTrue(response.getContentAsString().matches("Vermieter mit id " + id + " nicht gefunden"))
        );
    }

    @Test
    @Sql("/sql/user/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void addLenderRole_givenUserData_whenGivenNonexistentUser_thenNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(LENDER_BASE_URI + "/role/1234")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("lender@email.com", LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/user/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void addLenderRole_givenUserData_whenGivenLenderUser_thenConflictException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(LENDER_BASE_URI + "/role/-1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("lender@email.com", LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/user/test_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void addLenderRole_givenUserData_whenGivenNonLenderUser_thenLenderRoleAdded() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(LENDER_BASE_URI + "/role/-3")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("renter2@email.com", LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

        var userEntity = userRepository.findById(-3L);

        assertTrue(userEntity.isPresent());

        assertNotNull(userEntity.get().getLender());
    }

}
