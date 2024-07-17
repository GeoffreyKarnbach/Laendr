package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Reputation;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLocationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ReputationLocationRepositoryTest {

    @Autowired
    private ReputationLocationRepository reputationLocationRepository;

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByLocationId_givenReputationData_whenLocationReputationExists_thenReturnLocationReputation() {
        var result = reputationLocationRepository.findByLocationId(-1L);
        assertTrue(result.isPresent());
        assertEquals(-1L, result.get().getLocation().getId());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByLocationId_givenReputationData_whenLocationReputationDoesntExist_thenReturnNothing() {
        var result = reputationLocationRepository.findByLocationId(-999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findLastUpdatedBeforeDateStream_givenReputationData_whenReputationOlderThanTimeExists_thenReturnOnlyReputationOlderThanTime() {
        var cutoff = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        var result = reputationLocationRepository.findLastUpdatedBeforeDateStream(cutoff)
            .map(Reputation::getUpdatedAt);
        assertThat(result).allSatisfy(r -> assertThat(r).isBefore(cutoff));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findLastUpdatedBeforeDateStream_givenReputationData_whenNoReputationOlderThanTimeExists_thenDontReturnAnything() {
        var result = reputationLocationRepository.findLastUpdatedBeforeDateStream(LocalDateTime.now().minus(100, ChronoUnit.YEARS)).toList();
        assertEquals(0, result.size());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void summarizeAllByLocationNameContaining_givenReputationDataAndName_whenLocationContainsName_thenReturnReputationSummaryForLocation() {
        var result = reputationLocationRepository.summarizeAllByLocationNameContaining("TEST_location", PageRequest.of(0, 1)).toList();
        assertEquals(1, result.size());
        var reputation = reputationLocationRepository.findByLocationId(result.get(0).getSubjectId());
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
