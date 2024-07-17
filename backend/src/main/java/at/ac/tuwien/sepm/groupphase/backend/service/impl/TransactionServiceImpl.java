package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCancelDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCreateDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
import at.ac.tuwien.sepm.groupphase.backend.enums.TransactionStatus;
import at.ac.tuwien.sepm.groupphase.backend.exception.AccessForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.ReputationMapper;
import at.ac.tuwien.sepm.groupphase.backend.mapper.TransactionMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.AdminRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.RenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.TransactionService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.TransactionValidator;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final TimeslotRepository timeslotRepository;
    private final UserService userService;
    private final RenterRepository renterRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionValidator transactionValidator;
    private final AdminRepository adminRepository;
    private final ReputationMapper reputationMapper;

    @Override
    public TransactionDto startTransaction(TransactionCreateDto transactionDto, String user) {
        Optional<Timeslot> optionalTimeslot = timeslotRepository.findById(transactionDto.getTimeslotId());
        Timeslot timeslot;
        if (optionalTimeslot.isPresent()) {
            timeslot = optionalTimeslot.get();
        } else {
            throw new NotFoundException("No timeslot with id " + transactionDto.getTimeslotId() + " found");
        }

        ApplicationUser applicationUser = userService.findApplicationUserByEmail(user);
        Renter renter = renterRepository.findById(applicationUser.getId()).orElseThrow(
            () -> new ValidationException(new ValidationErrorRestDto("User with id " + applicationUser.getId() + " is not a renter", null))
        );

        transactionValidator.validateTransactionForCreate(transactionDto.getInitialMessage(), renter, timeslot);

        Transaction transaction = Transaction.builder()
            .initialMessage(transactionDto.getInitialMessage())
            .timeslot(timeslot)
            .renter(renter)
            .cancelled(false)
            .cancelNotified(false)
            .amountPaid(BigDecimal.ZERO)
            .build();
        return transactionMapper.entityToDto(transactionRepository.save(transaction));
    }

    @Override
    public TransactionDto findOne(Long id) {
        var info = checkUserIsInvolved(id);

        var dto = transactionMapper.entityToDto(info.transaction);
        if (info.isLender) {
            var renter = info.renter;
            if (!renter.getOwner().isDeleted()) {
                var name = Optional.ofNullable(renter.getOwner().getName()).orElse("Anonymer Mieter");
                dto.setPartnerName(name);
                dto.setPartnerEmail(renter.getEmail());
                dto.setPartnerPhone(renter.getPhone());
                dto.setPartnerReputation(reputationMapper.entityToDto(renter.getReputation()));
            } else {
                dto.setPartnerName("Gelöschter User");
            }
            dto.setOwnRoleInTransaction(AppRole.ROLE_LENDER);
        } else {
            var lender = info.lender;
            if (!lender.getOwner().isDeleted()) {
                var name = Optional.ofNullable(lender.getOwner().getName()).orElse("Anonymer Vermieter");
                dto.setPartnerName(name);
                dto.setPartnerEmail(lender.getEmail());
                dto.setPartnerPhone(lender.getPhone());
                dto.setPartnerReputation(reputationMapper.entityToDto(lender.getReputation()));
            } else {
                dto.setPartnerName("Gelöschter User");
            }
            if (info.isAdmin) {
                dto.setOwnRoleInTransaction(AppRole.ROLE_ADMIN);
            } else {
                dto.setOwnRoleInTransaction(AppRole.ROLE_RENTER);
            }
        }

        return dto;
    }

    @Override
    public void recordCancelNotified(Long id) {
        var transaction = checkUserIsInvolved(id).transaction;
        if (transaction.getCancelled()) {
            transaction.setCancelNotified(true);
            transactionRepository.save(transaction);
        }
    }

    @Override
    public PageableDto<TransactionDto> findAllByStatusForRole(TransactionStatus status, AppRole role, int page, int pageSize) {
        var user = UserUtil.getActiveUser();
        if (user == null) {
            throw new AccessForbiddenException("User not authenticated");
        }

        var result = switch (status) {
            case ACTIVE -> transactionRepository.findAllActiveForUserByRole(user.getEmail(), role.toString(), false, PageRequest.of(page, pageSize));
            case ACCEPTED -> transactionRepository.findAllActiveForUserByRole(user.getEmail(), role.toString(), true, PageRequest.of(page, pageSize));
            case CANCELLED -> transactionRepository.findAllCancelledForUserByRole(user.getEmail(), role.toString(), PageRequest.of(page, pageSize));
            case COMPLETED -> transactionRepository.findAllCompletedForUserByRole(user.getEmail(), role.toString(), PageRequest.of(page, pageSize));
            case REVIEWED -> transactionRepository.findAllReviewedForUserByRole(user.getEmail(), role.toString(), PageRequest.of(page, pageSize));
        };

        var transactions = result.stream().map(t -> findOne(t.getId())).toList();
        return new PageableDto<>(result.getTotalElements(), result.getTotalPages(), result.getNumberOfElements(), transactions);
    }

    @Override
    public List<Long> getIdsOutstandingCancelNotificationsForRole(AppRole role) {
        var user = UserUtil.getActiveUser();
        if (user == null) {
            throw new AccessForbiddenException("User not authenticated");
        }

        return transactionRepository.findAllIdsForCancelledAndNotNotified(user.getEmail(), role, role.toString());
    }

    @Override
    public int countAllNotReviewedForRole(AppRole role) {
        var user = UserUtil.getActiveUser();
        if (user == null) {
            throw new AccessForbiddenException("User not authenticated");
        }

        return transactionRepository.countAllNotReviewed(user.getEmail(), role.toString());
    }

    @Override
    public void completeTransaction(Long id, BigDecimal amountPaid) {
        TransactionWithUserInfo info = checkUserIsInvolved(id);

        if (!info.isLender) {
            throw new AccessForbiddenException("Only lender can complete transaction");
        }

        transactionValidator.validateInformationForTransactionComplete(amountPaid);

        Transaction transaction = info.transaction;
        transaction.setAmountPaid(amountPaid);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    @Override
    public void recordTransactionCancelation(TransactionCancelDto transactionCancelDto) {
        TransactionWithUserInfo info = checkUserIsInvolved(transactionCancelDto.getTransactionId());

        if (!info.isLender && !info.isRenter && !info.isAdmin) {
            throw new AccessForbiddenException("User is not involved in transaction, must be lender or renter to cancel.");
        }

        transactionValidator.validateInformationForTransactionCancelation(transactionCancelDto);

        Long transactionId = transactionCancelDto.getTransactionId();
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new NotFoundException(String.format("Could not find transaction with id %s", transactionId));
        }

        // Set the transaction to cancelled in the database
        Transaction transaction = transactionOpt.get();
        transaction.setCancelled(true);
        transaction.setCancelNotified(false);
        transaction.setCancelByRole(info.isLender ? AppRole.ROLE_LENDER : AppRole.ROLE_RENTER);
        transaction.setCancelReason(transactionCancelDto.getCancelReason());
        transaction.setCancelDescription(transactionCancelDto.getCancelMessage());
        transactionRepository.save(transaction);

        Long timeslotId = transaction.getTimeslot().getId();
        Optional<Timeslot> timeslotOpt = timeslotRepository.findById(timeslotId);
        if (timeslotOpt.isEmpty()) {
            throw new RuntimeException(String.format("Could not find timeslot with id %s", timeslotId));
        }

        // Set the timeslot to unused again
        Timeslot timeslot = timeslotOpt.get();
        timeslot.setUsed(false);
        timeslotRepository.save(timeslot);
    }

    @Override
    public PageableDto<TransactionDto> findAllTransactionsForTimeslot(Long timeslotId, int page, int pageSize) {
        var user = UserUtil.getActiveUser();
        var timeslotOpt = timeslotRepository.findById(timeslotId);

        if (timeslotOpt.isEmpty()) {
            throw new NotFoundException("Zeitfenster mit Id %s existiert nicht".formatted(timeslotId));
        }

        if (user == null || !user.getEmail().equals(timeslotOpt.get().getOwningLocation().getOwner().getOwner().getEmail())) {
            throw new ConflictException(
                new ValidationErrorRestDto("Benutzer ist nicht Eigentümer des Standorts des Zeitfenster", null)
            );
        }

        var result = transactionRepository.findAllOngoingByTimeslotId(timeslotId, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "renter.reputation.karma")));
        var transactions = result.stream().map(t -> findOne(t.getId())).toList();

        return new PageableDto<>(result.getTotalElements(), result.getTotalPages(), result.getNumberOfElements(), transactions);
    }

    @Override
    public void cancelTransactionsForTimeslotAsRenter(Long timeslotId) {
        var user = UserUtil.getActiveUser();
        if (user == null) {
            throw new AccessForbiddenException("Benutzer nicht authentifiziert");
        }

        var timeslotOpt = timeslotRepository.findById(timeslotId);
        if (timeslotOpt.isEmpty()) {
            throw new ConflictException(new ValidationErrorRestDto("Zeitfenster mit Id %s existiert nicht".formatted(timeslotId), null));
        }

        transactionRepository.cancelTransactionsForTimeslotAsRenter(timeslotId, user.getEmail());
    }

    @Override
    public void cancelAllActiveTransactionsForRenter(String email) {
        transactionRepository.findAllActiveForRenter(email).forEach(
            (transaction) -> recordTransactionCancelation(TransactionCancelDto.builder()
                .transactionId(transaction.getId())
                .cancelReason(CancelReason.USER_REMOVED)
                .cancelMessage(CancelReason.USER_REMOVED.getDisplayValue())
                .build())
        );
    }

    @Override
    public void acceptTransaction(Long id) {
        TransactionWithUserInfo info = checkUserIsInvolved(id);

        if (!info.isLender) {
            throw new AccessForbiddenException("Only lender can accept transaction");
        }

        if (info.transaction.getTimeslot().isUsed()) {
            throw new ConflictException(new ValidationErrorRestDto("Zeitfenster ist bereits vergeben", null));
        }

        Transaction currentTransaction = info.transaction;
        Long timeslotId = currentTransaction.getTimeslot().getId();

        // Should never be not found or else something is wrong with the database
        Timeslot timeslot = timeslotRepository.findById(timeslotId).orElseThrow();

        List<Transaction> competingTransactions = transactionRepository.findByTimeslotId(timeslotId);
        competingTransactions.remove(currentTransaction);

        // Cancel all other transactions for this timeslot
        for (Transaction competingTransaction : competingTransactions) {
            if (competingTransaction.getCancelled() != null && competingTransaction.getCancelled()) {
                continue;
            }
            competingTransaction.setCancelled(true);
            competingTransaction.setCancelNotified(false);
            competingTransaction.setCancelByRole(AppRole.ROLE_LENDER);
            competingTransaction.setCancelReason(CancelReason.NO_INTEREST);
            competingTransaction.setCancelDescription("Slot an anderen Benutzer vergeben");
            transactionRepository.save(competingTransaction);
        }

        // Accept the current transaction
        timeslot.setUsed(true);
        timeslotRepository.save(timeslot);
    }

    private TransactionWithUserInfo checkUserIsInvolved(Long id) {
        var user = UserUtil.getActiveUser();
        if (user == null) {
            throw new AccessForbiddenException("User not authenticated");
        }

        var transactionOpt = transactionRepository.findById(id);

        if (transactionOpt.isEmpty()) {
            throw new NotFoundException(String.format("Could not find transaction with id %s", id));
        }
        var transaction = transactionOpt.get();

        var renter = transaction.getRenter();
        var lender = transaction.getTimeslot().getOwningLocation().getOwner();

        var isRenter = renter.getOwner().getEmail().equals(user.getEmail());
        var isLender = lender.getOwner().getEmail().equals(user.getEmail());
        var isAdmin = adminRepository.existsByOwnerEmail(user.getEmail());

        if (!isRenter && !isLender && !isAdmin) {
            throw new AccessForbiddenException("User is not involved in this transaction");
        }

        return new TransactionWithUserInfo(transaction, renter, lender, isRenter, isLender, isAdmin);
    }

    @AllArgsConstructor
    private static class TransactionWithUserInfo {
        private Transaction transaction;
        private Renter renter;
        private Lender lender;
        private boolean isRenter;
        private boolean isLender;
        private boolean isAdmin;
    }

}
