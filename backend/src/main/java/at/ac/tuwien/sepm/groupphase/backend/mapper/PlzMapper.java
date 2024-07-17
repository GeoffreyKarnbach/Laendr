package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface PlzMapper {

    @Named("toDto")
    @Mapping(target = "coord", source = "plzEntity", qualifiedByName = "createCoordinateDto")
    PlzDto entityToDto(Plz plzEntity);

    @Named("createCoordinateDto")
    default CoordinateDto coordsToCoordinateDto(Plz plz) {
        if (plz.getCoordLat() != null && plz.getCoordLng() != null) {
            return new CoordinateDto(plz.getCoordLat(), plz.getCoordLng());
        }
        return null;
    }

}
