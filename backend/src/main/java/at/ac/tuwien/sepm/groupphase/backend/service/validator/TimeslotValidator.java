package at.ac.tuwien.sepm.groupphase.backend.service.validator;

import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TimeslotSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TimeslotValidator {

    private final LocationRepository locationRepository;
    private final TimeslotRepository timeslotRepository;

    public void validateTimeslotSearchDto(TimeslotSearchDto searchDto) {

        if (searchDto.getDay().isBefore(LocalDate.now())) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für die Werte zur Zeitfenster Suche fehlgeschlagen")
                    .errors(
                        List.of(ValidationErrorDto.builder()
                            .message("Angefragter Tag darf nicht in der Vergangenheit sein")
                            .build()
                        )
                    )
                    .build()
            );
        }

        Long locationId = searchDto.getLocationId();
        var conflictErrors = new ArrayList<String>();

        if (searchDto.isCallerIsLocationOwner()) {
            checkCallingUserIsLocationOwner(locationId, conflictErrors);
        }

        if (!locationRepository.existsById(locationId)) {
            conflictErrors.add("Standort mit Id %s existiert nicht".formatted(locationId));
        }

        if (conflictErrors.size() > 0) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für die Werte zur Zeitfenster Suche fehlgeschlagen")
                    .errors(
                        conflictErrors.stream().map(
                            message -> ValidationErrorDto.builder()
                                .message(message).build()
                        ).toList()
                    )
                    .build()
            );
        }
    }

    public void validateTimeslotForDelete(Long timeslotId) {
        var timeslotOpt = timeslotRepository.findById(timeslotId);
        if (timeslotOpt.isEmpty()) {
            throw new NotFoundException("Zeitfenster mit Id %s existiert nicht".formatted(timeslotId));
        }

        var timeslot = timeslotOpt.get();
        var conflictErrors = new ArrayList<ValidationErrorDto>();

        if (timeslot.isUsed()) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message("Zeitfenster ist gebucht und kann daher nicht gelöscht werden")
                    .build()
            );
        }

        if (!timeslot.isUsed() && timeslotRepository.isTimeslotRequested(timeslotId)) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message("Zeitfenster ist angefragt und kann daher nicht gelöscht werden")
                    .build()
            );
        }

        if (!isCallingUserTimeslotOwner(timeslot)) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message("Benutzer ist nicht Eigentümer des Standorts")
                    .build()
            );
        }

        if (conflictErrors.size() > 0) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für das Löschen eines Zeitfensters fehlgeschlagen")
                    .errors(conflictErrors)
                    .build()
            );
        }
    }

    public void validateTimeslotForUpdate(Timeslot currentTimeslot, TimeslotDto timeslotDto) {
        var validationErrors = new ArrayList<String>();
        if (timeslotDto.getId() == null) {
            validationErrors.add("Id von dem zu aktualisierendem Zeitfenster muss angegeben werden");
        }

        validateSimpleFields(timeslotDto, validationErrors);

        if (timeslotDto.getIsUsed() != null && timeslotDto.getIsUsed() != currentTimeslot.isUsed()) {
            validationErrors.add("Verwendungsstatus des Zeitfenster darf nicht verändert werden");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für das Aktualisieren eines Zeitfensters fehlgeschlagen")
                    .errors(
                        validationErrors.stream().map(
                            message -> ValidationErrorDto.builder()
                                .message(message).build()
                        ).toList()
                    ).build()
            );
        }


        var conflictErrors = new ArrayList<String>();

        if (!isCallingUserTimeslotOwner(currentTimeslot)) {
            conflictErrors.add("Benutzer ist nicht Eigentümer des Standorts");
        }

        if (currentTimeslot.isUsed()) {
            conflictErrors.add("Zeitfenster ist gebucht und kann daher nicht aktualisiert werden");
        }

        if (!currentTimeslot.isUsed() && timeslotRepository.isTimeslotRequested(currentTimeslot.getId())) {
            conflictErrors.add("Zeitfenster ist angefragt und kann daher nicht aktualisiert werden");
        }

        if (!conflictErrors.isEmpty()) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für das Aktualisieren eines Zeitfensters fehlgeschlagen")
                    .errors(
                        conflictErrors.stream().map(
                            message -> ValidationErrorDto.builder()
                                .message(message).build()
                        ).toList()
                    )
                    .build()
            );
        }
    }

    public void validateTimeslotForCreate(TimeslotDto timeslotDto) {
        var validationErrors = new ArrayList<String>();
        validateSimpleFields(timeslotDto, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für das Anlegen eines Zeitfensters fehlgeschlagen")
                    .errors(
                        validationErrors.stream().map(
                            message -> ValidationErrorDto.builder()
                                .message(message).build()
                        ).toList()
                    ).build()
            );
        }

        var conflictErrors = new ArrayList<String>();
        var locationId = timeslotDto.getLocationId();
        if (locationId != null) {
            checkCallingUserIsLocationOwner(locationId, conflictErrors);
        } else {
            conflictErrors.add("Standort muss angegeben werden");
        }

        if (!conflictErrors.isEmpty()) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für das Anlegen eines Zeitfensters fehlgeschlagen")
                    .errors(
                        conflictErrors.stream().map(
                            message -> ValidationErrorDto.builder()
                                .message(message).build()
                        ).toList()
                    )
                    .build()
            );
        }
    }

    private void checkCallingUserIsLocationOwner(Long locationId, ArrayList<String> conflictErrors) {
        var locationOpt = locationRepository.findById(locationId);
        if (locationOpt.isPresent()) {

            var ownerEmailOpt = locationRepository.findOwnerEmailById(locationId);
            var user = UserUtil.getActiveUser();
            if (user == null || ownerEmailOpt.isEmpty() || !ownerEmailOpt.get().equals(user.getEmail())) {
                conflictErrors.add("Benutzer ist nicht Eigentümer des Standorts mit Id %s".formatted(locationId));
            }
        } else {
            conflictErrors.add("Angegebener Standort mit Id %s existiert nicht".formatted(locationId));
        }
    }

    private static void validateSimpleFields(TimeslotDto timeslotDto, ArrayList<String> validationErrors) {
        if (timeslotDto.getStart().isBefore(LocalDateTime.now())) {
            validationErrors.add("Startzeitpunkt muss in der Zukunft sein");
        }

        if (timeslotDto.getEnd().isBefore(timeslotDto.getStart())) {
            validationErrors.add("Endzeitpunkt muss nach Startzeitpunkt sein");
        }

        if (timeslotDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            validationErrors.add("Preis muss größer als 0 sein");
        }
    }

    private boolean isCallingUserTimeslotOwner(Timeslot timeslot) {
        var user = UserUtil.getActiveUser();
        return user != null && user.getEmail().equals(timeslot.getOwningLocation().getOwner().getOwner().getEmail());
    }
}
