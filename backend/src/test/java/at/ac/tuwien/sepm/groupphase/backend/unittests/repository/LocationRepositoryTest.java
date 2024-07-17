package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDistanceDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class LocationRepositoryTest implements TestData {

    @Autowired
    private LocationRepository locationRepository;

    @Test
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByNameOrOwnerNameContaining_givenDataExists_whenSearchStringNotFound_thenEmptyPage() {
        var result = locationRepository.findAllByNameOrOwnerNameContaining("NOT_FOUND", PageRequest.of(0, 1));
        assertEquals(0, result.getNumberOfElements());
    }

    @Test
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByNameOrOwnerNameContaining_givenDataExists_whenSearchStringFoundInLocationName_thenResultPage() {
        var result = locationRepository.findAllByNameOrOwnerNameContaining("location", PageRequest.of(0, 1));
        assertEquals(1, result.getNumberOfElements());
    }

    @Test
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByNameOrOwnerNameContaining_givenDataExists_whenSearchStringFoundInLenderName_thenResultPage() {
        var result = locationRepository.findAllByNameOrOwnerNameContaining("lender", PageRequest.of(0, 1));
        assertEquals(1, result.getNumberOfElements());
    }

    @Test
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByOwnerId_givenDataExists_whenLenderIdValidAndRemovedLocationsExcluded_thenResultPage() {
        var result = locationRepository.findAllByOwnerId(1L, false, PageRequest.of(0, 5));

        assertAll(
            () -> assertEquals(1, result.getNumberOfElements()),
            () -> assertEquals(1, result.toList().get(0).getId()),
            () -> assertEquals(1, result.toList().get(0).getOwner().getId())
        );
    }

    @Test
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByOwnerId_givenDataExists_whenLenderIdValidAndRemovedLocationsIncluded_thenResultPage() {
        var result = locationRepository.findAllByOwnerId(1L, true, PageRequest.of(0, 1));

        assertEquals(1, result.getNumberOfElements());
    }

    @Test
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByOwnerId_givenDataExists_whenLenderDoesNotExistAndRemovedLocationsExcluded_thenResultPage() {
        var result = locationRepository.findAllByOwnerId(5L, false, PageRequest.of(0, 1));

        assertEquals(0, result.getNumberOfElements());
    }

    @Test
    @Sql("/sql/location/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByOwnerId_givenDataExists_whenLenderDoesNotExistAndRemovedLocationsIncluded_thenResultPage() {
        var result = locationRepository.findAllByOwnerId(5L, true, PageRequest.of(0, 1));

        assertEquals(0, result.getNumberOfElements());
    }

    private LocationFilterDto createLocationFilterDto_ForTest() {
        return new LocationFilterDto(null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenSearchStringNotFound_thenEmptyPage() {
        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setSearchString("NOT_FOUND");

        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 1));
        assertEquals(0, result.getNumberOfElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenSearchStringFoundInAllEntries_thenResultPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setSearchString("location");
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(3, result.getNumberOfElements());
        assertEquals(3, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenSearchStringFoundInLenderName_thenSingleResult() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setSearchString("owner");
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(1, result.getTotalElements());
    }


    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenPlzFoundInLocation_thenSingleResult() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setPlz(new PlzDto("3101", "Wien1", null));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenPlzNotFound_thenEmptyPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setPlz(new PlzDto("9999", "NOT_EXISTING", null));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenStateFoundInLocation_thenSingleResult() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setState(AustriaState.NOE);
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenStateNotFound_thenEmptyPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setState(AustriaState.BGLD);
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenAddressFoundInLocation_thenSingleResult() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setAddress("ess_1");
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenAddressNotFound_thenEmptyPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setAddress("NOT_FOUND");
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenPriceRangeMatchingWithTimeslot_thenSingleResult() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setPriceFrom(BigDecimal.valueOf(100));
        dto.setPriceTo(BigDecimal.valueOf(200));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenPriceRangeNotMatching_thenEmptyPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setPriceFrom(BigDecimal.valueOf(0));
        dto.setPriceTo(BigDecimal.valueOf(10));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenTimeRangeMatchingWithTimeslot_thenResultPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setTimeFrom(LocalDate.of(3000, 1, 1));
        dto.setTimeTo(LocalDate.of(3025, 1, 1));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(2, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenTimeRangeNotMatching_thenEmptyPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setTimeFrom(LocalDate.of(2990, 1, 1));
        dto.setTimeTo(LocalDate.of(3000, 1, 1));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenAllParametersAreMatching_thenSingleResult() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setSearchString("location");
        dto.setState(AustriaState.NOE);
        dto.setPlz(new PlzDto("3101", "Wien1", null));
        dto.setAddress("address_1");
        dto.setPriceFrom(BigDecimal.valueOf(100));
        dto.setPriceTo(BigDecimal.valueOf(200));
        dto.setTimeFrom(LocalDate.of(3000, 1, 1));
        dto.setTimeTo(LocalDate.of(3025, 1, 1));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(1, result.getNumberOfElements());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenSortingBySizeDesc_thenMultipleSortedResults() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "sizeInM2")));
        assertEquals(3, result.getNumberOfElements());
        assertEquals(3, result.getTotalElements());
        assertEquals(-3, result.getContent().get(0).getId());
        assertEquals(-2, result.getContent().get(1).getId());
        assertEquals(-1, result.getContent().get(2).getId());
    }

    @Test
    @Sql("/sql/location/filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParamsSortByPrice_givenDataExists_whenSortingByAvgPriceDesc_thenMultipleSortedResults() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        var result = locationRepository.findAllByFilterParamsSortByPrice(dto, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "price.avg")));
        assertEquals(3, result.getNumberOfElements());
        assertEquals(3, result.getTotalElements());
        assertEquals(-2, result.getContent().get(0).getId());
        assertEquals(-1, result.getContent().get(1).getId());
        assertEquals(-3, result.getContent().get(2).getId());
    }

    @Test
    @Sql("/sql/location/geo_filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenLocationsInDistance_thenResultPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setPosition(new LocationFilterDistanceDto(BigDecimal.valueOf(1000), new CoordinateDto(BigDecimal.valueOf(50), BigDecimal.valueOf(50))));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(2, result.getNumberOfElements());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @Sql("/sql/location/geo_filter_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findAllByFilterParams_givenDataExists_whenLocationsNotInDistance_thenEmptyPage() {

        LocationFilterDto dto = createLocationFilterDto_ForTest();
        dto.setPosition(new LocationFilterDistanceDto(BigDecimal.valueOf(1), new CoordinateDto(BigDecimal.valueOf(40), BigDecimal.valueOf(40))));
        var result = locationRepository.findAllByFilterParams(dto, PageRequest.of(0, 5));
        assertEquals(0, result.getNumberOfElements());
        assertEquals(0, result.getTotalElements());
    }
}
