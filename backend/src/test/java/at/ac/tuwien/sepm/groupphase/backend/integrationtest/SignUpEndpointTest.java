package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
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

import java.util.ArrayList;
import java.util.List;

import static at.ac.tuwien.sepm.groupphase.backend.basetest.TestData.BASE_URI;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class SignUpEndpointTest {

    private static final String SIGNUP_BASE_URI = BASE_URI + "/signup";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void signUp_givenData_whenValidData_thenJWTToken() throws Exception {

        SignUpDto signUpDto = SignUpDto.builder()
            .username("testuser")
            .email("test.sign-up@email.com")
            .originalPassword("password")
            .repeatedPassword("password")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(SIGNUP_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = response.getContentAsString();

        assertTrue(result != null);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

    }

    @Test
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void signUp_givenData_whenInvalidData_thenValidationError() throws Exception {

        SignUpDto signUpDto = SignUpDto.builder()
            .username("")
            .email("")
            .originalPassword("aaaa")
            .repeatedPassword("bbb")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(SIGNUP_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        List<String> errorMessages = new ArrayList<>();
        for (var error : result.getErrors()) {
            errorMessages.add(error.getMessage());
        }

        assertAll(
            () -> assertTrue(result != null),
            () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus()),
            () -> assertTrue(errorMessages.contains("Username darf nicht leer sein")),
            () -> assertTrue(errorMessages.contains("Email darf nicht leer sein")),
            () -> assertTrue(errorMessages.contains("Passwörter stimmen nicht überein."))
        );
    }
}
