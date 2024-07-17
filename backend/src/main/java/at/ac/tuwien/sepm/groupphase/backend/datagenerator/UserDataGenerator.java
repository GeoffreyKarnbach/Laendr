package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;

import java.time.LocalDateTime;
import java.util.Random;

public class UserDataGenerator {

    // https://www.statistik.at/statistiken/bevoelkerung-und-soziales/bevoelkerung/geburten/vornamen-der-geborenen
    private static final String[] FIRST_NAME_LIST = {
        "Paul", "Marie", "Jakob", "Emilia", "Maximilian", "Anna", "Elias", "Emma", "David", "Lena", "Felix", "Mia",
        "Leon", "Laura", "Tobias", "Valentina", "Jonas", "Hannah", "Noah", "Lea", "Lukas", "Sophia", "Alexander", "Sophie",
        "Moritz", "Johanna", "Leo", "Leonie", "Julian", "Lina", "Simon", "Nora", "Matteo", "Ella", "Fabian", "Lara",
        "Valentin", "Luisa", "Raphael", "Elena", "Emil", "Magdalena", "Luca", "Hanna", "Samuel", "Olivia", "Anton", "Amelie",
        "Florian", "Helena", "Theo", "Theresa", "Luis", "Valerie", "Matthias", "Katharina", "Johannes", "Julia", "Benjamin", "Mila",
        "Lorenz", "Sarah", "Theodor", "Miriam", "Liam", "Elisa", "Niklas", "Emily", "Michael", "Antonia", "Gabriel", "Klara",
        "Sebastian", "Sara", "Nico", "Alina", "Ben", "Franziska", "Daniel", "Marlene", "Philipp", "Lisa", "Finn", "Rosa",
        "Vincent", "Sofia", "Jonathan", "Clara", "Oskar", "Ida", "Max", "Elina", "Oliver", "Paula", "Mateo", "Leni",
        "Konstantin", "Rosalie", "Adam", "Jana", "Luka", "Isabella", "Leopold", "Nina", "Adrian", "Flora", "Fabio", "Paulina",
        "Levi", "Annika", "Matheo", "Maria", "Dominik", "Melina", "Josef", "Livia", "Ferdinand", "Charlotte", "Kilian", "Eva"
    };

    public static final String[] NAME_PREFIX_LIST = {
        "Real", "Vermieter", "0"
    };

    private static final String[] NAME_SUFFIX_LIST = {
        "Official", "Offiziell", "123", "Xx",
        "00", "99", "98", "95", "90", "80", "75",
        "Meier", "Berger", "Fischer", "Müller", "Schneider"
    };

    private static final String[] EMAIL_NAME_SUFFIX_LIST = {
        "Post", "Official", "Offiziell", "Ländr", "Laendr", "Landr"
    };
    private static final String[] EMAIL_DOMAIN_NAME_LIST = {
        "@email", "@mail", "@post", "@supermail", "@e-post",
        "@internet", "@cool", "@cloud", "@postfach"
    };
    private static final String[] EMAIL_DOMAIN_LIST = {
        ".com", ".org", ".local", ".at", ".de", ".eu", ".arpa",
        ".io", ".to", ".post", ".net"
    };
    private static final String[] PHONE_AREA_CODES = {
        "+43 ", "06"
    };

    private static String generateEmail(Random random, String firstName, ApplicationUserRepository userRepository) {
        String candidate;
        do {
            StringBuilder emailBuilder = new StringBuilder();
            emailBuilder.append(firstName);
            if (random.nextBoolean()) {
                emailBuilder.append(DatagenUtil.randomElement(random, EMAIL_NAME_SUFFIX_LIST));
            }
            emailBuilder.append(random.nextInt(10000));
            emailBuilder.append(DatagenUtil.randomElement(random, EMAIL_DOMAIN_NAME_LIST));
            emailBuilder.append(DatagenUtil.randomElement(random, EMAIL_DOMAIN_LIST));
            candidate = emailBuilder.toString();
        } while (userRepository.findApplicationUserByEmail(candidate).isPresent());
        return candidate;
    }

    private static String generatePhoneNumber(Random random) {
        var numberBuilder = new StringBuilder();
        numberBuilder.append(DatagenUtil.randomElement(random, PHONE_AREA_CODES));
        numberBuilder.append(String.format("%03d", random.nextInt(1000)));
        numberBuilder.append(" ");
        numberBuilder.append(String.format("%02d", random.nextInt(100)));
        numberBuilder.append(" ");
        numberBuilder.append(String.format("%02d", random.nextInt(100)));
        numberBuilder.append(" ");
        numberBuilder.append(random.nextInt(100));
        numberBuilder.append(" ");
        numberBuilder.append(String.format("%02d", random.nextInt(100)));
        return numberBuilder.toString();
    }

    public static TestRenter generateRenter(
        Random random,
        ApplicationUserRepository userRepository,
        RenterRepository renterRepository,
        ReputationRenterRepository reputationRepository,
        ReputationService reputationService,
        String password
    ) {
        var firstName = DatagenUtil.randomElement(random, FIRST_NAME_LIST);
        var userNameBuilder = new StringBuilder();
        if (random.nextBoolean()) {
            userNameBuilder.append(DatagenUtil.randomElement(random, NAME_PREFIX_LIST));
        }
        userNameBuilder.append(firstName);
        userNameBuilder.append(DatagenUtil.randomElement(random, NAME_SUFFIX_LIST));
        var username = userNameBuilder.toString();
        var email = generateEmail(random, firstName, userRepository).toLowerCase();

        var userEntity = userRepository.save(ApplicationUser.builder()
            .name(username)
            .email(email)
            .password(password)
            .isLocked(false)
            .isDeleted(false)
            .loginAttempts(0)
            .build());
        var reputationEntity = reputationRepository.save(reputationService.newRenterReputationEntity());
        var renterEntity = renterRepository.save(Renter.builder()
            .id(userEntity.getId())
            .email(userEntity.getEmail())
            .phone(generatePhoneNumber(random))
            .reputation(reputationEntity)
            .isDeleted(false)
            .build());

        return new TestRenter(firstName, userEntity, renterEntity);
    }

    public static TestLender generateLender(
        Random random,
        ApplicationUserRepository userRepository,
        RenterRepository renterRepository,
        ReputationRenterRepository reputationRenterRepository,
        LenderRepository lenderRepository,
        ReputationLenderRepository reputationLenderRepository,
        ReputationService reputationService,
        String password
    ) {
        var base = generateRenter(random, userRepository, renterRepository, reputationRenterRepository, reputationService, password);
        var repuationEntity = reputationLenderRepository.save(reputationService.newLenderReputationEntity());
        var lenderEntity = lenderRepository.save(Lender.builder()
            .id(base.userEntity.getId())
            .phone(base.renterEntity.getPhone())
            .email(base.renterEntity.getEmail())
            .description(random.nextDouble() < 0.7 ? LenderTemplate.LENDER_DESCRIPTION.generateString(random) : null)
            .reputation(repuationEntity)
            .isDeleted(false)
            .build());
        lenderEntity.setCreatedAt(LocalDateTime.now().minusYears(3).minusDays(20 + random.nextInt(20)).minusHours(random.nextInt(10)));
        lenderRepository.save(lenderEntity);

        return new TestLender(base.firstName, base.userEntity, base.renterEntity, lenderEntity);
    }

    public record TestRenter(
        String firstName,
        ApplicationUser userEntity,
        Renter renterEntity
    ) {}

    public record TestLender(
        String firstName,
        ApplicationUser userEntity,
        Renter renterEntity,
        Lender lenderEntity
    ) {}

}
