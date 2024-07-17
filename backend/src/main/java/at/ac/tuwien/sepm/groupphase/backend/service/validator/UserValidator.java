package at.ac.tuwien.sepm.groupphase.backend.service.validator;

import at.ac.tuwien.sepm.groupphase.backend.dto.ChangeUserPasswordDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.UserDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.AdminRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final ApplicationUserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PlzRepository plzRepository;

    public void validateSignUpUser(SignUpDto signUpDto) {
        var validationErrors = new ArrayList<ValidationErrorDto>();
        var conflictErrors = new ArrayList<ValidationErrorDto>();

        if (signUpDto.getUsername().isBlank()) {
            addError(validationErrors, "Username darf nicht leer sein");
        }

        if (signUpDto.getEmail().isBlank()) {
            addError(validationErrors, "Email darf nicht leer sein");
        }

        if (signUpDto.getOriginalPassword().isBlank()) {
            addError(validationErrors, "Passwort darf nicht leer sein");
        }

        if (signUpDto.getRepeatedPassword().isBlank()) {
            addError(validationErrors, "Passwort darf nicht leer sein");
        }

        if (signUpDto.getUsername() != null) {
            var username = signUpDto.getUsername();
            if (username.length() > 100) {
                addError(validationErrors, "Username darf nicht mehr als 100 Symbole betragen");
            }
        }

        if (signUpDto.getEmail() != null && signUpDto.getEmail().length() > 0) {
            var email = signUpDto.getEmail();
            Optional<ApplicationUser> applicationUser = userRepository.findApplicationUserByEmail(email);
            if (applicationUser.isPresent()) {
                addError(conflictErrors, "Email bereits in Verwendung");
            }

            validateEmail(validationErrors, email);
        }

        if (signUpDto.getOriginalPassword() != null
            && signUpDto.getRepeatedPassword() != null
            && signUpDto.getOriginalPassword().length() > 0
            && signUpDto.getRepeatedPassword().length() > 0) {
            String originalPassword = signUpDto.getOriginalPassword();
            String repeatedPassword = signUpDto.getRepeatedPassword();

            if (originalPassword.compareTo(repeatedPassword) != 0) {
                addError(validationErrors, "Passwörter stimmen nicht überein.");
            }

            if (originalPassword.length() < 4) {
                addError(validationErrors, "Passwort muss mindestens 4 Symbole betragen.");
            }

            if (originalPassword.length() > 50) {
                addError(validationErrors, "Passwort darf nicht mehr als 50 Symbole betragen.");
            }
        }

        if (validationErrors.size() > 0) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für User Erstellung fehlgeschlagen")
                    .errors(validationErrors)
                    .build()
            );
        }

        if (conflictErrors.size() > 0) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Fehler beim Erstellen des Users, Konflikt mit bereits vorhandenem User")
                    .errors(conflictErrors)
                    .build()
            );
        }
    }

    public void validateUserRequest(ApplicationUser requestedUser) {
        checkPermissionForUser(requestedUser);
    }

    private void checkPermissionForUser(ApplicationUser requestedUser) {
        // Check if the user has permission to access the requested user's information
        var applicationUser = UserUtil.getActiveUser();
        if (requestedUser != null && applicationUser != null) {
            boolean isUser = applicationUser.getEmail().equals(requestedUser.getEmail());
            boolean isAdmin = adminRepository.existsByOwnerEmail(applicationUser.getEmail());
            if (!isUser && !isAdmin) {
                throw new AccessDeniedException("Zugriff verweigert");
            }
        } else {
            throw new NotFoundException("User wurde nicht gefunden");
        }
    }

    public void validateDeleteUser(String email) {
        var activeUser = UserUtil.getActiveUser();

        boolean notAllowed = activeUser == null
            || (!activeUser.getEmail().equals(email)
            && activeUser.getRoles().stream().noneMatch(x -> x.getAuthority().equals(AppRole.ROLE_ADMIN.toString()))
        );

        if (notAllowed) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Fehler beim Löschen des Users")
                    .errors(List.of(
                        ValidationErrorDto.builder()
                            .message("Keine Berechtigung")
                            .build()
                    ))
                    .build()
            );
        }
    }

    private void addError(ArrayList<ValidationErrorDto> list, String message) {
        list.add(ValidationErrorDto.builder()
            .message(String.format(message))
            .build());
    }

    public void validateUpdateUser(Long id, UserDto userDto) {
        var validationErrors = new ArrayList<ValidationErrorDto>();
        var conflictErrors = new ArrayList<ValidationErrorDto>();

        if (userDto.getName().isBlank()) {
            addError(validationErrors, "Username darf nicht leer sein");
        }

        if (userDto.getEmail().isBlank()) {
            addError(validationErrors, "Email darf nicht leer sein");
        }

        if (userDto.getPlz() != null && !plzRepository.existsById(userDto.getPlz().getPlz())) {
            addError(validationErrors, "PLZ muss gültig sein");
        }

        if (userDto.getName() != null) {
            var username = userDto.getName();
            if (username.length() > 100) {
                addError(validationErrors, "Username darf nicht mehr als 100 Symbole betragen");
            }
        }

        if (userDto.getEmail() != null && userDto.getEmail().length() > 0) {
            var email = userDto.getEmail();
            Optional<ApplicationUser> applicationUser = userRepository.findApplicationUserByEmail(email);
            if (applicationUser.isPresent() && !Objects.equals(applicationUser.get().getId(), id)) {
                boolean isUser = applicationUser.get().getEmail().equals(email);
                boolean isAdmin = adminRepository.existsByOwnerEmail(applicationUser.get().getEmail());
                if (!isUser && !isAdmin) {
                    addError(conflictErrors, "Email bereits von anderem Account in Verwendung");
                }
            }

            validateEmail(validationErrors, email);
        }

        if (userDto.getLenderEmail() != null) {
            validateEmail(validationErrors, userDto.getLenderEmail());
        }

        if (userDto.getRenterEmail() != null) {
            validateEmail(validationErrors, userDto.getRenterEmail());
        }

        if (validationErrors.size() > 0) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für User Update fehlgeschlagen")
                    .errors(validationErrors)
                    .build()
            );
        }

        if (conflictErrors.size() > 0) {
            throw new ConflictException(
                ValidationErrorRestDto.builder()
                    .message("Fehler beim Updaten des Users, Konflikt mit bereits vorhandenem User")
                    .errors(conflictErrors)
                    .build()
            );
        }
    }

    private void validateEmail(ArrayList<ValidationErrorDto> validationErrors, String email) {
        if (email.length() > 100) {
            addError(validationErrors, "Email darf nicht mehr als 100 Symbole betragen");
        }

        if (email.isBlank()) {
            addError(validationErrors, "Email darf nicht leer sein");
        } else {
            if (!email.contains("@")) {
                addError(validationErrors, "Email muss @ enthalten");
            }

            if (email.charAt(0) == '@' || email.charAt(email.length() - 1) == '@') {
                addError(validationErrors, "Email darf nicht mit @ beginnen oder enden");
            }

            if (StringUtils.countOccurrencesOf(email, "@") > 1) {
                addError(validationErrors, "Email muss @ genau einmal enthalten");
            }
        }

    }

    public void validateChangePassword(ApplicationUser requestedUser, ChangeUserPasswordDto dto) {
        var validationErrors = new ArrayList<ValidationErrorDto>();

        checkPermissionForUser(requestedUser);

        if (dto.getNewPassword().isBlank()) {
            addError(validationErrors, "Passwort darf nicht leer sein");
        }

        if (dto.getRepeatedPassword().isBlank()) {
            addError(validationErrors, "Passwort darf nicht leer sein");
        }

        if (dto.getNewPassword() != null
            && dto.getRepeatedPassword() != null
            && dto.getNewPassword().length() > 0
            && dto.getRepeatedPassword().length() > 0) {
            String newPassword = dto.getNewPassword();
            String repeatedPassword = dto.getRepeatedPassword();

            if (newPassword.compareTo(repeatedPassword) != 0) {
                addError(validationErrors, "Passwörter stimmen nicht überein.");
            }

            if (newPassword.length() < 4) {
                addError(validationErrors, "Passwort muss mindestens 4 Symbole betragen.");
            }

            if (newPassword.length() > 50) {
                addError(validationErrors, "Passwort darf nicht mehr als 50 Symbole betragen.");
            }
        }


        if (validationErrors.size() > 0) {
            throw new ValidationException(
                ValidationErrorRestDto.builder()
                    .message("Validierung für Passwort-Änderung fehlgeschlagen")
                    .errors(validationErrors)
                    .build()
            );
        }
    }
}
