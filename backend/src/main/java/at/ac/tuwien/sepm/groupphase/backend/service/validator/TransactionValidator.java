package at.ac.tuwien.sepm.groupphase.backend.service.validator;

import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCancelDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionValidator {

    private final TransactionRepository transactionRepository;
    private static final int maxLongString = 1000;
    private static final int maxShortString = 100;

    public void validateTransactionForCreate(String initialMessage, Renter renter, Timeslot timeslot) {
        List<String> validationErrors = new ArrayList<>();

        List<ValidationErrorDto> validationErrorDtos = new ArrayList<>();

        if (initialMessage == null) {
            log.info("validateTransactionForCreate: message is null/blank");
            validationErrors.add("Nachricht muss angegeben werden.");
        } else if (initialMessage.length() > maxLongString) {
            log.info("validateTransactionForCreate: message is to long");
            validationErrors.add("Nachricht darf maximal " + maxLongString + " Symbole lang sein.");
        }

        if (renter.getId().equals(timeslot.getOwningLocation().getOwner().getId())) {
            log.info("validateTransactionForCreate: renter is owner of location");
            validationErrors.add("Mieter darf nicht der Besitzer der Location sein.");
        }

        for (int i = 0; i < validationErrors.size(); i++) {
            validationErrorDtos.add(new ValidationErrorDto((long) i, validationErrors.get(i), null));
        }

        if (validationErrors.size() > 0) {
            throw new ValidationException(new ValidationErrorRestDto("Validierungsfehler", validationErrorDtos));
        }
    }

    public void validateInformationForTransactionComplete(BigDecimal amountPaid) {

        List<String> validationErrors = new ArrayList<>();

        List<ValidationErrorDto> validationErrorDtos = new ArrayList<>();

        if (amountPaid.compareTo(BigDecimal.ZERO) < 0) {
            log.info("validateInformationForTransactionComplete: amountPaid is negative");
            validationErrors.add("Preis darf nicht negativ sein.");
        }

        for (int i = 0; i < validationErrors.size(); i++) {
            validationErrorDtos.add(new ValidationErrorDto((long) i, validationErrors.get(i), null));
        }

        if (validationErrors.size() > 0) {
            throw new ValidationException(new ValidationErrorRestDto("Validierungsfehler", validationErrorDtos));
        }
    }

    public void validateInformationForTransactionCancelation(TransactionCancelDto transactionCancelDto) {
        List<String> validationErrors = new ArrayList<>();

        List<ValidationErrorDto> validationErrorDtos = new ArrayList<>();

        if (transactionCancelDto.getCancelReason() == null) {
            log.info("validateInformationForTransactionCancelation: cancelReason is null");
            validationErrors.add("Abbruchgrund muss ausgewählt werden.");
        }

        if (transactionCancelDto.getCancelMessage() != null) {
            if (transactionCancelDto.getCancelMessage().length() > maxShortString) {
                log.info("validateInformationForTransactionCancelation: cancelMessage is to long");
                validationErrors.add("Begründung darf maximal " + maxShortString + " Symbole lang sein.");
            }
            if (transactionCancelDto.getCancelMessage().isBlank()) {
                log.info("validateInformationForTransactionCancelation: cancelMessage is null/blank");
                validationErrors.add("Begründung darf nicht leer sein.");
            }
        }

        Long transactionId = transactionCancelDto.getTransactionId();
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new NotFoundException(String.format("Could not find transaction with id %s", transactionId));
        }

        Transaction transaction = transactionOpt.get();

        if (transaction.getCancelled() != null && transaction.getCancelled()) {
            log.info("validateInformationForTransactionCancelation: transaction is already cancelled");
            validationErrors.add("Transaktion ist bereits abgebrochen.");
        }

        if (transaction.getCompletedAt() != null) {
            log.info("validateInformationForTransactionCancelation: transaction is already completed");
            validationErrors.add("Transaktion ist bereits abgeschlossen.");
        }

        for (int i = 0; i < validationErrors.size(); i++) {
            validationErrorDtos.add(new ValidationErrorDto((long) i, validationErrors.get(i), null));
        }

        if (validationErrors.size() > 0) {
            throw new ValidationException(new ValidationErrorRestDto("Validierungsfehler", validationErrorDtos));
        }
    }

}
