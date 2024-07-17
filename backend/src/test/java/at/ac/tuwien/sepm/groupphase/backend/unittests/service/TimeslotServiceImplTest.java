package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TimeslotTestData;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.TimeslotService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.TimeslotValidator;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TimeslotServiceImplTest implements TimeslotTestData {

    @MockBean
    private TimeslotRepository timeslotRepository;

    @MockBean
    private LocationRepository locationRepository;

    @Autowired
    private TimeslotService timeslotService;

    @MockBean
    private TimeslotValidator timeslotValidator;

    @BeforeEach
    public void before() {
        ReflectionTestUtils.setField(timeslotValidator, "locationRepository", locationRepository);
    }

    @Test
    public void findForLocationAndDay_givenInvalidLocationId_whenValidationFails_thenConflictException() {
        doThrow(ConflictException.class).when(timeslotValidator).validateTimeslotSearchDto(any());

        assertThrows(ConflictException.class,
            () -> timeslotService.getTimeslotsForLocationStartingOnCertainDay(
                TimeslotSearchDto.builder()
                    .locationId(3L)
                    .day(LocalDate.now()).
                    build()
            )
        );
    }

    @Test
    public void findForLocationAndDay_givenSearchDayInThePast_whenValidationFails_thenValidationException() {
        doThrow(ValidationException.class).when(timeslotValidator).validateTimeslotSearchDto(any());

        assertThrows(ValidationException.class,
            () -> timeslotService.getTimeslotsForLocationStartingOnCertainDay(
                TimeslotSearchDto.builder()
                    .locationId(3L)
                    .day(LocalDate.now()).
                    build()
            )
        );
    }

    @Test
    public void findForLocationAndDay_givenSearchDayInTheFuture_whenSearchingTimeslotsForDay_thenFindAllTimeslotsForGivenDay () {

        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            var timeslot = testData();

            when(timeslotRepository.findTimeslotsForLocationAndDay(any(),any(),any(), any()))
                .thenReturn(List.of(timeslot));
            when(locationRepository.findOwnerEmailById(any())).thenReturn(Optional.of("lender@email.com"));

            var result = timeslotService.getTimeslotsForLocationStartingOnCertainDay(
                TimeslotSearchDto.builder()
                    .locationId(3L)
                    .day(LocalDate.now()).
                    build()
            );

            var timeslotResult = result.get(0);

            assertAll(
                () -> assertEquals(timeslotResult.getId(), timeslot.getId()),
                () -> assertEquals(timeslotResult.getStart(), timeslot.getStart()),
                () -> assertEquals(timeslotResult.getEnd(), timeslot.getEnd()),
                () -> assertEquals(timeslotResult.getPrice(), timeslot.getPrice()),
                () -> assertEquals(timeslotResult.getPriceHourly(), timeslot.getPriceHourly()),
                () -> assertEquals(timeslotResult.getIsUsed(), timeslot.isUsed()),
                () -> assertEquals(timeslotResult.getIsRequestedByCallingUser(), timeslot.isRequestedByCallingUser())
            );
        }
    }

    @Test
    public void updateTimeslot_givenValidTimeslot_whenUpdating_thenUpdatedTimeslotDto() {
        var timeslotDto = TimeslotDto.builder()
            .id(-1L)
            .start(LocalDateTime.of(2090,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .build();

        var timeslot = Timeslot.builder()
            .id(-1L)
            .start(LocalDateTime.of(2012,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .priceHourly(BigDecimal.valueOf(5))
            .used(false)
            .owningLocation(
                Location.builder()
                    .id(-1L)
                    .owner(
                        Lender.builder()
                            .email("lender@email.com")
                            .build()
                    )
                    .build()
            )
            .build();

        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            when(timeslotRepository.findById(-1L)).thenReturn(Optional.of(timeslot));
            when(timeslotRepository.isTimeslotRequested(-1L)).thenReturn(false);

            when(timeslotRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, Timeslot.class);
                arg.setUpdatedAt(LocalDateTime.now());
                return arg;
            });

            var updatedDto = timeslotService.updateTimeslot(timeslotDto);

            assertAll(
                () -> assertEquals(timeslot.getId(), updatedDto.getId()),
                () -> assertEquals(timeslot.getStart(), updatedDto.getStart()),
                () -> assertEquals(timeslot.getEnd(), updatedDto.getEnd()),
                () -> assertEquals(0, timeslot.getPrice().compareTo(updatedDto.getPrice())),
                () -> assertEquals(0, timeslot.getPriceHourly().compareTo(updatedDto.getPriceHourly())),
                () -> assertEquals(timeslot.isUsed(), updatedDto.getIsUsed()),
                () -> assertNull(updatedDto.getIsRequested())
            );
        }
    }

    @Test
    public void updateTimeslot_givenInvalidTimeslotId_whenUpdating_thenNotFoundException() {
        var timeslotDto = TimeslotDto.builder()
            .id(-1L)
            .start(LocalDateTime.of(2012,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .build();

        when(timeslotRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> timeslotService.updateTimeslot(timeslotDto));
    }

    @Test
    public void createTimeslot_givenValidTimeslotDto_whenCreating_thenPersistedTimeslotDto() {
        var timeslotDto = TimeslotDto.builder()
            .start(LocalDateTime.of(2090,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .locationId(5L)
            .build();
        var timeDifferenceInHours = 2;

        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            when(locationRepository.findOwnerEmailById(any())).thenReturn(Optional.of("lender@email.com"));
            when(locationRepository.findById(any())).thenReturn(Optional.of(Location.builder().id(1L).build()));

            when(timeslotRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, Timeslot.class);
                arg.setUpdatedAt(LocalDateTime.now());
                return arg;
            });

            var createdDto = timeslotService.createTimeslot(timeslotDto);

            assertAll(
                () -> assertEquals(timeslotDto.getStart(), createdDto.getStart()),
                () -> assertEquals(timeslotDto.getEnd(), createdDto.getEnd()),
                () -> assertEquals(0, timeslotDto.getPrice().compareTo(createdDto.getPrice())),
                () -> assertEquals(0, createdDto.getPriceHourly().compareTo(
                    timeslotDto.getPrice().divide(BigDecimal.valueOf(timeDifferenceInHours),2, RoundingMode.HALF_UP))
                ),
                () -> assertEquals(false, createdDto.getIsUsed()),
                () -> assertNull(createdDto.getIsRequested())
            );
        }
    }

    @Test
    public void createTimeslot_givenInvalidUser_whenCreating_thenConflictException() {
        var timeslotDto = TimeslotDto.builder()
            .start(LocalDateTime.of(2090,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .locationId(5L)
            .build();

        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            doCallRealMethod().when(timeslotValidator).validateTimeslotForCreate(any());
            when(locationRepository.findOwnerEmailById(any())).thenReturn(Optional.of("lender_wrong@email.com"));
            when(locationRepository.findById(any())).thenReturn(Optional.of(Location.builder().id(1L).build()));

            when(timeslotRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, Timeslot.class);
                arg.setUpdatedAt(LocalDateTime.now());
                return arg;
            });

            assertThrows(ConflictException.class, () -> timeslotService.createTimeslot(timeslotDto));
        }
    }

    @Test
    public void createTimeslot_givenStartDateInThePast_whenCreating_thenValidationException() {
        var timeslotDto = TimeslotDto.builder()
            .start(LocalDateTime.of(2012,5,10,14,0))
            .end(LocalDateTime.of(2090,5,10,16,0))
            .price(BigDecimal.TEN)
            .locationId(5L)
            .build();

        try (var userUtil = Mockito.mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getActiveUser).thenReturn(new UserUtil.UserInfo("lender@email.com", null));

            doCallRealMethod().when(timeslotValidator).validateTimeslotForCreate(any());
            when(locationRepository.findOwnerEmailById(any())).thenReturn(Optional.of("lender@email.com"));
            when(locationRepository.findById(any())).thenReturn(Optional.of(Location.builder().id(1L).build()));

            when(timeslotRepository.save(any())).thenAnswer(invocation -> {
                var arg = invocation.getArgument(0, Timeslot.class);
                arg.setUpdatedAt(LocalDateTime.now());
                return arg;
            });

            assertThrows(ValidationException.class, () -> timeslotService.createTimeslot(timeslotDto));
        }
    }
}
