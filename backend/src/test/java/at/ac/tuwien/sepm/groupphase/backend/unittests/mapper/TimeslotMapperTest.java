package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TimeslotTestData;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.TimeslotView;
import at.ac.tuwien.sepm.groupphase.backend.mapper.TimeslotMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TimeslotMapperTest implements TimeslotTestData {

    @Autowired
    private TimeslotMapper timeslotMapper;

    private final TimeslotView timeslot = testData();

    @Test
    public void entityToDto_givenTimeslotEntity_whenMapTimeslotEntityToDto_thenEntityHasAllProperties() {

        TimeslotDto timeslotDto = timeslotMapper.viewToTimeslotLenderDto(timeslot);
        assertAll(
            () -> assertEquals(timeslotDto.getId(), timeslot.getId()),
            () -> assertEquals(timeslotDto.getStart(), timeslot.getStart()),
            () -> assertEquals(timeslotDto.getEnd(), timeslot.getEnd()),
            () -> assertEquals(timeslotDto.getPrice(), timeslot.getPrice()),
            () -> assertEquals(timeslotDto.getPriceHourly(), timeslot.getPriceHourly()),
            () -> assertEquals(timeslotDto.getIsUsed(), timeslot.isUsed()),
            () -> assertEquals(timeslotDto.getIsRequested(), timeslot.isRequested()),
            () -> assertEquals(timeslotDto.getIsRequestedByCallingUser(), timeslot.isRequestedByCallingUser())
        );
    }

    @Test
    public void entityToDto_givenListWithTwoTimeslotEntities_whenMapListWithTwoTimeslotEntitiesToDto_thenGetListWithSizeTwoAndAllProperties() {
        List<TimeslotView> timeslots = new ArrayList<>();
        timeslots.add(timeslot);
        timeslots.add(timeslot);

        List<TimeslotDto> timeslotDtos = timeslotMapper.viewToTimeslotLenderDto(timeslots);
        assertEquals(2, timeslotDtos.size());
        TimeslotDto timeslotDto = timeslotDtos.get(0);
        assertAll(
            () -> assertEquals(timeslotDto.getId(), timeslot.getId()),
            () -> assertEquals(timeslotDto.getStart(), timeslot.getStart()),
            () -> assertEquals(timeslotDto.getEnd(), timeslot.getEnd()),
            () -> assertEquals(timeslotDto.getPrice(), timeslot.getPrice()),
            () -> assertEquals(timeslotDto.getPriceHourly(), timeslot.getPriceHourly()),
            () -> assertEquals(timeslotDto.getIsUsed(), timeslot.isUsed()),
            () -> assertEquals(timeslotDto.getIsRequested(), timeslot.isRequested()),
            () -> assertEquals(timeslotDto.getIsRequestedByCallingUser(), timeslot.isRequestedByCallingUser())
        );
    }
}
