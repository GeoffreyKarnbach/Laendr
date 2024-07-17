package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = ReputationMapper.class)
public interface LocationMapper {

    @Named("toDto")
    @Mapping(target = "isRemoved", source = "removed")
    @Mapping(target = "size", source = "sizeInM2")
    @Mapping(target = "callerIsOwner", ignore = true)
    @Mapping(target = "primaryImageUrl", source = "primaryImage.url")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "coord", source = "location", qualifiedByName = "createCoordinateDto")
    LocationDto entityToDto(Location location);

    @IterableMapping(qualifiedByName = "toDto")
    @Mapping(target = "owner", ignore = true)
    List<LocationDto> entityToDto(List<Location> locations);

    @Named("toEntity")
    @Mapping(target = "removed", source = "isRemoved")
    @Mapping(target = "sizeInM2", source = "size")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "coordLat", source = "coord.lat")
    @Mapping(target = "coordLng", source = "coord.lng")
    Location dtoToEntity(LocationCreationDto locationDto);

    @Named("toEntity")
    @Mapping(target = "removed", source = "isRemoved")
    @Mapping(target = "sizeInM2", source = "size")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "coordLat", source = "coord.lat")
    @Mapping(target = "coordLng", source = "coord.lng")
    Location dtoToEntity(LocationDto locationDto);

    @Named("toEntity")
    @Mapping(target = "sizeInM2", source = "size")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "coordLat", source = "coord.lat")
    @Mapping(target = "coordLng", source = "coord.lng")
    Location mapUpdateToLocation(@MappingTarget Location location, LocationDto update);

    @Named("createCoordinateDto")
    default CoordinateDto coordsToCoordinateDto(Location location) {
        if (location.getCoordLat() != null && location.getCoordLng() != null) {
            return new CoordinateDto(location.getCoordLat(), location.getCoordLng());
        }
        return null;
    }

}
