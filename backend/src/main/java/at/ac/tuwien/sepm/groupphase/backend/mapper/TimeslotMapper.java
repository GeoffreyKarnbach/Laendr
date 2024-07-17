package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.TimeslotView;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper
public interface TimeslotMapper {
    @Named("toRenterDto")
    @Mapping(target = "isUsed", source = "used")
    @Mapping(target = "isRequested", ignore = true)
    @Mapping(target = "isRequestedByCallingUser", source = "requestedByCallingUser")
    TimeslotDto viewToTimeslotRenterDto(TimeslotView timeslot);

    @IterableMapping(qualifiedByName = "toRenterDto")
    List<TimeslotDto> viewToTimeslotRenterDto(List<TimeslotView> timeslots);

    @Named("toLenderDto")
    @Mapping(target = "isUsed", source = "used")
    @Mapping(target = "isRequested", source = "requested")
    @Mapping(target = "isRequestedByCallingUser", source = "requestedByCallingUser")
    TimeslotDto viewToTimeslotLenderDto(TimeslotView timeslot);

    @IterableMapping(qualifiedByName = "toLenderDto")
    List<TimeslotDto> viewToTimeslotLenderDto(List<TimeslotView> timeslots);

    @Mapping(target = "isUsed", source = "used")
    TimeslotDto entityToDto(Timeslot timeslot);

    @Mapping(target = "used", source = "isUsed")
    Timeslot updateDtoToEntity(@MappingTarget Timeslot timeslot, TimeslotDto timeslotDto);

    @Mapping(target = "used", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Timeslot dtoToEntity(TimeslotDto timeslotDto);
}
