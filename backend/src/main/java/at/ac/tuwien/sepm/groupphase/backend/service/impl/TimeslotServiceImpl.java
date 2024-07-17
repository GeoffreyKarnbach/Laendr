package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.TimeslotMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.TimeslotService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.TimeslotValidator;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
public class TimeslotServiceImpl implements TimeslotService {

    private final TimeslotMapper timeslotMapper;
    private final TimeslotRepository timeslotRepository;
    private final LocationRepository locationRepository;
    private final TimeslotValidator timeslotValidator;

    @Override
    public List<TimeslotDto> getTimeslotsForLocationStartingOnCertainDay(TimeslotSearchDto searchDto) {

        timeslotValidator.validateTimeslotSearchDto(searchDto);

        LocalDate searchDay = searchDto.getDay();
        LocalDateTime searchStartTimestamp = searchDay.atStartOfDay();

        if (searchDay.isEqual(LocalDate.now())) {
            searchStartTimestamp = LocalDateTime.now();
        }

        var user = UserUtil.getActiveUser();

        var timeslots = timeslotRepository.findTimeslotsForLocationAndDay(
            searchDto.getLocationId(),
            searchStartTimestamp,
            searchDay.atTime(23, 59, 59),
            user != null ? user.getEmail() : null
        );

        if (searchDto.isCallerIsLocationOwner()) {
            return timeslotMapper.viewToTimeslotLenderDto(timeslots);
        } else {
            return timeslotMapper.viewToTimeslotRenterDto(timeslots);
        }
    }

    @Override
    public void deleteTimeslot(Long timeslotId) {
        timeslotValidator.validateTimeslotForDelete(timeslotId);

        var timeslot = timeslotRepository.findById(timeslotId).get();
        timeslot.setDeleted(true);
        timeslotRepository.save(timeslot);
    }

    @Override
    public TimeslotDto updateTimeslot(TimeslotDto updatedTimeslot) {

        var timeslotOpt = timeslotRepository.findById(updatedTimeslot.getId());
        if (timeslotOpt.isEmpty()) {
            throw new NotFoundException("Zeitfenster mit Id %s existiert nicht".formatted(updatedTimeslot.getId()));
        }
        var currentTimeslot = timeslotOpt.get();

        timeslotValidator.validateTimeslotForUpdate(currentTimeslot, updatedTimeslot);

        setPriceHourly(updatedTimeslot);

        return timeslotMapper.entityToDto(
            timeslotRepository.save(timeslotMapper.updateDtoToEntity(currentTimeslot, updatedTimeslot))
        );
    }

    @Override
    public TimeslotDto createTimeslot(TimeslotDto timeslotDto) {
        timeslotValidator.validateTimeslotForCreate(timeslotDto);

        setPriceHourly(timeslotDto);

        var timeslotEntity = timeslotMapper.dtoToEntity(timeslotDto);

        // safe to get() location, because it is validated in timeslotValidator
        timeslotEntity.setOwningLocation(locationRepository.findById(timeslotDto.getLocationId()).get());

        return timeslotMapper.entityToDto(
            timeslotRepository.save(timeslotEntity)
        );
    }

    private static void setPriceHourly(TimeslotDto updatedTimeslot) {
        var timeDifferenceInMinutes = ChronoUnit.MINUTES.between(updatedTimeslot.getStart(), updatedTimeslot.getEnd());
        var timeDifferenceInHours = (double) timeDifferenceInMinutes / 60;

        var priceHourly = updatedTimeslot.getPrice().divide(BigDecimal.valueOf(timeDifferenceInHours), 2, HALF_UP);
        updatedTimeslot.setPriceHourly(priceHourly);
    }
}
