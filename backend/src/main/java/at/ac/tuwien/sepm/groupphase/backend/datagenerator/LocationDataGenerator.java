package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.LocationTag;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class LocationDataGenerator {

    private static final int CURRENT_TIMESLOT_COUNT = 35;
    private static final int PAST_TIMESLOT_COUNT = 70;
    private static final String[] PLZ_LIST = {
        "1010", "1210", "1160", "2091", "2126",
        "2141", "2221", "2305", "2371", "2434",
        "2552", "2723", "3420", "3552", "3742",
        "4020", "4190", "4320", "4613", "5093",
        "5532", "5760", "6152", "6456", "6808",
        "7210", "7552", "8036", "8221", "8294",
        "8630", "8772", "8904", "8967", "9183",
        "9500", "9624", "9781", "9991"
    };

    private static Timeslot generateTimeslot(
        Random random,
        Location location,
        TimeslotRepository repository,
        LocalDateTime base,
        int offset
    ) {
        int hours = 2 + (offset % 3);
        int price = random.nextInt(1000) + 100;
        var timeslotEntity = repository.save(Timeslot.builder()
            .start(base.plusDays(offset % 5).plusHours(offset % 5))
            .end(base.plusDays(offset % 5).plusHours((offset % 5) + hours))
            .price(BigDecimal.valueOf(price))
            .priceHourly(BigDecimal.valueOf(price / hours))
            .used(false)
            .deleted(false)
            .owningLocation(location)
            .build());
        timeslotEntity.setCreatedAt(base.minusDays(10));
        repository.save(timeslotEntity);
        return timeslotEntity;
    }

    public static TestLocation generateLocation(
        Random random,
        List<UserDataGenerator.TestLender> lenders,
        List<LocationTag> tags,
        LocationRepository locationRepository,
        ReputationLocationRepository reputationRepository,
        ReputationService reputationService,
        PlzRepository plzRepository,
        TimeslotRepository timeslotRepository
    ) {

        HashSet<LocationTag> locationTags = new HashSet<>();
        for (int i = 0; i < (random.nextInt(7) + 1); i++) {
            LocationTag tag = DatagenUtil.randomElement(random, tags);
            while (locationTags.contains(tag)) {
                tag = DatagenUtil.randomElement(random, tags);
            }
            locationTags.add(tag);
        }

        var plz = DatagenUtil.randomElement(random, PLZ_LIST);
        var state = switch (plz.charAt(0)) {
            case '2', '3' -> AustriaState.NOE;
            case '4' -> AustriaState.OOE;
            case '5' -> AustriaState.SBG;
            case '6' -> AustriaState.T;
            case '7' -> AustriaState.BGLD;
            case '8' -> AustriaState.STMK;
            case '9' -> AustriaState.KTN;
            default -> AustriaState.W;
        };
        var locationContext = new HashMap<String, Object>();
        var locationNameBuilder = new StringBuilder();
        LocationTemplate.LOCATION_TITLE.generate(locationNameBuilder, random, locationContext);
        var locationDescriptionBuilder = new StringBuilder();
        LocationTemplate.LOCATION_DESCRIPTION.generate(locationDescriptionBuilder, random, locationContext);
        var locationType = (TestLocationType) locationContext.getOrDefault("loctype", TestLocationType.LOCATION_UNKNOWN);

        var owner = DatagenUtil.randomElement(random, lenders);
        var reputationEntity = reputationRepository.save(reputationService.newLocationReputationEntity());
        var plzEntity = plzRepository.findById(plz).orElseThrow();
        var locationEntity = locationRepository.save(Location.builder()
            .name(locationNameBuilder.toString())
            .description(locationDescriptionBuilder.toString())
            .removed(false)
            .plz(plzEntity)
            .coordLat(plzEntity.getCoordLat())
            .coordLng(plzEntity.getCoordLng())
            .state(state)
            .address(LocationTemplate.ADDRESS.generateString(random))
            .sizeInM2(BigDecimal.valueOf(random.nextInt(locationType.sizeUpperBound) + 1))
            .owner(owner.lenderEntity())
            .reputation(reputationEntity)
            .tags(locationTags)
            .build());
        locationEntity.setCreatedAt(LocalDateTime.now().minus(3, ChronoUnit.YEARS));
        locationRepository.save(locationEntity);


        var currentTimeslots = IntStream.range(0, CURRENT_TIMESLOT_COUNT)
            .mapToObj((i) -> generateTimeslot(random, locationEntity, timeslotRepository, LocalDateTime.now().minusDays(1).withSecond(0).withNano(0), i))
            .toList();
        var pastTimeslots = IntStream.range(0, PAST_TIMESLOT_COUNT)
            .mapToObj((i) -> generateTimeslot(random, locationEntity, timeslotRepository, LocalDateTime.now().minusYears(2).withSecond(0).withNano(0), i))
            .toList();

        return new TestLocation(locationEntity, owner, currentTimeslots, pastTimeslots, locationType);
    }

    @AllArgsConstructor
    @Getter
    public enum TestLocationType {
        LOCATION_UNKNOWN(
            "Diese Location",
            null,
            999
        ),
        LOCATION_CELLAR(
            "Dieser Keller",
            new String[] {"keller1.jpg", "keller2.jpg"},
            40
        ),
        LOCATION_MEADOW(
            "Diese Wiese",
            new String[] {"wiese1.jpg", "wiese2.jpg"},
            500
        ),
        LOCATION_ROOM(
            "Dieses Zimmer",
            new String[] {"saal1.jpg", "saal2.jpg"},
            40
        ),
        LOCATION_HALL(
            "Dieser Saal",
            new String[] {"saal1.jpg", "saal2.jpg"},
            100
        ),
        LOCATION_AREA(
            "Dieses Areal",
            new String[] {"wiese1.jpg", "wiese2.jpg"},
            700
        );

        private String demonstrative;
        private String[] imageSet;
        int sizeUpperBound;
    }

    public record TestLocation(
        Location locationEntity,
        UserDataGenerator.TestLender owner,
        List<Timeslot> currentTimeslots,
        List<Timeslot> pastTimeslots,
        TestLocationType type
    ) {
    }

}
