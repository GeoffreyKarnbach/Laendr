package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.LocationForLenderSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.LocationSortingCriterion;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LocationServiceSortingTest {

    @Autowired
    private LocationService locationService;

    @Test
    @Sql("/sql/reputation/sort_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void searchByName_givenData_whenSortingByReputation_thenResultIsOrderedByReputation() {
        var result = locationService.searchByName("", 0, 3, LocationSortingCriterion.RECOMMENDED_DESC).getResult();

        assertAll(
            () -> assertEquals(-1, result.get(0).getId()),
            () -> assertEquals(-2, result.get(1).getId()),
            () -> assertEquals(-3, result.get(2).getId())
        );
    }

    @Test
    @Sql("/sql/reputation/sort_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void searchByLender_givenData_whenSortingByReputation_thenResultIsOrderedByReputation() {
        var searchDto = LocationForLenderSearchDto.builder()
            .id(-1L)
            .includeRemovedLocations(false)
            .page(0)
            .pageSize(3)
            .sort(LocationSortingCriterion.RECOMMENDED_DESC)
            .build();
        var result = locationService.searchByLender(searchDto).getResult();

        assertAll(
            () -> assertEquals(-1, result.get(0).getId()),
            () -> assertEquals(-2, result.get(1).getId()),
            () -> assertEquals(-3, result.get(2).getId())
        );
    }

}
