package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Reputation;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLenderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ReputationLenderRepositoryTest {

    @Autowired
    private ReputationLenderRepository reputationLenderRepository;

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByLenderId_givenReputationData_whenLenderReputationExists_thenReturnLenderReputation() {
        var result = reputationLenderRepository.findByLenderId(-1L);
        assertTrue(result.isPresent());
        assertEquals(-1L, result.get().getLender().getId());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByLenderId_givenReputationData_whenLenderReputationDoesntExist_thenReturnNothing() {
        var result = reputationLenderRepository.findByLenderId(-999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findLastUpdatedBeforeDateStream_givenReputationData_whenReputationOlderThanTimeExists_thenReturnOnlyReputationOlderThanTime() {
        var cutoff = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        var result = reputationLenderRepository.findLastUpdatedBeforeDateStream(cutoff)
            .map(Reputation::getUpdatedAt);
        assertThat(result).allSatisfy(r -> assertThat(r).isBefore(cutoff));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findLastUpdatedBeforeDateStream_givenReputationData_whenNoReputationOlderThanTimeExists_thenDontReturnAnything() {
        var result = reputationLenderRepository.findLastUpdatedBeforeDateStream(LocalDateTime.now().minus(100, ChronoUnit.YEARS)).toList();
        assertEquals(0, result.size());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void summarizeAllByLenderNameContaining_givenReputationDataAndName_whenLenderContainsName_thenReturnReputationSummaryForLender() {
        var result = reputationLenderRepository.summarizeAllByLenderNameContaining("TEST_lender", PageRequest.of(0, 1)).toList();
        assertEquals(1, result.size());
        var reputation = reputationLenderRepository.findByLenderId(result.get(0).getSubjectId());
        assertTrue(reputation.isPresent());
        assertAll(
            () -> assertEquals(reputation.get().getRatings(), result.get(0).getRatings()),
            () -> assertEquals(reputation.get().getAverageRating(), result.get(0).getAverageRating()),
            () -> assertEquals(reputation.get().getKarma(), result.get(0).getKarma()),
            () -> assertTrue(reputation.get().getCreatedAt().equals(result.get(0).getLastChange())
                || reputation.get().getUpdatedAt().equals(result.get(0).getLastChange()))
        );
    }

}
