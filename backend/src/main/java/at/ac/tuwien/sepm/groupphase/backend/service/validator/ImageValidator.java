package at.ac.tuwien.sepm.groupphase.backend.service.validator;

import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageValidator {

    private final UserService userService;
    private final LocationRepository locationRepository;

    /**
     * Validates an image group for creation and the user (check if location owner).
     *
     * @param images the image files as an array
     * @param locationId the id of the location the images belong to
     */
    public void validateImageGroup(MultipartFile[] images, Long locationId, String user) {
        int numberOfImages = images.length;

        if (numberOfImages < 1 || numberOfImages > 10) {
            throw new ValidationException(new ValidationErrorRestDto("Anzahl an Bildern muss zwischen 1 und 10 liegen.", null));
        }

        this.checkIfUserIsOwnerOfLocation(locationId, user);

        for (MultipartFile file : images) {
            this.validateImage(file); //Validate all images individually
        }
    }

    public void validateLocationId(Long locationId) {
        Optional<Location> location = locationRepository.findById(locationId);
        if (location.isEmpty()) {
            throw new NotFoundException("Location " + locationId + " existiert nicht.");
        }
    }

    private void checkIfUserIsOwnerOfLocation(Long locationId, String user) {
        ApplicationUser applicationUser = userService.findApplicationUserByEmail(user);
        Optional<Location> optLocation = locationRepository.findById(locationId);

        if (optLocation.isEmpty()) {
            throw new ValidationException(new ValidationErrorRestDto("Location existiert nicht.", null));
        }

        Location location = optLocation.get();

        if (!location.getOwner().getId().equals(applicationUser.getId())) {
            throw new ValidationException(new ValidationErrorRestDto("User ist nicht der Besitzer der Location.", null));
        }
    }

    public void validateUpdatedGroup(Long locationId, String user) {
        this.checkIfUserIsOwnerOfLocation(locationId, user);
    }

    /**
     * Validates an image for creation.
     * An image can not exceed 1MB.
     * An image can only be of type jpg, jpeg or png.
     *
     * @param file image to check
     * @throws ValidationException in case of invalid file
     */
    private void validateImage(MultipartFile file) {

        if (file.getSize() > 1024 * 1024) {
            throw new ValidationException(new ValidationErrorRestDto("Bild kann nicht größer als 1MB sein.", null));
        }

        String[] allowedTypes = {"jpg", "jpeg", "png", "PNG", "JPG", "JPEG"};
        String[] filename = file.getOriginalFilename().split("\\.");
        if (filename.length < 2 || !allowedTypes[0].equals(filename[1]) && !allowedTypes[1].equals(filename[1]) && !allowedTypes[2].equals(filename[1])) {
            throw new ValidationException(new ValidationErrorRestDto("Bilddatei kann nur jpg, JPG, jpeg, JPEG, png oder PNG sein.", null));
        }

    }
}
