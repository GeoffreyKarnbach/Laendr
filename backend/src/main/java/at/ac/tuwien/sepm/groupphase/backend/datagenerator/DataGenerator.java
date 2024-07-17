package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.Admin;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.repository.AdminRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ImageRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationTagRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.stream.IntStream;

@Profile("generateData")
@Component
@Slf4j
@RequiredArgsConstructor
@DependsOn({"plzDataGenerator", "tagDataGenerator"})
public class DataGenerator {

    private static final int RENTER_COUNT = 100;
    private static final int LENDER_COUNT = 30;

    private static final int LOCATION_COUNT = 40;

    private final ApplicationUserRepository applicationUserRepository;
    private final AdminRepository adminRepository;
    private final RenterRepository renterRepository;
    private final LenderRepository lenderRepository;
    private final LocationRepository locationRepository;
    private final ReputationRenterRepository reputationRenterRepository;
    private final ReputationLenderRepository reputationLenderRepository;
    private final ReputationLocationRepository reputationLocationRepository;
    private final ReputationService reputationService;
    private final PlzRepository plzRepository;
    private final TimeslotRepository timeslotRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewLocationRepository reviewLocationRepository;
    private final ReviewRenterRepository reviewRenterRepository;
    private final ImageRepository imageRepository;

    private final LocationTagRepository locationTagRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    private void generateData() {
        if (applicationUserRepository.findAll().size() > 0
            || locationRepository.findAll().size() > 0
            || timeslotRepository.findAll().size() > 0
            || transactionRepository.findAll().size() > 0) {
            log.info("Data already existing");
        } else {
            log.info("Generating test data");
            String password = passwordEncoder.encode("password");

            var adminUser = applicationUserRepository.save(ApplicationUser.builder()
                .name("Admin")
                .email("admin@email.com")
                .password(password)
                .isLocked(false)
                .isDeleted(false)
                .loginAttempts(0)
                .build());
            adminRepository.save(Admin.builder().id(adminUser.getId()).build());
            renterRepository.save(Renter.builder()
                .id(adminUser.getId())
                .reputation(reputationRenterRepository.save(reputationService.newRenterReputationEntity()))
                .isDeleted(false)
                .build());

            var renterUser = applicationUserRepository.save(ApplicationUser.builder()
                .name("Renter")
                .email("renter@email.com")
                .password(password)
                .isLocked(false)
                .isDeleted(false)
                .loginAttempts(0)
                .build());
            renterRepository.save(Renter.builder()
                .id(renterUser.getId())
                .reputation(reputationRenterRepository.save(reputationService.newRenterReputationEntity()))
                .isDeleted(false)
                .build());

            var lenderUser = applicationUserRepository.save(ApplicationUser.builder()
                .name("Lender")
                .email("lender@email.com")
                .password(password)
                .isLocked(false)
                .isDeleted(false)
                .loginAttempts(0)
                .build());
            renterRepository.save(Renter.builder()
                .id(lenderUser.getId())
                .reputation(reputationRenterRepository.save(reputationService.newRenterReputationEntity()))
                .isDeleted(false)
                .build());
            lenderRepository.save(Lender.builder()
                .id(lenderUser.getId())
                .email("lender@email.com")
                .phone("+43 123 45 67 8 90")
                .isDeleted(false)
                .reputation(reputationLenderRepository.save(reputationService.newLenderReputationEntity()))
                .build());

            Random random = new Random(0xBEEF);

            var renters = IntStream.range(0, RENTER_COUNT)
                .mapToObj((i) -> UserDataGenerator.generateRenter(
                    random, applicationUserRepository, renterRepository, reputationRenterRepository, reputationService, password))
                .toList();

            var lenders = IntStream.range(0, LENDER_COUNT)
                .mapToObj((i) -> UserDataGenerator.generateLender(
                    random, applicationUserRepository, renterRepository, reputationRenterRepository, lenderRepository, reputationLenderRepository,
                    reputationService, password))
                .toList();

            var tags = locationTagRepository.findAll();

            var locations = IntStream.range(0, LOCATION_COUNT)
                .mapToObj((i) -> LocationDataGenerator.generateLocation(random, lenders, tags, locationRepository, reputationLocationRepository, reputationService,
                    plzRepository, timeslotRepository))
                .toList();


            TransactionDataGenerator.generateTransactions(random, renters, locations, transactionRepository, reviewLocationRepository, reviewRenterRepository, timeslotRepository, reputationService);
            lenders.stream().map(UserDataGenerator.TestLender::lenderEntity).forEach((l) -> reputationService.updateReputationForLender(l.getId()));

            ImageDataGenerator.generateImages(random, locations, locationRepository, imageRepository);
        }

    }

}
