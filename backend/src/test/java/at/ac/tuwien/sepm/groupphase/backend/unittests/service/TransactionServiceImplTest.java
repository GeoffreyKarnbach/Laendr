package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.TransactionStatus;
import at.ac.tuwien.sepm.groupphase.backend.exception.AccessForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.ReputationMapper;
import at.ac.tuwien.sepm.groupphase.backend.mapper.TransactionMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.AdminRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.TransactionService;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TransactionServiceImplTest implements TestData {

    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private AdminRepository adminRepository;
    @MockBean
    private TimeslotRepository timeslotRepository;

    @MockBean
    private ReputationMapper reputationMapper;
    @MockBean
    private TransactionMapper transactionMapper;

    @Autowired
    private TransactionService transactionService;

    @Test
    void findOne_givenNoTransaction_whenQuerying_thenNotFoundException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("", null));

            when(transactionRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> transactionService.findOne(1L));
        }
    }

    @Test
    void findOne_givenTransaction_whenUserNotInvolved_thenAccessForbiddenException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("user@email.com", null));

            var transaction = Transaction.builder()
                .renter(Renter.builder().owner(ApplicationUser.builder().email("renter@email.com").build()).build())
                .timeslot(
                    Timeslot.builder().owningLocation(
                            Location.builder().owner(
                                    Lender.builder().owner(ApplicationUser.builder().email("lender@email.com").build()).build())
                                .build())
                        .build())
                .build();
            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));
            when(adminRepository.existsByOwnerEmail(any())).thenReturn(false);

            assertThrows(AccessForbiddenException.class, () -> transactionService.findOne(1L));
        }
    }

    @Test
    void findOne_givenTransaction_whenLender_thenDto() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            var transaction = Transaction.builder()
                .renter(Renter.builder().owner(ApplicationUser.builder().email("renter@email.com").build()).build())
                .timeslot(
                    Timeslot.builder().owningLocation(
                            Location.builder().owner(
                                    Lender.builder().owner(ApplicationUser.builder().email("lender@email.com").build()).build())
                                .build())
                        .build())
                .build();
            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));
            when(adminRepository.existsByOwnerEmail(any())).thenReturn(false);

            when(transactionMapper.entityToDto(any())).thenReturn(new TransactionDto());
            when(reputationMapper.entityToDto(any())).thenReturn(new ReputationDto());

            var result = transactionService.findOne(1L);

            assertEquals("Anonymer Mieter", result.getPartnerName());
            assertEquals(AppRole.ROLE_LENDER, result.getOwnRoleInTransaction());

            verify(transactionMapper).entityToDto(any());
            verify(reputationMapper).entityToDto(any());
        }
    }

    @Test
    void findOne_givenTransaction_whenRenter_thenDto() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));

            var transaction = Transaction.builder()
                .renter(Renter.builder().owner(ApplicationUser.builder().email("renter@email.com").build()).build())
                .timeslot(
                    Timeslot.builder().owningLocation(
                            Location.builder().owner(
                                    Lender.builder().owner(ApplicationUser.builder().email("lender@email.com").build()).build())
                                .build())
                        .build())
                .build();
            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));
            when(adminRepository.existsByOwnerEmail(any())).thenReturn(false);

            when(transactionMapper.entityToDto(any())).thenReturn(new TransactionDto());
            when(reputationMapper.entityToDto(any())).thenReturn(new ReputationDto());

            var result = transactionService.findOne(1L);

            assertEquals("Anonymer Vermieter", result.getPartnerName());
            assertEquals(AppRole.ROLE_RENTER, result.getOwnRoleInTransaction());

            verify(transactionMapper).entityToDto(any());
            verify(reputationMapper).entityToDto(any());
        }
    }

    @Test
    void recordCancelNotified_givenTransaction_whenCancelled_thenSavedEntity() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));

            var transaction = Transaction.builder()
                .renter(Renter.builder().owner(ApplicationUser.builder().email("renter@email.com").build()).build())
                .timeslot(
                    Timeslot.builder().owningLocation(
                            Location.builder().owner(
                                    Lender.builder().owner(ApplicationUser.builder().email("lender@email.com").build()).build())
                                .build())
                        .build())
                .cancelled(true)
                .build();
            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));
            when(adminRepository.existsByOwnerEmail(any())).thenReturn(false);

            transactionService.recordCancelNotified(1L);

            var captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertEquals(true, captor.getValue().getCancelNotified());
        }
    }

    @Test
    void findAllByStatusForRole_givenData_whenValidUser_thenDto() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));

            var transaction = Transaction.builder()
                .renter(Renter.builder().owner(ApplicationUser.builder().email("renter@email.com").build()).build())
                .timeslot(
                    Timeslot.builder().owningLocation(
                            Location.builder().owner(
                                    Lender.builder().owner(ApplicationUser.builder().email("lender@email.com").build()).build())
                                .build())
                        .build())
                .build();

            var repoResult = mock(Page.class);
            when(repoResult.getTotalElements()).thenReturn(1L);
            when(repoResult.getTotalPages()).thenReturn(1);
            when(repoResult.getNumberOfElements()).thenReturn(1);
            when(repoResult.stream()).thenReturn(Stream.of(transaction));

            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));
            when(transactionRepository.findAllActiveForUserByRole(any(), any(), anyBoolean(), any())).thenReturn(repoResult);
            when(adminRepository.existsByOwnerEmail(any())).thenReturn(false);

            when(transactionMapper.entityToDto(any())).thenReturn(new TransactionDto());
            when(reputationMapper.entityToDto(any())).thenReturn(new ReputationDto());

            var resultPage = transactionService.findAllByStatusForRole(TransactionStatus.ACTIVE, AppRole.ROLE_RENTER, 0, 1);
            var result = resultPage.getResult().get(0);

            assertEquals("Anonymer Vermieter", result.getPartnerName());
            assertEquals(AppRole.ROLE_RENTER, result.getOwnRoleInTransaction());

            verify(transactionMapper).entityToDto(any());
            verify(reputationMapper).entityToDto(any());
        }
    }

    @Test
    void getIdsOutstandingCancelNotificationsForRole_givenData_whenValidUser_thenList() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));

            when(transactionRepository.findAllIdsForCancelledAndNotNotified(any(), any(), any())).thenReturn(new ArrayList<>());

            var result = transactionService.getIdsOutstandingCancelNotificationsForRole(AppRole.ROLE_RENTER);

            assertEquals(0, result.size());
        }
    }

    @Test
    void countAllNotReviewedForRole_givenData_whenValidUser_thenCount() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));

            when(transactionRepository.countAllNotReviewed(any(), any())).thenReturn(0);

            var result = transactionService.countAllNotReviewedForRole(AppRole.ROLE_RENTER);

            assertEquals(0, result);
        }
    }

    @Test
    void cancelTransactionsForTimeslotAsRenter_givenTimeslotId_whenNoUser_thenAccessForbiddenException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(null);

            assertThrows(AccessForbiddenException.class, () -> transactionService.cancelTransactionsForTimeslotAsRenter(1L));
        }
    }

    @Test
    void cancelTransactionsForTimeslotAsRenter_givenInvalidTimeslotId_whenRenterUser_thenAccessForbiddenException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));
            when(timeslotRepository.findById(1L)).thenReturn(Optional.empty());

            var exception = assertThrows(ConflictException.class, () -> transactionService.cancelTransactionsForTimeslotAsRenter(1L));

            assertEquals("Zeitfenster mit Id 1 existiert nicht", exception.getMessage());
        }
    }

    @Test
    void findAllTransactionsForTimeslot_givenTimeslotId_whenCorrectLenderUser_thenPageOfTransaction() {

        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            var timeslot = Timeslot.builder().owningLocation(
                    Location.builder().owner(
                            Lender.builder().owner(ApplicationUser.builder().email("lender@email.com").build()).build())
                        .build())
                .build();

            var transaction = Transaction.builder()
                .id(2L)
                .renter(Renter.builder().owner(ApplicationUser.builder().email("renter@email.com").build()).build())
                .timeslot(timeslot)
                .cancelled(false)
                .build();

            var repoResult = mock(Page.class);
            when(repoResult.getTotalElements()).thenReturn(1L);
            when(repoResult.getTotalPages()).thenReturn(1);
            when(repoResult.getNumberOfElements()).thenReturn(1);
            when(repoResult.stream()).thenReturn(Stream.of(transaction));

            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));
            when(transactionRepository.findAllOngoingByTimeslotId(any(), any())).thenReturn(repoResult);
            when(timeslotRepository.findById(any())).thenReturn(Optional.of(timeslot));

            when(transactionMapper.entityToDto(any())).thenReturn(TransactionDto.builder().id(2L).build());
            when(reputationMapper.entityToDto(any())).thenReturn(new ReputationDto());

            var result = transactionService.findAllTransactionsForTimeslot(2L, 0, 1);

            assertAll(
                () -> assertEquals(1, result.getTotalResults()),
                () -> assertEquals(1, result.getTotalPages()),
                () -> assertEquals(2, result.getResult().get(0).getId())
            );
        }
    }

    @Test
    void findAllTransactionsForTimeslot_givenTimeslotId_whenUserWrongLender_thenConflictException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            var timeslot = Timeslot.builder().owningLocation(
                    Location.builder().owner(
                            Lender.builder().owner(ApplicationUser.builder().email("lender_wrong@email.com").build()).build())
                        .build())
                .build();

            when(timeslotRepository.findById(any())).thenReturn(Optional.of(timeslot));

            assertThrows(ConflictException.class, () -> transactionService.findAllTransactionsForTimeslot(2L, 0, 1));
        }
    }

    @Test
    void findAllTransactionsForTimeslot_givenInvalidTimeslotId_whenUserCorrectLender_thenNotFoundException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            when(timeslotRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> transactionService.findAllTransactionsForTimeslot(2L, 0, 1));
        }
    }
}
