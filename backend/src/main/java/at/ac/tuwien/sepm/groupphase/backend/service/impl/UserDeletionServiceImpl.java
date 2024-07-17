package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import at.ac.tuwien.sepm.groupphase.backend.service.TransactionService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserDeletionService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDeletionServiceImpl implements UserDeletionService {

    private final UserService userService;
    private final TransactionService transactionService;
    private final ApplicationUserRepository applicationUserRepository;
    private final RenterRepository renterRepository;
    private final LenderRepository lenderRepository;
    private final LocationService locationService;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public void deleteUser(long id) {
        var userEntity = applicationUserRepository.findById(id);
        if (!userEntity.isPresent()) {
            throw new NotFoundException("Nutzer mit ID %d nicht gefunden".formatted(id));
        }

        var user = userEntity.get();
        var email = user.getEmail();

        userValidator.validateDeleteUser(email);

        var renter = user.getRenter();
        var lender = user.getLender();

        if (lender != null) {
            locationService.removeAllExistingLocationsForLender(email);

            lender.setPhone(null);
            lender.setEmail(null);
            lender.setDescription(null);
            lender.setDeleted(true);

            lenderRepository.save(lender);
        }

        if (renter != null) {
            transactionService.cancelAllActiveTransactionsForRenter(email);

            renter.setPhone(null);
            renter.setEmail(null);
            renter.setDeleted(true);

            renterRepository.save(renter);
        }

        user.setEmail(java.util.UUID.randomUUID() + "@deleted.laendr");
        user.setPassword("");
        user.setName("Gelöschter User");
        user.setPlz(null);
        user.setState(null);
        user.setLocked(true);
        user.setDeleted(true);
        user.setLoginAttempts(0);

        applicationUserRepository.save(user);
    }
}
