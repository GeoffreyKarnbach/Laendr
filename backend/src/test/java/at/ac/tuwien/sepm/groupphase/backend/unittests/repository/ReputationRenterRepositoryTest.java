package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.Reputation;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRenterRepository;
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
public class ReputationRenterRepositoryTest {

    @Autowired
    private ReputationRenterRepository reputationRenterRepository;

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByRenterId_givenReputationData_whenRenterReputationExists_thenReturnRenterReputation() {
        var result = reputationRenterRepository.findByRenterId(-2L);
        assertTrue(result.isPresent());
        assertEquals(-2L, result.get().getRenter().getId());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findByRenterId_givenReputationData_whenRenterReputationDoesntExist_thenReturnNothing() {
        var result = reputationRenterRepository.findByRenterId(-999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findLastUpdatedBeforeDateStream_givenReputationData_whenReputationOlderThanTimeExists_thenReturnOnlyReputationOlderThanTime() {
        var cutoff = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        var result = reputationRenterRepository.findLastUpdatedBeforeDateStream(cutoff)
            .map(Reputation::getUpdatedAt);
        assertThat(result).allSatisfy(r -> assertThat(r).isBefore(cutoff));
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findLastUpdatedBeforeDateStream_givenReputationData_whenNoReputationOlderThanTimeExists_thenDontReturnAnything() {
        var result = reputationRenterRepository.findLastUpdatedBeforeDateStream(LocalDateTime.now().minus(100, ChronoUnit.YEARS)).toList();
        assertEquals(0, result.size());
    }

    @Test
    @Sql("/sql/reputation/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void summarizeAllByLenderNameContaining_givenReputationDataAndName_whenLenderContainsName_thenReturnReputationSummaryForLender() {
        var result = reputationRenterRepository.summarizeAllByRenterNameContaining("TEST_renter", PageRequest.of(0, 1)).toList();
        assertEquals(1, result.size());
        var reputation = reputationRenterRepository.findByRenterId(result.get(0).getSubjectId());
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
