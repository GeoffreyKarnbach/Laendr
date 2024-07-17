package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCancelDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCreateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDetailDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TransactionEndpointTest implements TestData {

    private static final String TRANSACTION_BASE_URI = BASE_URI + "/transactions";

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
    private TransactionRepository transactionRepository;

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void startTransaction_givenCorrectData_thenReturnsTransactionDto() throws Exception {
        String initialMessage = "initMessage";
        long timeslotId = 1;

        TransactionCreateDto transactionCreateDto = TransactionCreateDto.builder()
            .timeslotId(timeslotId)
            .initialMessage(initialMessage)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionCreateDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), TransactionDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), response.getStatus()),
            () -> assertEquals(result.getInitialMessage(), initialMessage),
            () -> assertEquals(result.getTimeslot().getId(), timeslotId),
            () -> assertNotNull(result.getCreatedAt()),
            () -> assertFalse(result.getCancelled()),
            () -> assertEquals(result.getTotalPaid(), BigDecimal.ZERO)
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void startTransaction_givenNonExistentTimeslot_thenThrowsNotFoundException() throws Exception {
        String initialMessage = "initMessage";
        long timeslotId = 2;

        TransactionCreateDto transactionCreateDto = TransactionCreateDto.builder()
            .timeslotId(timeslotId)
            .initialMessage(initialMessage)
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionCreateDto))
            )
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(response.getStatus(), HttpStatus.NOT_FOUND.value()),
            () -> assertEquals(response.getContentAsString(), "No timeslot with id " + timeslotId + " found")
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void find_givenData_whenInvolvedUser_thenTransactionDto() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), TransactionDto.class);

        assertEquals("test_location_1", result.getLocationName());
        assertEquals("test_lender_1", result.getPartnerName());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void find_givenData_whenUninvolvedUser_then403() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/transaction/cancelled_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void recordCancelNotified_givenData_whenCancelledTransaction_thenFlagSet() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/cancel-notification")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
        assertEquals(true, transactionRepository.findById(1L).get().getCancelNotified());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findAllByStatusForRole_givenDataForActive_whenActive_thenTransactionDtos() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/all/ACTIVE")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .queryParam("role", "ROLE_RENTER")
                .queryParam("page", "0")
                .queryParam("pageSize", "1"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<TransactionDto> dtos = result.getResult().stream().map(item -> objectMapper.convertValue(item, TransactionDto.class)).toList();

        assertEquals("test_location_1", dtos.get(0).getLocationName());
        assertEquals("test_lender_1", dtos.get(0).getPartnerName());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findAllByStatusForRole_givenDataForActive_whenCompleted_thenNoDtos() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/all/COMPLETED")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .queryParam("role", "ROLE_RENTER")
                .queryParam("page", "0")
                .queryParam("pageSize", "1"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);

        assertEquals(0, result.getTotalResults());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findAllByStatusForRole_givenDataForActive_whenWrongRole_thenNoDtos() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/all/COMPLETED")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .queryParam("role", "ROLE_LENDER")
                .queryParam("page", "0")
                .queryParam("pageSize", "1"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);

        assertEquals(0, result.getTotalResults());
    }

    @Test
    @Sql("/sql/transaction/cancelled_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getIdsForOutstandingCancelNotificationsForRole_givenDataForCancelled_whenRightRole_thenIds() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/all/cancel-notifications")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .queryParam("role", "ROLE_RENTER"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        // JsonMapper sees Integer and not Long
        List<Integer> result = objectMapper.readValue(response.getContentAsString(), List.class);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void countAllNotReviewedForRole_givenNoDataForCompleted_whenRightRole_then0() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/all/not-reviewed")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES))
                .queryParam("role", "ROLE_RENTER"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), Integer.class);

        assertEquals(0, result);
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void completeTransaction_givenValidDataFromLender_whenCompleted_thenTransactionCompleted() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/complete")
                .param("price", new BigDecimal(100).toString())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

        MvcResult mvcResult2 = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response2 = mvcResult2.getResponse();
        var result = objectMapper.readValue(response2.getContentAsString(), TransactionDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response2.getStatus()),
            () -> assertNotNull(result.getCompletedAt()),
            () -> assertEquals(0, result.getTotalPaid().compareTo(new BigDecimal(100)))
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void completeTransaction_givenValidDataFromRenter_whenAttempted_then403Forbidden() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/complete")
                .param("price", new BigDecimal(100).toString())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void completeTransaction_givenNegativePrice_whenAttempted_then422ValidationError() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/complete")
                .param("price", new BigDecimal(-100).toString())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus()),
            () -> assertEquals("Preis darf nicht negativ sein.", result.getErrors().get(0).getMessage())
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransaction_givenValidReason_whenLenderAttempted_thenTransactionCancelled() throws Exception {
        TransactionCancelDto transactionCancelDto = TransactionCancelDto.builder()
            .transactionId(1L)
            .cancelReason(CancelReason.NO_INTEREST)
            .cancelMessage("No interest")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/cancel")
                .content(objectMapper.writeValueAsString(transactionCancelDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        MvcResult mvcResult2 = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response2 = mvcResult2.getResponse();
        var result = objectMapper.readValue(response2.getContentAsString(), TransactionDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response2.getStatus()),
            () -> assertEquals(CancelReason.NO_INTEREST, result.getCancelReason()),
            () -> assertEquals("No interest", result.getCancelDescription()),
            () -> assertEquals(AppRole.ROLE_LENDER, result.getCancelByRole())
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransaction_givenValidReason_whenRenterAttempted_thenTransactionCancelled() throws Exception {
        TransactionCancelDto transactionCancelDto = TransactionCancelDto.builder()
            .transactionId(1L)
            .cancelReason(CancelReason.SCAM)
            .cancelMessage("Scam")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/cancel")
                .content(objectMapper.writeValueAsString(transactionCancelDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        MvcResult mvcResult2 = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response2 = mvcResult2.getResponse();
        var result = objectMapper.readValue(response2.getContentAsString(), TransactionDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response2.getStatus()),
            () -> assertEquals(CancelReason.SCAM, result.getCancelReason()),
            () -> assertEquals("Scam", result.getCancelDescription()),
            () -> assertEquals(AppRole.ROLE_RENTER, result.getCancelByRole())
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql("/sql/user/additional_user.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransaction_givenValidReason_whenNotRenterAndNotLenderAttempts_thenForbidden() throws Exception {
        TransactionCancelDto transactionCancelDto = TransactionCancelDto.builder()
            .transactionId(1L)
            .cancelReason(CancelReason.SCAM)
            .cancelMessage("Scam")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/cancel")
                .content(objectMapper.writeValueAsString(transactionCancelDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER2_USER, LENDER2_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus()),
            () -> assertEquals("User is not involved in this transaction", response.getContentAsString())
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransaction_givenBlankMessage_whenRenterAttempted_thenValidationError() throws Exception {
        TransactionCancelDto transactionCancelDto = TransactionCancelDto.builder()
            .transactionId(1L)
            .cancelReason(CancelReason.SCAM)
            .cancelMessage("     ")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/cancel")
                .content(objectMapper.writeValueAsString(transactionCancelDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);
        List<String> errorMessages = new ArrayList<>();

        for (ValidationErrorDetailDto error : result.getErrors()) {
            errorMessages.add(error.getMessage());
        }

        assertAll(
            () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus()),
            () -> assertTrue(errorMessages.contains("Begründung darf nicht leer sein."))
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransaction_givenNonExistentTransactionId_whenAttempted_thenNotFoundException() throws Exception {
        Long nonExistentTransactionId = 101L;

        TransactionCancelDto transactionCancelDto = TransactionCancelDto.builder()
            .transactionId(nonExistentTransactionId)
            .cancelReason(CancelReason.SCAM)
            .cancelMessage("Scam")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/cancel")
                .content(objectMapper.writeValueAsString(transactionCancelDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
            () -> assertEquals("Could not find transaction with id " + nonExistentTransactionId, response.getContentAsString())
        );
    }

    @Test
    @Sql("/sql/transaction/cancelled_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransaction_givenCancelledTransaction_whenAttempted_thenValidationError() throws Exception {
        TransactionCancelDto transactionCancelDto = TransactionCancelDto.builder()
            .transactionId(1L)
            .cancelReason(CancelReason.SCAM)
            .cancelMessage("Scam")
            .build();

        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/cancel")
                .content(objectMapper.writeValueAsString(transactionCancelDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        List<String> errorMessages = new ArrayList<>();

        for (ValidationErrorDetailDto error : result.getErrors()) {
            errorMessages.add(error.getMessage());
        }
        assertAll(
            () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus()),
            () -> assertTrue(errorMessages.contains("Transaktion ist bereits abgebrochen."))
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransactionsForTimeslotAsRenter_givenInvalidTimeslotId_whenUserIsRenter_then409() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(TRANSACTION_BASE_URI + "/timeslot/5")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void cancelTransactionsForTimeslotAsRenter_givenTimeslotId_whenUserIsRenter_then204AndTransactionCancelled() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(TRANSACTION_BASE_URI + "/timeslot/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus()),
            () -> assertEquals(1, transactionRepository.findAll().stream().filter(Transaction::getCancelled).count())
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findTransactionsForTimeslot_givenValidTimeslotId_whenUserIsOwningLender_then200AndPageOfTransactions() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/timeslot/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .queryParam("page", "0")
                .queryParam("pageSize", "1"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<TransactionDto> dtos = result.getResult().stream().map(item -> objectMapper.convertValue(item, TransactionDto.class)).toList();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(1, result.getTotalResults()),
            () -> assertEquals(1, result.getTotalPages()),
            () -> assertEquals(1, dtos.get(0).getId())
        );
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findTransactionsForTimeslot_givenNonExistingTimeslot_whenUserIsOwningLender_then404() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/timeslot/5")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .queryParam("page", "0")
                .queryParam("pageSize", "1"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/transaction/active_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findTransactionsForTimeslot_givenValidTimeslotId_whenUserIsNotOwningLender_then409() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/timeslot/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER2_USER, LENDER2_ROLES))
                .queryParam("page", "0")
                .queryParam("pageSize", "1"))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    @Sql("/sql/transaction/accept_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void acceptTransaction_givenValidRequest_whenUserIsLocationOwner_thenAcceptedAndOthersRefused() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/accept")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

        List<TransactionDto> transactions = new ArrayList<>();

        MvcResult mvcResult2 = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/1")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        TransactionDto acceptedTransaction = objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), TransactionDto.class);

        for (int i = 2; i <= 4; i++){
            MvcResult mvcResult3 = this.mockMvc.perform(get(TRANSACTION_BASE_URI + "/" + i)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
                .andDo(print())
                .andReturn();

            transactions.add(objectMapper.readValue(mvcResult3.getResponse().getContentAsString(), TransactionDto.class));
        }

        assertAll(
            () -> assertTrue(acceptedTransaction.getTimeslot().getIsUsed()),
            () -> assertTrue(transactions.get(0).getCancelled() == null || transactions.get(0).getCancelled()),
            () -> assertEquals("Slot an anderen Benutzer vergeben", transactions.get(0).getCancelDescription()),
            () -> assertTrue(transactions.get(1).getCancelled() == null || transactions.get(1).getCancelled()),
            () -> assertEquals("Slot an anderen Benutzer vergeben", transactions.get(1).getCancelDescription()),
            () -> assertTrue(transactions.get(2).getCancelled() == null || transactions.get(2).getCancelled()),
            () -> assertEquals("Slot an anderen Benutzer vergeben", transactions.get(2).getCancelDescription())
        );

    }

    @Test
    @Sql("/sql/transaction/accept_transaction.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void acceptTransaction_givenValidRequest_whenUserIsNotLocationOwner_then403() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(TRANSACTION_BASE_URI + "/1/accept")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(RENTER_USER, RENTER_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }
}
