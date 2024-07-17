package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLender;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationRenter;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewRenter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.exception.AccessForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import at.ac.tuwien.sepm.groupphase.backend.service.ReviewService;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ReviewServiceImplTest {

    @MockBean
    private ReviewLocationRepository reviewLocationRepository;

    @MockBean
    private ReviewRenterRepository reviewRenterRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @Autowired
    private ReviewService reviewService;

    @MockBean
    private ReputationService reputationService;

    private final ReviewCreationDto creationDto = ReviewCreationDto.builder().transactionId(1L).rating(3).comment("Comment").build();

    private final Transaction transaction = Transaction.builder()
        .id(1L)
        .renter(Renter.builder().id(1L).reputation(ReputationRenter.builder().build()).owner(ApplicationUser.builder().email("renter@email.com").build()).build())
        .timeslot(
            Timeslot.builder().owningLocation(
                    Location.builder()
                        .id(1L)
                        .reputation(ReputationLocation.builder().build())
                        .owner(
                            Lender.builder().id(1L).reputation(
                                ReputationLender.builder().build()
                            ).owner(ApplicationUser.builder().email("lender@email.com").build()).build())
                        .build())
                .build())
        .build();

    @Test
    void getAllByLocationId_givenCorrectData_returnsPageWithReview() {
        long locationId = -1;
        int page = 0;
        int pageSize = 5;

        LocalDateTime date = LocalDateTime.now();
        String comment = "comment";
        int rating = 4;

        var review = ReviewLocation.builder()
            .comment(comment)
            .createdAt(date)
            .rating(rating)
            .build();

        Page<ReviewLocation> pageResult = new PageImpl<>(List.of(review));

        when(reviewLocationRepository.findAllByLocationIdPage(locationId, PageRequest.of(page, pageSize))).thenReturn(pageResult);

        var result = reviewService.getAllByLocationId(locationId, page, pageSize);

        var reviewDtoResult = result.getResult().get(0);

        assertAll(
            () -> assertEquals(result.getTotalResults(), 1),
            () -> assertEquals(result.getTotalPages(), 1),
            () -> assertNotNull(reviewDtoResult),
            () -> assertEquals(reviewDtoResult.getComment(), comment),
            () -> assertEquals(reviewDtoResult.getCreatedAt(), date),
            () -> assertEquals(reviewDtoResult.getRating(), rating)
        );
    }

    @Test
    public void createLocationReview_givenValidCreationDto_whenCreatingReview_thenPersistedReview() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));

            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));

            doNothing().when(reputationService).updateReputationForLender(1L);
            doNothing().when(reputationService).updateReputationForLocation(1L);

            when(reviewLocationRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, ReviewLocation.class);
                arg.setId(1L);
                arg.setRating(creationDto.getRating());
                arg.setComment(creationDto.getComment());
                arg.setCreatedAt(LocalDateTime.now());
                return arg;
            });

            var persistedReview = reviewService.createLocationReview(creationDto);

            assertAll(
                () -> assertEquals(creationDto.getRating(), persistedReview.getRating()),
                () -> assertEquals(creationDto.getComment(), persistedReview.getComment())
            );
        }
    }

    @Test
    public void createLocationReview_givenValidCreationDto_whenUserIsNotRenter_thenAccessForbiddenException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter_wrong@email.com", null));

            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));

            doNothing().when(reputationService).updateReputationForLender(1L);
            doNothing().when(reputationService).updateReputationForLocation(1L);

            when(reviewLocationRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, ReviewLocation.class);
                arg.setId(1L);
                arg.setRating(creationDto.getRating());
                arg.setComment(creationDto.getComment());
                arg.setCreatedAt(LocalDateTime.now());
                return arg;
            });

            var exception = assertThrows(AccessForbiddenException.class, () -> reviewService.createLocationReview(creationDto));
            assertEquals("Benutzer ist nicht Mieter in der Transaktion.", exception.getMessage());
        }
    }

    @Test
    public void createLocationReview_givenValidCreationDto_whenTransactionDoesNotExist_thenNotFoundException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("renter@email.com", null));

            when(transactionRepository.findById(any())).thenReturn(Optional.empty());

            doNothing().when(reputationService).updateReputationForLender(1L);
            doNothing().when(reputationService).updateReputationForLocation(1L);

            when(reviewLocationRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, ReviewLocation.class);
                arg.setId(1L);
                arg.setRating(creationDto.getRating());
                arg.setComment(creationDto.getComment());
                arg.setCreatedAt(LocalDateTime.now());
                return arg;
            });

            var exception = assertThrows(NotFoundException.class, () -> reviewService.createLocationReview(creationDto));
            assertEquals("Transaktion mit Id 1 existiert nicht.", exception.getMessage());
        }
    }

    @Test
    public void createRenterReview_givenValidCreationDto_whenCreatingReview_thenPersistedReview() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));

            doNothing().when(reputationService).updateReputationForRenter(1L);

            when(reviewRenterRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, ReviewRenter.class);
                arg.setId(1L);
                arg.setRating(creationDto.getRating());
                arg.setComment(creationDto.getComment());
                arg.setCreatedAt(LocalDateTime.now());
                return arg;
            });

            var persistedReview = reviewService.createRenterReview(creationDto);

            assertAll(
                () -> assertEquals(creationDto.getRating(), persistedReview.getRating()),
                () -> assertEquals(creationDto.getComment(), persistedReview.getComment())
            );
        }
    }

    @Test
    public void createRenterReview_givenValidCreationDto_whenUserIsNotLender_thenAccessForbiddenException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender_wrong@email.com", null));

            when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));

            doNothing().when(reputationService).updateReputationForRenter(1L);

            when(reviewRenterRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, ReviewRenter.class);
                arg.setId(1L);
                arg.setRating(creationDto.getRating());
                arg.setComment(creationDto.getComment());
                arg.setCreatedAt(LocalDateTime.now());
                return arg;
            });

            var exception = assertThrows(AccessForbiddenException.class, () -> reviewService.createRenterReview(creationDto));
            assertEquals("Benutzer ist nicht Vermieter in der Transaktion.", exception.getMessage());
        }
    }

    @Test
    public void createRenterReview_givenValidCreationDto_whenTransactionDoesNotExist_thenNotFoundException() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            when(transactionRepository.findById(any())).thenReturn(Optional.empty());

            doNothing().when(reputationService).updateReputationForRenter(1L);

            when(reviewRenterRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, ReviewRenter.class);
                arg.setId(1L);
                arg.setRating(creationDto.getRating());
                arg.setComment(creationDto.getComment());
                arg.setCreatedAt(LocalDateTime.now());
                return arg;
            });

            var exception = assertThrows(NotFoundException.class, () -> reviewService.createRenterReview(creationDto));
            assertEquals("Transaktion mit Id 1 existiert nicht.", exception.getMessage());
        }
    }

    @Test
    public void getReviewCount_underCorrectCircumstances_returnCorrectValue() {
        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            when(reviewLocationRepository.countAllByReviewerEmail(any())).thenReturn(1L);
            when(reviewRenterRepository.countAllByReviewerEmail(any())).thenReturn(2L);
            when(transactionRepository.countAllCompletedByEmail(any())).thenReturn(5L);

            var result = reviewService.getReviewCount();

            assertAll(
                () -> assertEquals(5L, result.getCompletedTransactions()),
                () -> assertEquals(3L, result.getReviews())
            );
        }
    }
}
