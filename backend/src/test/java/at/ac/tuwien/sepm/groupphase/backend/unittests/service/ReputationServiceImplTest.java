package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewAverage;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewRenter;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ReputationServiceImplTest {

    @Autowired
    private ReputationService reputationService;

    @MockBean
    private ReviewRenterRepository reviewRenterRepository;

    @MockBean
    private ReviewLocationRepository reviewLocationRepository;

    @Autowired
    private LenderRepository lenderRepository;

    @Autowired
    private RenterRepository renterRepository;

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void reputationFor_givenReputationData_whenSubjectDoesntExist_thenThrowNotFound() {
        assertAll(
            () -> assertThrows(NotFoundException.class, () -> reputationService.getReputationForRenter(-123L)),
            () -> assertThrows(NotFoundException.class, () -> reputationService.getReputationForLocation(-123L)),
            () -> assertThrows(NotFoundException.class, () -> reputationService.getReputationForLender(-123L))
        );
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationFor_givenReputationData_whenSubjectDoesntExist_thenThrowNotFound() {
        assertAll(
            () -> assertThrows(NotFoundException.class, () -> reputationService.updateReputationForLender(-123L)),
            () -> assertThrows(NotFoundException.class, () -> reputationService.updateReputationForLocation(-123L)),
            () -> assertThrows(NotFoundException.class, () -> reputationService.updateReputationForRenter(-123L))
        );
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateTimeDecayBefore_givenOldReputation_whenUpdatingTimeDecay_thenReputationGetsMoreNeutral() {
        var oldKarma = reputationService.getReputationForLender(-3L).getKarma().doubleValue();
        reputationService.updateTimeDecayBefore(LocalDateTime.now());
        var newKarma = reputationService.getReputationForLender(-3L).getKarma().doubleValue();

        assertTrue(Math.abs(oldKarma - 0.5) > Math.abs(newKarma - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForRenter_givenSameRatingsCloseTogether_whenOneHasDifferentReviewers_thenItAffectsReputationMore() {
        when(reviewRenterRepository.findAllByRenterIdStream(-4L)).thenReturn(Stream.of(
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-4L))
                .reviewer(lenderRepository.findById(-1L).get()) // same reviewer
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build(),
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-4L))
                .reviewer(lenderRepository.findById(-1L).get()) // same reviewer
                .createdAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.findAllByRenterIdStream(-5L)).thenReturn(Stream.of(
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-5L))
                .reviewer(lenderRepository.findById(-1L).get()) // different reviewers
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build(),
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-5L))
                .reviewer(lenderRepository.findById(-6L).get()) // different reviewers
                .createdAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.calculateAverageForRenterId(-4L)).thenReturn(new ReviewAverageImpl());
        when(reviewRenterRepository.calculateAverageForRenterId(-5L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForRenter(-4L);
        reputationService.updateReputationForRenter(-5L);

        var karmaSameReviewer = reputationService.getReputationForRenter(-4L).getKarma().doubleValue();
        var karmaDifferentReviewer = reputationService.getReputationForRenter(-5L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaDifferentReviewer - 0.5) > Math.abs(karmaSameReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLocation_givenSameRatingsCloseTogether_whenOneHasDifferentReviewers_thenItAffectsReputationMore() {
        when(reviewLocationRepository.findAllByLocationIdStream(-1L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get()) // same reviewer
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build(),
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get()) // same reviewer
                .createdAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLocationIdStream(-2L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-4L).get()) // different reviewer
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build(),
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-5L).get()) // different reviewer
                .createdAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLocationId(-1L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLocationId(-2L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLocation(-1L);
        reputationService.updateReputationForLocation(-2L);

        var karmaSameReviewer = reputationService.getReputationForLocation(-1L).getKarma().doubleValue();
        var karmaDifferentReviewer = reputationService.getReputationForLocation(-2L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaDifferentReviewer - 0.5) > Math.abs(karmaSameReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLender_givenSameRatingsCloseTogether_whenOneHasDifferentReviewers_thenItAffectsReputationMore() {
        when(reviewLocationRepository.findAllByLenderIdStream(-1L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get()) // same reviewer
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build(),
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get()) // same reviewer
                .createdAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLenderIdStream(-6L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-4L).get()) // different reviewer
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build(),
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-5L).get()) // different reviewer
                .createdAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(11, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLenderId(-1L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLenderId(-6L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLender(-1L);
        reputationService.updateReputationForLender(-6L);

        var karmaSameReviewer = reputationService.getReputationForLender(-1L).getKarma().doubleValue();
        var karmaDifferentReviewer = reputationService.getReputationForLender(-6L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaDifferentReviewer - 0.5) > Math.abs(karmaSameReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForRenter_givenSameRatings_whenReviewerHasMoreKarma_thenItAffectsReputationMore() {
        when(reviewRenterRepository.findAllByRenterIdStream(-4L)).thenReturn(Stream.of(
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.2")) // less karma
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-4L))
                .reviewer(lenderRepository.findById(-1L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.findAllByRenterIdStream(-5L)).thenReturn(Stream.of(
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.8")) // more karma
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-5L))
                .reviewer(lenderRepository.findById(-1L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.calculateAverageForRenterId(-4L)).thenReturn(new ReviewAverageImpl());
        when(reviewRenterRepository.calculateAverageForRenterId(-5L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForRenter(-4L);
        reputationService.updateReputationForRenter(-5L);

        var karmaLowerReviewer = reputationService.getReputationForRenter(-4L).getKarma().doubleValue();
        var karmaHigherReviewer = reputationService.getReputationForRenter(-5L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaHigherReviewer - 0.5) > Math.abs(karmaLowerReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLocation_givenSameRatings_whenReviewerHasMoreKarma_thenItAffectsReputationMore() {
        when(reviewLocationRepository.findAllByLocationIdStream(-1L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.2")) // less karma
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLocationIdStream(-2L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.8")) // more karma
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLocationId(-1L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLocationId(-2L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLocation(-1L);
        reputationService.updateReputationForLocation(-2L);

        var karmaLowerReviewer = reputationService.getReputationForLocation(-1L).getKarma().doubleValue();
        var karmaHigherReviewer = reputationService.getReputationForLocation(-2L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaHigherReviewer - 0.5) > Math.abs(karmaLowerReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLender_givenSameRatings_whenReviewerHasMoreKarma_thenItAffectsReputationMore() {
        when(reviewLocationRepository.findAllByLenderIdStream(-1L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.2")) // less karma
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLenderIdStream(-6L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.8")) // more karma
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLenderId(-1L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLenderId(-6L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLender(-1L);
        reputationService.updateReputationForLender(-6L);

        var karmaLowerReviewer = reputationService.getReputationForLender(-1L).getKarma().doubleValue();
        var karmaHigherReviewer = reputationService.getReputationForLender(-6L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaHigherReviewer - 0.5) > Math.abs(karmaLowerReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForRenter_givenSameRatings_whenReviweeIsVeryYoung_thenReputationGainIsDiminished() {
        when(reviewRenterRepository.findAllByRenterIdStream(-4L)).thenReturn(Stream.of(// 1 year old
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-4L))
                .reviewer(lenderRepository.findById(-1L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.findAllByRenterIdStream(-7L)).thenReturn(Stream.of(// 1 hour old
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-4L))
                .reviewer(lenderRepository.findById(-1L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.calculateAverageForRenterId(-4L)).thenReturn(new ReviewAverageImpl());
        when(reviewRenterRepository.calculateAverageForRenterId(-7L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForRenter(-4L);
        reputationService.updateReputationForRenter(-7L);

        var karmaOldAccount = reputationService.getReputationForRenter(-4L).getKarma().doubleValue();
        var karmaRecentAccount = reputationService.getReputationForRenter(-7L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaOldAccount - 0.5) > Math.abs(karmaRecentAccount - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLocation_givenSameRatings_whenReviweeIsVeryYoung_thenReputationGainIsDiminished() {
        when(reviewLocationRepository.findAllByLocationIdStream(-2L)).thenReturn(Stream.of(// 3 years old
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLocationIdStream(-3L)).thenReturn(Stream.of(// 1 hour old
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-3L))
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLocationId(-2L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLocationId(-3L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLocation(-2L);
        reputationService.updateReputationForLocation(-3L);

        var karmaOldLocation = reputationService.getReputationForLocation(-2L).getKarma().doubleValue();
        var karmaRecentLocation = reputationService.getReputationForLocation(-3L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaOldLocation - 0.5) > Math.abs(karmaRecentLocation - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLender_givenSameRatings_whenReviweeIsVeryYoung_thenReputationGainIsDiminished() {
        when(reviewLocationRepository.findAllByLenderIdStream(-1L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L)) // 3 years old
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLenderIdStream(-8L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-4L)) // 3 hours old
                .reviewer(renterRepository.findById(-4L).get())
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLenderId(-1L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLenderId(-8L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLender(-1L);
        reputationService.updateReputationForLender(-8L);

        var karmaOldLender = reputationService.getReputationForLender(-1L).getKarma().doubleValue();
        var karmaRecentLender = reputationService.getReputationForLender(-8L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaOldLender - 0.5) > Math.abs(karmaRecentLender - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForRenter_givenSameRatings_whenReviewerIsVeryYoung_thenReputationGainIsDiminished() {
        when(reviewRenterRepository.findAllByRenterIdStream(-4L)).thenReturn(Stream.of(
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-4L))
                .reviewer(lenderRepository.findById(-1L).get()) // 3 years old
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.findAllByRenterIdStream(-5L)).thenReturn(Stream.of(
            ReviewRenter.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForRenter(-4L))
                .reviewer(lenderRepository.findById(-8L).get()) // 1 hour old
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewRenterRepository.calculateAverageForRenterId(-4L)).thenReturn(new ReviewAverageImpl());
        when(reviewRenterRepository.calculateAverageForRenterId(-5L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForRenter(-4L);
        reputationService.updateReputationForRenter(-5L);

        var karmaOldReviewer = reputationService.getReputationForRenter(-4L).getKarma().doubleValue();
        var karmaRecentReviewer = reputationService.getReputationForRenter(-7L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaOldReviewer - 0.5) > Math.abs(karmaRecentReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLocation_givenSameRatings_whenReviewerIsVeryYoung_thenReputationGainIsDiminished() {
        when(reviewLocationRepository.findAllByLocationIdStream(-1L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get()) // 1 year old
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLocationIdStream(-2L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-7L).get()) // 1 hour old
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLocationId(-1L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLocationId(-2L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLocation(-1L);
        reputationService.updateReputationForLocation(-2L);

        var karmaOldReviewer = reputationService.getReputationForLocation(-1L).getKarma().doubleValue();
        var karmaRecentReviewer = reputationService.getReputationForLocation(-2L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaOldReviewer - 0.5) > Math.abs(karmaRecentReviewer - 0.5));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateReputationForLender_givenSameRatings_whenReviewerIsVeryYoung_thenReputationGainIsDiminished() {
        when(reviewLocationRepository.findAllByLenderIdStream(-1L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-1L))
                .reviewer(renterRepository.findById(-4L).get()) // 1 year old
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.findAllByLenderIdStream(-6L)).thenReturn(Stream.of(
            ReviewLocation.builder()
                .rating(4)
                .reviewerKarmaAtReview(new BigDecimal("0.5"))
                .revieweeKarmaAtReview(new BigDecimal("0.5"))
                .reputation(reputationService.getReputationForLocation(-2L))
                .reviewer(renterRepository.findById(-7L).get()) // 1 hour old
                .createdAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .updatedAt(LocalDateTime.now().minus(10, ChronoUnit.DAYS))
                .build()
        ));
        when(reviewLocationRepository.calculateAverageForLenderId(-1L)).thenReturn(new ReviewAverageImpl());
        when(reviewLocationRepository.calculateAverageForLenderId(-6L)).thenReturn(new ReviewAverageImpl());
        reputationService.updateReputationForLender(-1L);
        reputationService.updateReputationForLender(-6L);

        var karmaOldReviewer = reputationService.getReputationForLender(-1L).getKarma().doubleValue();
        var karmaRecentReviewer = reputationService.getReputationForLender(-6L).getKarma().doubleValue();

        assertTrue(Math.abs(karmaOldReviewer - 0.5) > Math.abs(karmaRecentReviewer - 0.5));
    }

    private static class ReviewAverageImpl implements ReviewAverage {
        public BigDecimal getAverage() {
            return BigDecimal.ONE.add(BigDecimal.ONE);
        }

        public int getCount() {
            return 0;
        }
    }

}
