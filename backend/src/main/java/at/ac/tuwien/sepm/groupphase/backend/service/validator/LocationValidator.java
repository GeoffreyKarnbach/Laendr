package at.ac.tuwien.sepm.groupphase.backend.service.validator;

import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.LocationTag;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.LocationMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationTagRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocationValidator {

    private static final int maxShortString = 255;
    private static final int maxLongString = 1000;
    private final BigDecimal minLocationSize = new BigDecimal(0.0);
    private final BigDecimal maxLocationSize = new BigDecimal(100000.0);

    private final LocationMapper locationMapper;
    private final LenderRepository lenderRepository;
    private final PlzRepository plzRepository;

    private final LocationTagRepository locationTagRepository;

    private void checkValidationError(Location location) {
        List<String> validationErrors = new ArrayList<>();

        if (location.getName() == null || location.getName().isBlank()) {
            validationErrors.add("Name darf nicht leer sein.");
        }
        if (location.getName().length() > maxShortString) {
            validationErrors.add("Name darf nicht länger als " + maxShortString + " Symbole sein.");
        }

        if (location.getDescription() == null) {
            validationErrors.add("Beschreibung muss angegeben werden.");
        }
        if (location.getDescription().length() > maxLongString) {
            validationErrors.add("Beschreibung darf nicht länger als " + maxLongString + " Symbole sein.");
        }

        if (location.getPlz() == null || location.getPlz().getPlz() == null || location.getPlz().getPlz().isBlank()) {
            validationErrors.add("PLZ muss angegeben werden.");
        } else if (!plzRepository.existsById(location.getPlz().getPlz())) {
            validationErrors.add("PLZ muss existieren.");
        }

        if (location.getState() == null) {
            validationErrors.add("Bundesland muss angegeben werden.");
        }

        if (location.getAddress() == null || location.getAddress().isBlank()) {
            validationErrors.add("Addresse darf nicht leer sein.");
        }
        if (location.getAddress().length() > maxShortString) {
            validationErrors.add("Address darf nicht länger als " + maxShortString + " Symbole sein.");
        }

        if (location.getSizeInM2() == null) {
            validationErrors.add("Größe muss angegeben werden.");
        }
        if (location.getSizeInM2().compareTo(minLocationSize) <= 0) {
            validationErrors.add("Größe muss mindestens " + minLocationSize + " betragen.");
        }
        if (location.getSizeInM2().compareTo(maxLocationSize) > 0) {
            validationErrors.add("Größe darf höchstens " + maxLocationSize + " betragen.");
        }

        List<ValidationErrorDto> validationErrorDtos = new ArrayList<>();

        for (int i = 0; i < validationErrors.size(); i++) {
            validationErrorDtos.add(new ValidationErrorDto(Long.valueOf(i), validationErrors.get(i), null));
        }

        if (validationErrors.size() > 0) {
            throw new ValidationException(new ValidationErrorRestDto("Validierungsfehler bei Location", validationErrorDtos));
        }
    }

    public void validateLocationForCreation(LocationCreationDto location) {

        Location toCheck = locationMapper.dtoToEntity(location);
        this.checkValidationError(toCheck);
    }

    public void validateLocationTags(List<String> tags) {
        List<String> validationErrors = new ArrayList<>();

        for (String tag : tags) {
            Optional<LocationTag> locationTag = locationTagRepository.findByName(tag);
            if (locationTag.isEmpty()) {
                validationErrors.add("Tag '" + tag + "' existiert nicht.");
            }
        }

        List<ValidationErrorDto> validationErrorDtos = new ArrayList<>();

        for (int i = 0; i < validationErrors.size(); i++) {
            validationErrorDtos.add(new ValidationErrorDto(Long.valueOf(i), validationErrors.get(i), null));
        }

        if (validationErrors.size() > 0) {
            throw new ValidationException(new ValidationErrorRestDto("Validation Fehler Location", validationErrorDtos));
        }
    }

    public void validateLocationForUpdate(LocationDto location) {

        Location toCheck = locationMapper.dtoToEntity(location);
        this.checkValidationError(toCheck);
    }

    public void validateLocationForFilter(LocationFilterDto filterDto) {

        var conflictErrors = new ArrayList<ValidationErrorDto>();

        if (filterDto.getPriceFrom() != null && filterDto.getPriceTo() != null) {
            if (filterDto.getPriceFrom().compareTo(filterDto.getPriceTo()) == 1) {
                conflictErrors.add(
                    ValidationErrorDto.builder()
                        .message(String.format("Minimale Preis darf nicht größer als maximaler Preis sein."))
                        .build()
                );
            }

        }

        if (filterDto.getTimeFrom() != null && filterDto.getTimeFrom().isBefore(LocalDate.now())) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message("'Zeitraum von' kann nicht in der Vergangenheit liegen!")
                    .build()
            );
        }

        if (filterDto.getTimeTo() != null && filterDto.getTimeTo().isBefore(LocalDate.now())) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message("'Zeitraum bis' kann nicht in der Vergangenheit liegen!")
                    .build()
            );
        }

        if (filterDto.getTimeFrom() != null && filterDto.getTimeTo() != null) {
            if (filterDto.getTimeFrom().compareTo(filterDto.getTimeTo()) > 0) {
                conflictErrors.add(
                    ValidationErrorDto.builder()
                        .message(String.format("Anfangszeit darf nicht Endzeit überschreiten."))
                        .build()
                );
            }
        }

        if (filterDto.getPosition() != null
            && filterDto.getPosition().getDistance() != null
            && filterDto.getPosition().getDistance().compareTo(BigDecimal.ZERO) < 0) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message("Die Distanz darf nicht negativ sein!")
                    .build()
            );
        }

        if (filterDto.getPosition() != null && filterDto.getPosition().getCoord() != null
            && (filterDto.getPosition().getCoord().getLat() == null || filterDto.getPosition().getCoord().getLng() == null)) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message("Die Koordinaten müssen vollständig sein!")
                    .build()
            );
        }

        if (filterDto.getTags() != null && filterDto.getTags().length > 0) {
            validateLocationTags(List.of(filterDto.getTags()));
        }

        if (conflictErrors.size() > 0) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Validierung von Location-filter fehlgeschlagen.")
                    .errors(conflictErrors)
                    .build()
            );
        }
    }

    public void validateLocationsForLenderRequest(long lenderId, boolean includeRemovedLocations) {

        var conflictErrors = new ArrayList<ValidationErrorDto>();

        var lender = lenderRepository.findById(lenderId);

        if (lender.isEmpty()) {
            conflictErrors.add(
                ValidationErrorDto.builder()
                    .message(String.format("Vermieter mit ID %s konnte nicht gefunden werden", lenderId))
                    .build()
            );
        }

        if (includeRemovedLocations) {
            var requestingUser = UserUtil.getActiveUser();
            var bothUsersFound = lender.isPresent() && requestingUser != null;
            if (bothUsersFound && !lender.get().getOwner().getEmail().equals(requestingUser.getEmail())) {
                conflictErrors.add(
                    ValidationErrorDto.builder()
                        .message(String.format("Anfrage kommt nicht von Vermieter mit ID %s", lenderId))
                        .build()
                );
            }
        }

        if (conflictErrors.size() > 0) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Validierung der Suchparameter für Location fehlgeschlagen.")
                    .errors(conflictErrors)
                    .build()
            );
        }
    }
}
