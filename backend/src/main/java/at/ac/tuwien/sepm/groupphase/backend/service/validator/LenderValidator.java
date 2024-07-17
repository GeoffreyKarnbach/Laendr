package at.ac.tuwien.sepm.groupphase.backend.service.validator;

import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class LenderValidator {

    public void validateAddLenderRole(ApplicationUser user) {
        var conflictErrors = new ArrayList<ValidationErrorDto>();

        var activeUser = UserUtil.getActiveUser();
        if (activeUser == null || !activeUser.getEmail().equals(user.getEmail())) {
            addError(conflictErrors, "Nutzer ist nicht Account-Besitzer");
        } else {

            if (user.getLender() != null) {
                addError(conflictErrors, "Nutzer ist bereits Vermieter");
            }

        }

        if (conflictErrors.size() > 0) {
            throw new ConflictException(ValidationErrorRestDto.builder()
                .message("Fehler beim Erstellen des Vermieters")
                .errors(conflictErrors)
                .build());
        }
    }

    private void addError(ArrayList<ValidationErrorDto> list, String message) {
        list.add(ValidationErrorDto.builder()
            .message(String.format(message))
            .build());
    }

}
