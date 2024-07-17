package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.entity.TimeslotView;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class TimeslotRepositoryTest {

    @Autowired
    private TimeslotRepository timeslotRepository;

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findTimeslotsForLocationAndDay_givenLocationIdAndDates_whenSearchingTimeslots_thenTimeslots(){
        List<TimeslotView> result = timeslotRepository.findTimeslotsForLocationAndDay(
            -2L,
            LocalDateTime.of(2090, 5, 13, 12, 0),
            LocalDateTime.of(2090, 5, 13, 23, 59),
            "test_lender@email.com"
        );
        assertEquals(3, result.size());
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void findTimeslotsForLocationAndDay_givenNonExistingLocation_whenSearchingTimeslots_thenEmptyList(){
        List<TimeslotView> result = timeslotRepository.findTimeslotsForLocationAndDay(
            -200L,
            LocalDateTime.of(2090, 5, 13, 12, 0),
            LocalDateTime.of(2090, 5, 13, 23, 59),
            "test_lender@email.com"
        );
        assertEquals(0, result.size());
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void isTimeslotRequested_givenTimeslotId_whenTimeslotIsAssociatedWithTransaction_thenTrue(){
        assertTrue(timeslotRepository.isTimeslotRequested(-1L));
    }

    @Test
    @Sql("/sql/timeslot/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void isTimeslotRequested_givenTimeslotId_whenTimeslotIsNotAssociatedWithTransaction_thenFalse(){
        assertFalse(timeslotRepository.isTimeslotRequested(-2L));
    }
}
