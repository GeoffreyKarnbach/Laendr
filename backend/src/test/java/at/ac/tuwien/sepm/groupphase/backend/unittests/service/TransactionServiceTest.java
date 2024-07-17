package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCreateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.TransactionService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TransactionServiceTest {

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private TimeslotRepository timeslotRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private RenterRepository renterRepository;

    @Autowired
    private TransactionService transactionService;

    @Test
    public void createLocation_givenData_whenCreated_thenTransactionWithAllInformation() {

        long timeslotId = 5L;
        long userId = 4L;
        long lenderId = 3L;

        String description = "test";
        String user = "user5@email.com";

        var timeslot = Timeslot.builder()
            .id(timeslotId)
            .owningLocation(Location.builder()  //necessary for validation
                .owner(Lender.builder()
                    .id(lenderId)
                    .build())
                .build())
            .build();

        var applicationUser = ApplicationUser.builder()
            .id(userId)
            .email(user)
            .build();

        var renter = Renter.builder()
            .id(userId)
            .build();

        var transactionCreateDto = TransactionCreateDto.builder()
            .timeslotId(timeslotId)
            .initialMessage(description)
            .build();

        when(timeslotRepository.findById(timeslotId)).thenReturn(Optional.of(timeslot));
        when(userService.findApplicationUserByEmail(user)).thenReturn(applicationUser);
        when(renterRepository.findById(userId)).thenReturn(Optional.ofNullable(renter));
        when(transactionRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Transaction.class);
            arg.setId(1L);
            arg.setCreatedAt(LocalDateTime.now());
            return arg;
        });

        TransactionDto transactionDto = transactionService.startTransaction(transactionCreateDto, user);

        assertAll(
            () -> assertEquals(transactionDto.getTimeslot().getId(), timeslot.getId()),
            () -> assertEquals(transactionDto.getId(), 1L),
            () -> assertEquals(transactionDto.getInitialMessage(), description),
            () -> assertNotNull(transactionDto.getCreatedAt())
        );
    }

    @Test
    public void createLocation_givenWrongData_thenReturnedWithValidationError() {

        long timeslotId = 5L;
        long userId = 4L;

        String description = "";
        String user = "user5@email.com";

        var timeslot = Timeslot.builder()
            .id(timeslotId)
            .owningLocation(Location.builder()  //necessary for validation
                .owner(Lender.builder()
                    .id(userId)
                    .build())
                .build())
            .build();

        var applicationUser = ApplicationUser.builder()
            .id(userId)
            .email(user)
            .build();

        var renter = Renter.builder()
            .id(userId)
            .build();

        var transactionCreateDto = TransactionCreateDto.builder()
            .timeslotId(timeslotId)
            .initialMessage(description)
            .build();

        when(timeslotRepository.findById(timeslotId)).thenReturn(Optional.of(timeslot));
        when(userService.findApplicationUserByEmail(user)).thenReturn(applicationUser);
        when(renterRepository.findById(userId)).thenReturn(Optional.ofNullable(renter));

        ValidationException thrown = assertThrows(ValidationException.class, () -> transactionService.startTransaction(transactionCreateDto, user));


        List<String> messages = new ArrayList<>();
        for (ValidationErrorDto error : thrown.getValidationErrorRestDto().getErrors()) {
            messages.add(error.getMessage());
        }
        assertAll(
            () -> assertEquals(thrown.getValidationErrorRestDto().getErrors().size(), 1),
            () -> assertTrue(messages.contains("Mieter darf nicht der Besitzer der Location sein."))
        );
    }

    @Test
    public void createLocation_givenWrongId_thenNotFoundException() {
        long timeslotId = 6L;

        var transactionCreateDto = TransactionCreateDto.builder()
            .timeslotId(timeslotId)
            .initialMessage("test")
            .build();

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> transactionService.startTransaction(transactionCreateDto, null));

        assertTrue(thrown.getMessage().contains("No timeslot with id " + timeslotId + " found"));
    }

}
