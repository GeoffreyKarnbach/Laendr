package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.dto.LoginDto;
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

import static at.ac.tuwien.sepm.groupphase.backend.basetest.TestData.LOGIN_BASE_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LoginEndpointTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql("/sql/user/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void login_givenUserData_whenCredentialsAreValid_then200() throws Exception {
        String validUsername = "lender@email.com";
        String validPassword = "password";
        var content = new LoginDto(validUsername, validPassword);
        String body = objectMapper.writeValueAsString(content);

        // Perform the POST request to the login endpoint
        MvcResult mvcResult = mockMvc.perform(post(LOGIN_BASE_URI)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)).andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/user/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void login_givenUserData_whenCredentialsAreInvalid_then403() throws Exception {
        String validUsername = "lender@email.com";
        String validPassword = "wrong";
        var content = new LoginDto(validUsername, validPassword);
        String body = objectMapper.writeValueAsString(content);

        // Perform the POST request to the login endpoint
        MvcResult mvcResult = mockMvc.perform(post(LOGIN_BASE_URI)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)).andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }
}
