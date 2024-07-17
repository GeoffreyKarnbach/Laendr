package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper
public interface UserMapper {

    @Named("toEntity")
    @Mapping(target = "id", ignore = true) // Ignore mapping for the id field
    @Mapping(target = "name", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "originalPassword")
    @Mapping(target = "isDeleted", constant = "false")
    ApplicationUser dtoToEntity(SignUpDto signUpDto);

    @Named("toDto")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "isLocked", source = "locked")
    @Mapping(target = "isLender", expression = "java(user.getLender() != null)")
    @Mapping(target = "coordinates", source = "user", qualifiedByName = "createCoordinateDto")
    UserDto entityToDto(ApplicationUser user);

    @Named("toEntity")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "lender", ignore = true)
    @Mapping(target = "coordLat", source = "coordinates.lat")
    @Mapping(target = "coordLng", source = "coordinates.lng")
    @Mapping(target = "id", ignore = true)
    ApplicationUser mapUpdateToAppUser(@MappingTarget ApplicationUser user, UserDto update);

    @Mapping(target = "email", source = "userDto.email")
    @Mapping(target = "isLender", constant = "true")
    @Mapping(target = "lenderDescription", source = "lender.description")
    @Mapping(target = "lenderPhone", source = "lender.phone")
    @Mapping(target = "lenderEmail", source = "lender.email")
    @Mapping(target = "id", ignore = true)
    UserDto mergeLenderInfoIntoDto(UserDto userDto, Lender lender);

    @Named("toEntity")
    @Mapping(target = "email", source = "lenderEmail")
    @Mapping(target = "phone", source = "lenderPhone")
    @Mapping(target = "description", source = "lenderDescription")
    @Mapping(target = "id", ignore = true)
    Lender mapUpdateToLender(@MappingTarget Lender lender, UserDto update);

    @Mapping(target = "email", source = "userDto.email")
    @Mapping(target = "renterPhone", source = "renter.phone")
    @Mapping(target = "renterEmail", source = "renter.email")
    @Mapping(target = "id", ignore = true)
    UserDto mergeRenterInfoIntoDto(UserDto userDto, Renter renter);

    @Named("toEntity")
    @Mapping(target = "email", source = "renterEmail")
    @Mapping(target = "phone", source = "renterPhone")
    @Mapping(target = "id", ignore = true)
    Renter mapUpdateToRenter(@MappingTarget Renter renter, UserDto update);

    @Named("createCoordinateDto")
    default CoordinateDto coordsToCoordinateDto(ApplicationUser user) {
        if (user.getCoordLat() != null && user.getCoordLng() != null) {
            return new CoordinateDto(user.getCoordLat(), user.getCoordLng());
        }
        return null;
    }
}
