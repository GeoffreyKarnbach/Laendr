package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.ImageCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ImageUpdatedCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping(value = "/api/v1/images")
@Slf4j
@RequiredArgsConstructor
public class ImageEndpoint {

    private final ImageService imageService;

    @Secured("ROLE_LENDER")
    @PostMapping
    @Operation(summary = "Upload new array of images belonging to specific location", security = @SecurityRequirement(name = "apiKey"))
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadImages(@RequestParam("images") MultipartFile[] images, @RequestParam("locationId") Long locationId) {
        log.info("POST /api/v1/images/{}", images);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        imageService.uploadMultipleImages(images, locationId, authentication.getName());
    }

    @Secured("ROLE_LENDER")
    @PutMapping
    @Operation(summary = "Upload updated array of images belonging to specific location", security = @SecurityRequirement(name = "apiKey"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateImages(@RequestBody ImageUpdatedCollectionDto images) {
        log.info("PUT /api/v1/images/{}", images);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        imageService.updateImages(images, authentication.getName());
    }

    @PermitAll
    @GetMapping(value = "/all/{id}")
    @Operation(summary = "Returns an array of all image URLs belonging to the specified location", security = @SecurityRequirement(name = "apiKey"))
    @ResponseStatus(HttpStatus.OK)
    public ImageCollectionDto getImages(@PathVariable Long id) {
        log.info("GET /api/v1/images/{}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return imageService.getAllImages(id, authentication.getName());
    }

    @PermitAll
    @GetMapping(value = "/{fileName}")
    @Operation(summary = "Returns the specified image")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InputStreamResource> getImage(@PathVariable("fileName") String fileName) {
        log.info("GET /api/v1/images/{}", fileName);

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        MediaType mediaType = extension.equals("png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok().contentType(mediaType).body(new InputStreamResource(imageService.getImage(fileName)));
    }
}
