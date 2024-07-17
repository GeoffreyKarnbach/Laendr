package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationColumnDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SortDirectionDto;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationSummaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ReputationSummaryServiceImplTest {

    @Autowired
    private ReputationSummaryService reputationSummaryService;

    @Autowired
    private ReputationLenderRepository reputationLenderRepository;

    @Autowired
    private ReputationLocationRepository reputationLocationRepository;

    @Autowired
    private ReputationRenterRepository reputationRenterRepository;

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getLenderReputations_givenReputationData_whenResultFound_thenSummaryMatchesReputationData() {
        var summaries = reputationSummaryService.getLenderReputations("", 0, 1, ReputationColumnDto.SUBJECT_NAME, SortDirectionDto.ASCENDING);
        assertEquals(1, summaries.getResultCount());

        var summary = summaries.getResult().get(0);
        var reputationOptional = reputationLenderRepository.findByLenderId(summary.getSubjectId());
        assertTrue(reputationOptional.isPresent());
        var reputation = reputationOptional.get();

        assertAll(
            () -> assertEquals(reputation.getLender().getOwner().getId(), summary.getSubjectId()),
            () -> assertEquals(reputation.getLender().getOwner().getName(), summary.getSubject()),
            () -> assertEquals(reputation.getKarma(), summary.getKarma()),
            () -> assertEquals(reputation.getAverageRating(), summary.getAverageRating()),
            () -> assertEquals(reputation.getRatings(), summary.getRatings()),
            () -> assertTrue(reputation.getCreatedAt().equals(summary.getLastChange())
                || reputation.getUpdatedAt().equals(summary.getLastChange()))
        );
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getLenderReputations_givenReputationData_whenQueryMatchesNothing_thenReturnNothing() {
        var summaries = reputationSummaryService.getLenderReputations("abcdefghijklmnopqrstuvwxyz", 0, 1, ReputationColumnDto.SUBJECT_NAME, SortDirectionDto.ASCENDING);
        assertEquals(0, summaries.getResultCount());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getLocationReputations_givenReputationData_whenResultFound_thenSummaryMatchesReputationData() {
        var summaries = reputationSummaryService.getLocationReputations("", 0, 1, ReputationColumnDto.SUBJECT_NAME, SortDirectionDto.ASCENDING);
        assertEquals(1, summaries.getResultCount());

        var summary = summaries.getResult().get(0);
        var reputationOptional = reputationLocationRepository.findByLocationId(summary.getSubjectId());
        assertTrue(reputationOptional.isPresent());
        var reputation = reputationOptional.get();

        assertAll(
            () -> assertEquals(reputation.getLocation().getId(), summary.getSubjectId()),
            () -> assertEquals(reputation.getLocation().getName(), summary.getSubject()),
            () -> assertEquals(reputation.getKarma(), summary.getKarma()),
            () -> assertEquals(reputation.getAverageRating(), summary.getAverageRating()),
            () -> assertEquals(reputation.getRatings(), summary.getRatings()),
            () -> assertTrue(reputation.getCreatedAt().equals(summary.getLastChange())
                || reputation.getUpdatedAt().equals(summary.getLastChange()))
        );
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getLocationReputations_givenReputationData_whenQueryMatchesNothing_thenReturnNothing() {
        var summaries = reputationSummaryService.getLocationReputations("abcdefghijklmnopqrstuvwxyz", 0, 1, ReputationColumnDto.SUBJECT_NAME, SortDirectionDto.ASCENDING);
        assertEquals(0, summaries.getResultCount());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRenterReputations_givenReputationData_whenResultFound_thenSummaryMatchesReputationData() {
        var summaries = reputationSummaryService.getRenterReputations("", 0, 1, ReputationColumnDto.SUBJECT_NAME, SortDirectionDto.ASCENDING);
        assertEquals(1, summaries.getResultCount());

        var summary = summaries.getResult().get(0);
        var reputationOptional = reputationRenterRepository.findByRenterId(summary.getSubjectId());
        assertTrue(reputationOptional.isPresent());
        var reputation = reputationOptional.get();

        assertAll(
            () -> assertEquals(reputation.getRenter().getOwner().getId(), summary.getSubjectId()),
            () -> assertEquals(reputation.getRenter().getOwner().getName(), summary.getSubject()),
            () -> assertEquals(reputation.getKarma(), summary.getKarma()),
            () -> assertEquals(reputation.getAverageRating(), summary.getAverageRating()),
            () -> assertEquals(reputation.getRatings(), summary.getRatings()),
            () -> assertTrue(reputation.getCreatedAt().equals(summary.getLastChange())
                || reputation.getUpdatedAt().equals(summary.getLastChange()))
        );
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRenterReputations_givenReputationData_whenQueryMatchesNothing_thenReturnNothing() {
        var summaries = reputationSummaryService.getRenterReputations("abcdefghijklmnopqrstuvwxyz", 0, 1, ReputationColumnDto.SUBJECT_NAME, SortDirectionDto.ASCENDING);
        assertEquals(0, summaries.getResultCount());
    }

}
