package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.CoordinateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PlzDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLocation;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.mapper.LocationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LocationMapperTest {

    @Autowired
    private LocationMapper locationMapper;

    ReputationLocation reputationLocation = ReputationLocation.builder()
        .ratings(3)
        .karma(BigDecimal.TEN)
        .weightPositive(BigDecimal.ZERO)
        .weightNegative(BigDecimal.ZERO)
        .build();

    Location entity = Location.builder()
        .id(1L)
        .name("name")
        .description("description")
        .removed(false)
        .plz(Plz.builder().plz("2222").build())
        .state(AustriaState.NOE)
        .address("address")
        .sizeInM2(BigDecimal.ONE)
        .createdAt(LocalDateTime.now())
        .reputation(reputationLocation)
        .build();

    @Test
    public void entityToDto_given_whenLocationEntity_thenMappedDto() {
        var dto = locationMapper.entityToDto(entity);

        assertAll(
            () -> assertEquals(entity.getId(), dto.getId()),
            () -> assertEquals(entity.getName(), dto.getName()),
            () -> assertEquals(entity.getDescription(), dto.getDescription()),
            () -> assertEquals(entity.isRemoved(), dto.getIsRemoved()),
            () -> assertEquals(entity.getPlz().getPlz(), dto.getPlz().getPlz()),
            () -> assertEquals(entity.getState(), dto.getState()),
            () -> assertEquals(entity.getAddress(), dto.getAddress()),
            () -> assertEquals(entity.getSizeInM2(), dto.getSize()),
            () -> assertEquals(entity.getCreatedAt(), dto.getCreatedAt()),
            () -> assertEquals(entity.getReputation().getRatings(), dto.getReputation().getRatings()),
            () -> assertNull(dto.getCoord())
        );
    }

    @Test
    public void entityToDto_givenList_whenLocationEntity_thenMappedDto() {
        var dto = locationMapper.entityToDto(List.of(entity));

        assertAll(
            () -> assertEquals(entity.getId(), dto.get(0).getId()),
            () -> assertEquals(entity.getName(), dto.get(0).getName()),
            () -> assertEquals(entity.getDescription(), dto.get(0).getDescription()),
            () -> assertEquals(entity.isRemoved(), dto.get(0).getIsRemoved()),
            () -> assertEquals(entity.getPlz().getPlz(), dto.get(0).getPlz().getPlz()),
            () -> assertEquals(entity.getState(), dto.get(0).getState()),
            () -> assertEquals(entity.getAddress(), dto.get(0).getAddress()),
            () -> assertEquals(entity.getSizeInM2(), dto.get(0).getSize()),
            () -> assertEquals(entity.getCreatedAt(), dto.get(0).getCreatedAt()),
            () -> assertEquals(entity.getReputation().getRatings(), dto.get(0).getReputation().getRatings())
        );
    }

    @Test
    public void dtoToEntity_given_whenLocationCreationDto_thenMappedEntity() {
        var dto = LocationCreationDto.builder()
            .name("name")
            .description("description")
            .plz(PlzDto.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("address")
            .size(BigDecimal.TEN)
            .build();

        var entity = locationMapper.dtoToEntity(dto);

        assertAll(
            () -> assertEquals(dto.getName(), entity.getName()),
            () -> assertEquals(dto.getDescription(), entity.getDescription()),
            () -> assertEquals(dto.getPlz().getPlz(), entity.getPlz().getPlz()),
            () -> assertEquals(dto.getState(), entity.getState()),
            () -> assertEquals(dto.getAddress(), entity.getAddress()),
            () -> assertEquals(dto.getSize(), entity.getSizeInM2())
        );
    }

    @Test
    public void dtoToEntity_given_whenLocationDto_thenMappedEntity() {
        var dto = LocationDto.builder()
            .id(1L)
            .name("name")
            .description("description")
            .isRemoved(false)
            .plz(PlzDto.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("address")
            .size(BigDecimal.TEN)
            .createdAt(LocalDateTime.now())
            .build();

        var entity = locationMapper.dtoToEntity(dto);

        assertAll(
            () -> assertEquals(dto.getId(), entity.getId()),
            () -> assertEquals(dto.getName(), entity.getName()),
            () -> assertEquals(dto.getDescription(), entity.getDescription()),
            () -> assertEquals(dto.getIsRemoved(), entity.isRemoved()),
            () -> assertEquals(dto.getPlz().getPlz(), entity.getPlz().getPlz()),
            () -> assertEquals(dto.getState(), entity.getState()),
            () -> assertEquals(dto.getAddress(), entity.getAddress()),
            () -> assertEquals(dto.getSize(), entity.getSizeInM2()),
            () -> assertEquals(dto.getCreatedAt(), entity.getCreatedAt())
        );
    }

    @Test
    public void entityToDto_givenLocationEntity_whenCoordinatesNotNull_thenMappedDto() {
        var entity = Location.builder()
            .id(1L)
            .name("name")
            .description("description")
            .removed(false)
            .plz(Plz.builder().plz("2222").build())
            .state(AustriaState.NOE)
            .address("address")
            .sizeInM2(BigDecimal.ONE)
            .createdAt(LocalDateTime.now())
            .reputation(reputationLocation)
            .coordLat(BigDecimal.ONE)
            .coordLng(BigDecimal.ONE)
            .build();
        var dto = locationMapper.entityToDto(entity);

        assertAll(
            () -> assertEquals(entity.getId(), dto.getId()),
            () -> assertEquals(entity.getName(), dto.getName()),
            () -> assertEquals(entity.getDescription(), dto.getDescription()),
            () -> assertEquals(entity.isRemoved(), dto.getIsRemoved()),
            () -> assertEquals(entity.getPlz().getPlz(), dto.getPlz().getPlz()),
            () -> assertEquals(entity.getState(), dto.getState()),
            () -> assertEquals(entity.getAddress(), dto.getAddress()),
            () -> assertEquals(entity.getSizeInM2(), dto.getSize()),
            () -> assertEquals(entity.getCreatedAt(), dto.getCreatedAt()),
            () -> assertEquals(entity.getReputation().getRatings(), dto.getReputation().getRatings()),
            () -> assertEquals(entity.getCoordLat(), dto.getCoord().getLat()),
            () -> assertEquals(entity.getCoordLng(), dto.getCoord().getLng())
        );
    }

    @Test
    public void dtoToEntity_givenLocationDto_whenCoordinatesNotNull_thenMappeddEntity() {
        var dto = LocationDto.builder()
            .id(1L)
            .name("name")
            .description("description")
            .isRemoved(false)
            .plz(PlzDto.builder().plz("1234").build())
            .state(AustriaState.W)
            .address("address")
            .size(BigDecimal.TEN)
            .createdAt(LocalDateTime.now())
            .coord(new CoordinateDto(BigDecimal.ONE, BigDecimal.ONE))
            .build();

        var entity = locationMapper.dtoToEntity(dto);

        assertAll(
            () -> assertEquals(dto.getId(), entity.getId()),
            () -> assertEquals(dto.getName(), entity.getName()),
            () -> assertEquals(dto.getDescription(), entity.getDescription()),
            () -> assertEquals(dto.getIsRemoved(), entity.isRemoved()),
            () -> assertEquals(dto.getPlz().getPlz(), entity.getPlz().getPlz()),
            () -> assertEquals(dto.getState(), entity.getState()),
            () -> assertEquals(dto.getAddress(), entity.getAddress()),
            () -> assertEquals(dto.getSize(), entity.getSizeInM2()),
            () -> assertEquals(dto.getCreatedAt(), entity.getCreatedAt()),
            () -> assertEquals(dto.getCoord().getLat(), entity.getCoordLat()),
            () -> assertEquals(dto.getCoord().getLng(), entity.getCoordLng())
        );
    }

}
