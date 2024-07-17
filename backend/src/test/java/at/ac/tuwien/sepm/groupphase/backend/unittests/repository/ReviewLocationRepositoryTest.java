package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewLocationRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ReviewLocationRepositoryTest {

    @Autowired
    private ReviewLocationRepository reviewLocationRepository;

    @Test
    @Sql("/sql/review/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllByLocationIdPage_givenCorrectRequest_returnsPageWithReview(){
        long locationId = -1;
        int page = 0;
        int pageSize = 5;

        var result = reviewLocationRepository.findAllByLocationIdPage(locationId, PageRequest.of(page, pageSize));

        assertAll(
            () -> assertEquals(result.getTotalElements(), 1),
            () -> assertEquals(result.getTotalPages(), 1),
            () -> assertEquals(result.stream().toList().get(0).getId(), -1L)
        );
    }

    @Test
    @Sql("/sql/review/test_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllByLocationIdPage_givenIncorrectRequest_returnsEmptyPage(){
        long locationId = -555;
        int page = 0;
        int pageSize = 5;

        var result = reviewLocationRepository.findAllByLocationIdPage(locationId, PageRequest.of(page, pageSize));

        assertAll(
            () -> assertEquals(result.getTotalElements(), 0),
            () -> assertEquals(result.getTotalPages(), 0)
        );
    }

    @Test
    @Sql("/sql/review/extended_review_data.sql")
    @Sql(value = "/sql/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Transactional
    public void countAllByReviewerEmail() {
        var result = reviewLocationRepository.countAllByReviewerEmail("test3@mail.com");

        assertEquals(1, result);
    }

}
