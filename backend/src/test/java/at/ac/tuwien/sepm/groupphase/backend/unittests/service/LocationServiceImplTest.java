package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationForLenderSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.enums.LocationSortingCriterion;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LocationServiceImplTest implements TestData {

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private LenderRepository lenderRepository;

    @MockBean
    private ApplicationUserRepository applicationUserRepository;

    @MockBean
    private PlzRepository plzRepository;

    @Autowired
    private LocationService locationService;

    @Test
    void searchByName_givenData_whenFound_thenWrappedLocationDtos() {
        var repoResult = mock(Page.class);
        when(repoResult.getTotalElements()).thenReturn(1L);
        when(repoResult.getTotalPages()).thenReturn(1);
        when(repoResult.getNumberOfElements()).thenReturn(1);
        when(repoResult.stream()).thenReturn(Stream.of(new Location()));

        when(locationRepository.findAllByNameOrOwnerNameContaining(any(), any())).thenReturn(repoResult);

        var result = locationService.searchByName("TEST", 1, 1, LocationSortingCriterion.RECOMMENDED_DESC);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getResultCount());
        assertEquals(1, result.getResult().size());
    }

    @Test
    void searchByName_givenData_whenNotFound_thenEmptyWrapper() {
        var repoResult = mock(Page.class);
        when(repoResult.getTotalElements()).thenReturn(0L);
        when(repoResult.getTotalPages()).thenReturn(0);
        when(repoResult.getNumberOfElements()).thenReturn(0);
        when(repoResult.stream()).thenReturn(Stream.of());

        when(locationRepository.findAllByNameOrOwnerNameContaining(any(), any())).thenReturn(repoResult);

        var result = locationService.searchByName("TEST", 1, 1, LocationSortingCriterion.RECOMMENDED_DESC);

        assertEquals(0, result.getTotalResults());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getResultCount());
        assertEquals(0, result.getResult().size());
    }

    @Test
    void searchByFilter_givenData_whenFound_thenWrappedLocationDtos() {
        var repoResult = mock(Page.class);
        when(repoResult.getTotalElements()).thenReturn(1L);
        when(repoResult.getTotalPages()).thenReturn(1);
        when(repoResult.getNumberOfElements()).thenReturn(1);
        when(repoResult.stream()).thenReturn(Stream.of(new Location()));

        when(locationRepository.findAllByFilterParams(any(), any())).thenReturn(repoResult);

        LocationFilterDto dto = new LocationFilterDto();
        dto.setSearchString("TEST");
        var result = locationService.filter(dto, 1, 1, LocationSortingCriterion.RECOMMENDED_DESC);

        assertEquals(1, result.getTotalResults());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getResultCount());
        assertEquals(1, result.getResult().size());
    }

    @Test
    void searchByFilter_givenData_whenNotFound_thenEmptyWrapper() {
        var repoResult = mock(Page.class);
        when(repoResult.getTotalElements()).thenReturn(0L);
        when(repoResult.getTotalPages()).thenReturn(0);
        when(repoResult.getNumberOfElements()).thenReturn(0);
        when(repoResult.stream()).thenReturn(Stream.of());

        when(locationRepository.findAllByFilterParams(any(), any())).thenReturn(repoResult);

        LocationFilterDto dto = new LocationFilterDto();
        dto.setSearchString("TEST");
        var result = locationService.filter(dto, 1, 1, LocationSortingCriterion.RECOMMENDED_DESC);

        assertEquals(0, result.getTotalResults());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getResultCount());
        assertEquals(0, result.getResult().size());
    }

    @Test
    void filter_givenInvalidData_whenFiltering_thenConflictError() {

        LocationFilterDto dto = new LocationFilterDto();
        dto.setPriceFrom(BigDecimal.valueOf(100));
        dto.setPriceTo(BigDecimal.valueOf(50));
        dto.setTimeFrom(LocalDate.of(2023, 1, 1));
        dto.setTimeTo(LocalDate.of(2020, 1, 1));

        ConflictException thrown = assertThrows(ConflictException.class, () -> locationService.filter(dto, 0, 1, LocationSortingCriterion.RECOMMENDED_DESC));

        List<String> messages = new ArrayList<>();
        for (ValidationErrorDto error : thrown.getValidationErrorRestDto().getErrors()) {
            messages.add(error.getMessage());
        }
        assertAll(
            () -> assertEquals(thrown.getValidationErrorRestDto().getErrors().size(), 4),
            () -> assertTrue(messages.contains("Minimale Preis darf nicht größer als maximaler Preis sein.")),
            () -> assertTrue(messages.contains("Anfangszeit darf nicht Endzeit überschreiten.")),
            () -> assertTrue(messages.contains("'Zeitraum bis' kann nicht in der Vergangenheit liegen!")),
            () -> assertTrue(messages.contains("'Zeitraum von' kann nicht in der Vergangenheit liegen!"))
        );
    }

    @Test
    void searchByLender_givenData_whenFound_thenWrappedLocationDtos() {
        var repoResult = mock(Page.class);
        when(repoResult.getTotalElements()).thenReturn(1L);
        when(repoResult.getTotalPages()).thenReturn(1);
        when(repoResult.getNumberOfElements()).thenReturn(1);
        when(repoResult.stream()).thenReturn(Stream.of(new Location()));

        when(locationRepository.findAllByOwnerId(anyLong(), eq(false), any())).thenReturn(repoResult);
        when(lenderRepository.findById(any())).thenReturn(Optional.of(Lender.builder().id(1L).build()));

        var searchDto = LocationForLenderSearchDto.builder()
            .id(1L)
            .includeRemovedLocations(false)
            .page(1)
            .pageSize(1)
            .sort(LocationSortingCriterion.RECOMMENDED_DESC)
            .build();
        var result = locationService.searchByLender(searchDto);

        assertAll(
            () -> assertEquals(1, result.getTotalResults()),
            () -> assertEquals(1, result.getTotalPages()),
            () -> assertEquals(1, result.getResultCount()),
            () -> assertEquals(1, result.getResult().size())
        );
    }


    @Test
    void searchByLender_givenData_whenNot_found_thenEmptyWrapper() {
        var repoResult = mock(Page.class);
        when(repoResult.getTotalElements()).thenReturn(0L);
        when(repoResult.getTotalPages()).thenReturn(0);
        when(repoResult.getNumberOfElements()).thenReturn(0);
        when(repoResult.stream()).thenReturn(Stream.of());

        when(locationRepository.findAllByOwnerId(anyLong(), eq(false), any())).thenReturn(repoResult);
        when(lenderRepository.findById(any())).thenReturn(Optional.of(Lender.builder().id(1L).build()));

        var searchDto = LocationForLenderSearchDto.builder()
            .id(4L)
            .includeRemovedLocations(false)
            .page(1)
            .pageSize(1)
            .sort(LocationSortingCriterion.RECOMMENDED_DESC)
            .build();
        var result = locationService.searchByLender(searchDto);

        assertAll(
            () -> assertEquals(0, result.getTotalResults()),
            () -> assertEquals(0, result.getTotalPages()),
            () -> assertEquals(0, result.getResultCount()),
            () -> assertEquals(0, result.getResult().size())
        );
    }

    @Test
    void createLocation_givenData_whenCreated_thenLocationWithAllInformationPlusIdAndCreatedAt() {

        var location = LocationCreationDto.builder()
            .name("NAME")
            .description("DESCRIPTION")
            .plz(PlzDto.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("Teststrasse 1")
            .size(new BigDecimal(100))
            .build();

        var userEmail = "lender@email.com";

        var user = ApplicationUser.builder()
            .id(-1L)
            .email(userEmail)
            .password(passwordEncoder.encode("password"))
            .name("Lender")
            .build();

        when(applicationUserRepository.findApplicationUserByEmail(userEmail)).thenReturn(Optional.of(user));
        when(lenderRepository.findById(-1L)).thenReturn(Optional.of(Lender.builder().id(-1L).build()));
        when(plzRepository.existsById("1234")).thenReturn(true);
        when(plzRepository.findById("1234")).thenReturn(Optional.of(Plz.builder().plz("1234").build()));
        when(locationRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Location.class);
            arg.setId(1L);
            arg.setCreatedAt(LocalDateTime.now());
            return arg;
        });

        LocationDto result = locationService.createLocation(location, userEmail);

        assertEquals(location.getName(), result.getName());
        assertEquals(location.getDescription(), result.getDescription());
        assertEquals(location.getPlz().getPlz(), result.getPlz().getPlz());
        assertEquals(location.getState(), result.getState());
        assertEquals(location.getAddress(), result.getAddress());
        assertEquals(location.getSize(), result.getSize());
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createLocation_givenInvalidData_whenCreated_thenValidationError() {

        var location = LocationCreationDto.builder()
            .name("  ")
            .description("  ")
            .plz(PlzDto.builder().plz("AAA").build())
            .state(AustriaState.W)
            .address("")
            .size(new BigDecimal(1000000000))
            .build();

        var userEmail = "lender@email.com";

        var user = ApplicationUser.builder()
            .id(-1L)
            .email(userEmail)
            .password(passwordEncoder.encode("password"))
            .name("Lender")
            .build();

        when(applicationUserRepository.findApplicationUserByEmail(userEmail)).thenReturn(Optional.of(user));
        when(lenderRepository.findById(-1L)).thenReturn(Optional.of(Lender.builder().id(-1L).build()));
        when(locationRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Location.class);
            arg.setId(1L);
            arg.setCreatedAt(LocalDateTime.now());
            return arg;
        });

        ValidationException thrown = assertThrows(ValidationException.class, () -> locationService.createLocation(location, userEmail));

        List<String> messages = new ArrayList<>();
        for (ValidationErrorDto error : thrown.getValidationErrorRestDto().getErrors()) {
            messages.add(error.getMessage());
        }
        assertAll(
            () -> assertEquals(thrown.getValidationErrorRestDto().getErrors().size(), 4),
            () -> assertTrue(messages.contains("Name darf nicht leer sein.")),
            () -> assertTrue(messages.contains("PLZ muss existieren.")),
            () -> assertTrue(messages.contains("Addresse darf nicht leer sein.")),
            () -> assertTrue(messages.contains("Größe darf höchstens 100000 betragen."))
        );
    }

    @Test
    void getLocationById_givenIdAndUser_whenFound_thenLocationDtoIsOwnerTrue() {
        var userEmail = "lender@email.com";
        var location = Location.builder()
            .id(-1L)
            .name("NAME")
            .description("DESCRIPTION")
            .plz(Plz.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("Teststrasse 1")
            .sizeInM2(new BigDecimal(100))
            .owner(Lender.builder()
                .id(-1L)
                .email("lender_official@email.com")
                .phone("1234567890")
                .owner(ApplicationUser.builder()
                    .id(-1L)
                    .email(userEmail)
                    .build())
                .build())
            .build();

        when(locationRepository.findById(-1L)).thenReturn(Optional.of(location));
        when(applicationUserRepository.findApplicationUserByEmail(userEmail)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).email(userEmail).build()));
        when(lenderRepository.findById(-1L)).thenReturn(Optional.of(Lender.builder().id(-1L).build()));

        var result = locationService.getLocationById(-1L, userEmail);

        assertAll(
            () -> assertEquals(location.getId(), result.getId()),
            () -> assertEquals(location.getName(), result.getName()),
            () -> assertEquals(location.getDescription(), result.getDescription()),
            () -> assertEquals(location.getPlz().getPlz(), result.getPlz().getPlz()),
            () -> assertEquals(location.getState(), result.getState()),
            () -> assertEquals(location.getAddress(), result.getAddress()),
            () -> assertEquals(location.getSizeInM2(), result.getSize()),
            () -> assertTrue(result.isCallerIsOwner())
        );
    }

    @Test
    void getLocationById_givenWrongIdAndUser_whenNotFound_thenThrowNotFoundException() {
        var userEmail = "lender@email.com";
        var location = Location.builder()
            .id(-1L)
            .name("NAME")
            .description("DESCRIPTION")
            .plz(Plz.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("Teststrasse 1")
            .sizeInM2(new BigDecimal(100))
            .owner(Lender.builder().id(-1L).build())
            .build();

        when(locationRepository.findById(-1L)).thenReturn(Optional.of(location));
        when(applicationUserRepository.findApplicationUserByEmail(userEmail)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).email(userEmail).build()));
        when(lenderRepository.findById(-1L)).thenReturn(Optional.of(Lender.builder().id(-1L).build()));

        Throwable thrown = assertThrows(NotFoundException.class, () -> locationService.getLocationById(-2L, userEmail));
        assertEquals("Location with id -2 not found", thrown.getMessage());
    }

    @Test
    void updateLocation_givenNonExistentId_whenUpdated_thenThrowNotFoundException() {
        var locationId = -2L;
        var userEmail = "lender@email.com";

        var location = LocationDto.builder()
            .name("NAME")
            .description("DESCRIPTION")
            .plz(PlzDto.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("Teststrasse 1")
            .size(new BigDecimal(100))
            .build();

        when(locationRepository.findById(-1L)).thenReturn(Optional.empty());

        Throwable thrown = assertThrows(NotFoundException.class, () -> locationService.updateLocation(locationId, userEmail, location));
        assertEquals("Location with id " + locationId + " not found", thrown.getMessage());
    }

    @Test
    void updateLocation_givenCorrectData_whenUpdated_thenLocationDtoHasNewData() {
        var locationId = -1L;
        var userEmail = "lender@email.com";
        var currentLocation = Location.builder()
            .id(locationId)
            .name("NAME")
            .description("DESCRIPTION")
            .plz(Plz.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("Teststrasse 1")
            .sizeInM2(new BigDecimal(100))
            .owner(Lender.builder().id(-1L).build())
            .createdAt(LocalDateTime.of(2021, 1, 1, 1, 1, 1))
            .updatedAt(LocalDateTime.of(2021, 1, 1, 1, 1, 1))
            .build();

        var updatedLocation = LocationDto.builder()
            .name("NEW NAME")
            .description("NEW DESCRIPTION")
            .plz(PlzDto.builder().plz("4321").build())
            .state(AustriaState.NOE)
            .address("Teststrasse 2")
            .size(new BigDecimal(200))
            .build();

        when(locationRepository.findById(-1L)).thenReturn(Optional.of(currentLocation));
        when(applicationUserRepository.findApplicationUserByEmail(userEmail)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).email(userEmail).build()));
        when(plzRepository.existsById("4321")).thenReturn(true);
        when(plzRepository.getReferenceById("4321")).thenReturn(Plz.builder().plz("4321").build());
        when(locationRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Location.class);
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        var result = locationService.updateLocation(locationId, userEmail, updatedLocation);

        assertAll(
            () -> assertEquals(updatedLocation.getName(), result.getName()),
            () -> assertEquals(updatedLocation.getDescription(), result.getDescription()),
            () -> assertEquals(updatedLocation.getPlz(), result.getPlz()),
            () -> assertEquals(updatedLocation.getState(), result.getState()),
            () -> assertEquals(updatedLocation.getAddress(), result.getAddress()),
            () -> assertEquals(updatedLocation.getSize(), result.getSize()),
            () -> assertEquals(currentLocation.getCreatedAt(), result.getCreatedAt())
        );

    }

    @Test
    void removeLocation_givenCorrectData_whenRemoved_thenReturnedHasRemovedFlagSet() {
        var locationId = -1L;
        var userEmail = "lender@email.com";
        var location = Location.builder()
            .id(locationId)
            .name("NAME")
            .owner(Lender.builder().id(-1L).build())
            .timeslots(null)
            .createdAt(LocalDateTime.of(2021, 1, 1, 1, 1, 1))
            .updatedAt(LocalDateTime.of(2021, 1, 1, 1, 1, 1))
            .build();

        when(locationRepository.findById(-1L)).thenReturn(Optional.of(location));
        when(applicationUserRepository.findApplicationUserByEmail(userEmail)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).email(userEmail).build()));
        when(locationRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Location.class);
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        var result = locationService.removeLocation(locationId, userEmail);

        assertAll(
            () -> assertEquals(location.getId(), result.getId()),
            () -> assertTrue(location.isRemoved()),
            () -> assertEquals(location.getName(), result.getName()),
            () -> assertEquals(location.getCreatedAt(), result.getCreatedAt())
        );
    }

    @Test
    void removeLocation_byWrongUser_thenThrows() {
        var locationId = -1L;
        var userId = -2L;
        var userEmail = "renter@email.com";
        var location = Location.builder().owner(Lender.builder().id(-1L).build())
            .build();

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(applicationUserRepository.findApplicationUserByEmail(userEmail)).thenReturn(Optional.of(ApplicationUser.builder().id(userId).email(userEmail).build()));

        Throwable thrown = assertThrows(ValidationException.class, () -> locationService.removeLocation(locationId, userEmail));

        assertEquals(thrown.getMessage(), "Benutzer mit der id " + userId + " ist nicht der Besitzer der zu löschenden Location (id= " + locationId + ")");
    }




}
