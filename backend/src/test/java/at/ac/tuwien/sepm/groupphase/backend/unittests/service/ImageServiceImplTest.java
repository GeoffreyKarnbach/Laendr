package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.dto.ImageUpdatedCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ImageRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("test")
public class ImageServiceImplTest implements TestData {

    @MockBean
    private ImageRepository imageRepository;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ImageService imageService;

    private void setupDirectory() {
        String projectPath = System.getProperty("user.dir");

        File directory = new File(projectPath + "/resources/uploads/images/");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Critical Error: Could not create directory: " + projectPath + "/resources/uploads/images/");
            }
        }
    }

    @Test
    void uploadMultipleImages_givenValidInput_whenImageUpload_thenFilesExistInDirectoryAndDb() {

        this.setupDirectory();

        // Setup phase
        String projectPath = System.getProperty("user.dir");
        String uploadedImagesPath = projectPath + "/resources/uploads/images/";

        String[] originalImages = new String[] {
            projectPath + "/src/test/resources/images/1.png",
            projectPath + "/src/test/resources/images/2.png",
            projectPath + "/src/test/resources/images/3.png"
        };

        MultipartFile[] images = new MultipartFile[3];

        for (int i = 0; i < 3; i++) {
            try {
                images[i] = new MockMultipartFile("file", originalImages[i], "image/png", new FileInputStream(originalImages[i]));
            } catch (IOException e) {
                log.error("Error while creating multipart file");
                fail();
                return;
            }
        }

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        Long locationId = 1L;
        String user = LENDER_USER;

        // Original state of the directory
        List<String> currentlyExistingFiles = new ArrayList<>();
        Path uploadPathObject = Paths.get(uploadedImagesPath);

        try {
            Files.walk(uploadPathObject).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    currentlyExistingFiles.add(filePath.toString());
                }
            });
        } catch (IOException e) {
            log.error("Error while reading directory");
            fail();
            return;
        }

        // Response mocking
        when(locationRepository.findById(1L)).thenReturn(Optional.of(LOCATION_1));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).build()));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER2_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-2L).build()));
        when(imageRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Image.class);
            arg.setCreatedAt(LocalDateTime.now());
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        // Execution
        imageService.uploadMultipleImages(images, locationId, user);

        // Verification
        List<String> currentlyExistingFilesAfterExecution = new ArrayList<>();
        try {
            Files.walk(uploadPathObject).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    currentlyExistingFilesAfterExecution.add(filePath.toString());
                }
            });
        } catch (IOException e) {
            log.error("Error while reading directory");
            fail();
            return;
        }

        assertEquals(currentlyExistingFilesAfterExecution.size(), currentlyExistingFiles.size() + originalImages.length);
        List<String> fileDifference = new ArrayList<>(currentlyExistingFilesAfterExecution);
        fileDifference.removeAll(currentlyExistingFiles);

        boolean allFilesExist = true;

        for (String originalImage : originalImages) {
            boolean localFind = false;
            for (String newImage : fileDifference) {
                FileInputStream newImageStream;
                byte[] newImageBytes;

                FileInputStream originalImageStream;
                byte[] originalImageBytes;

                try {
                    newImageStream = new FileInputStream(originalImage);
                    newImageBytes = newImageStream.readAllBytes();
                    newImageStream.close();

                    originalImageStream = new FileInputStream(newImage);
                    originalImageBytes = originalImageStream.readAllBytes();
                    originalImageStream.close(); // Close to be able to delete later

                } catch (IOException e) {
                    fail();
                    return;
                }

                if (newImageBytes.length != originalImageBytes.length) {
                    continue;
                }
                boolean identicalContent = true;
                for (int i = 0; i < originalImageBytes.length; i++) {
                    if (newImageBytes[i] != originalImageBytes[i]) {
                        identicalContent = false;
                        break;
                    }
                }

                if (identicalContent) {
                    localFind = true;
                    break;
                }
            }

            if (!localFind) {
                allFilesExist = false;
                break;
            }
        }

        assertTrue(allFilesExist);

        // Clean up, directory should be back in original state
        for (String file : fileDifference) {
            File fileToDelete = new File(file);
            fileToDelete.delete();
        }
    }

    @Test
    void uploadMultipleImages_givenInvalidId_whenImageUpload_thenNotFoundException() {
        // Setup phase
        String projectPath = System.getProperty("user.dir");

        String[] originalImages = new String[] {
            projectPath + "/src/test/resources/images/1.png",
            projectPath + "/src/test/resources/images/2.png",
            projectPath + "/src/test/resources/images/3.png"
        };

        MultipartFile[] images = new MultipartFile[3];

        for (int i = 0; i < 3; i++) {
            try {
                images[i] = new MockMultipartFile("file", originalImages[i], "image/png", new FileInputStream(originalImages[i]));
            } catch (IOException e) {
                log.error("Error while creating multipart file");
                fail();
                return;
            }
        }

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        Long locationId = 2L;
        String user = LENDER_USER;

        // Response mocking
        when(locationRepository.findById(1L)).thenReturn(Optional.of(LOCATION_1));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).build()));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER2_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-2L).build()));
        when(imageRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Image.class);
            arg.setCreatedAt(LocalDateTime.now());
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        // Execution
        Throwable thrown = assertThrows(NotFoundException.class, () -> {
            imageService.uploadMultipleImages(images, locationId, user);
        });

        assertEquals("Location " + locationId + " existiert nicht.", thrown.getMessage());
    }

    @Test
    void uploadMultipleImages_givenValidInputButRequestNotFromOwner_whenImageUpload_thenValidationException() {
        // Setup phase
        String projectPath = System.getProperty("user.dir");

        String[] originalImages = new String[] {
            projectPath + "/src/test/resources/images/1.png",
            projectPath + "/src/test/resources/images/2.png",
            projectPath + "/src/test/resources/images/3.png"
        };

        MultipartFile[] images = new MultipartFile[3];

        for (int i = 0; i < 3; i++) {
            try {
                images[i] = new MockMultipartFile("file", originalImages[i], "image/png", new FileInputStream(originalImages[i]));
            } catch (IOException e) {
                log.error("Error while creating multipart file");
                fail();
                return;
            }
        }

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        Long locationId = 1L;
        String user = LENDER2_USER;

        // Response mocking
        when(locationRepository.findById(1L)).thenReturn(Optional.of(LOCATION_1));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).build()));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER2_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-2L).build()));
        when(imageRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Image.class);
            arg.setCreatedAt(LocalDateTime.now());
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        // Execution
        Throwable thrown = assertThrows(ValidationException.class, () -> {
            imageService.uploadMultipleImages(images, locationId, user);
        });

        assertEquals("User ist nicht der Besitzer der Location.", thrown.getMessage());
    }

    @Test
    void getAllImages_givenValidLocationIdAndUserIsOwner_whenRequested_thenImageCollectionDtoAndOwnerFlagTrue() {
        Long locationId = 1L;
        String user = LENDER_USER;

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        List<Image> images = new ArrayList<>();
        images.add(Image.builder()
            .id(1L)
            .url("test.jpg")
            .location(LOCATION_1)
            .build());

        images.add(Image.builder()
            .id(2L)
            .url("test2.jpg")
            .location(LOCATION_1)
            .build());

        images.add(Image.builder()
            .id(3L)
            .url("test3.jpg")
            .location(LOCATION_1)
            .build());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(LOCATION_1));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).build()));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER2_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-2L).build()));
        when(imageRepository.findByLocationId(locationId)).thenReturn(images);

        var result = imageService.getAllImages(locationId, user);

        assertAll(
            () -> assertEquals(1L, result.getLocationId()),
            () -> assertEquals(3, result.getImages().length),
            () -> assertEquals("test.jpg", result.getImages()[0]),
            () -> assertEquals("test2.jpg", result.getImages()[1]),
            () -> assertEquals("test3.jpg", result.getImages()[2]),
            () -> assertTrue(result.isCallerIsOwner())
        );
    }

    @Test
    void getAllImages_givenValidLocationIdAndUserIsNotOwner_whenRequested_thenImageCollectionDtoAndOwnerFlagFalse() {
        Long locationId = 1L;
        String user = LENDER2_USER;

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        List<Image> images = new ArrayList<>();
        images.add(Image.builder()
            .id(1L)
            .url("test.jpg")
            .location(LOCATION_1)
            .build());

        images.add(Image.builder()
            .id(2L)
            .url("test2.jpg")
            .location(LOCATION_1)
            .build());

        images.add(Image.builder()
            .id(3L)
            .url("test3.jpg")
            .location(LOCATION_1)
            .build());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(LOCATION_1));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).build()));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER2_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-2L).build()));
        when(imageRepository.findByLocationId(locationId)).thenReturn(images);

        var result = imageService.getAllImages(locationId, user);

        assertAll(
            () -> assertEquals(1L, result.getLocationId()),
            () -> assertEquals(3, result.getImages().length),
            () -> assertEquals("test.jpg", result.getImages()[0]),
            () -> assertEquals("test2.jpg", result.getImages()[1]),
            () -> assertEquals("test3.jpg", result.getImages()[2]),
            () -> assertFalse(result.isCallerIsOwner())
        );
    }

    @Test
    void getAllImages_givenInvalidLocationId_whenRequested_thenNotFoundException() {
        Long locationId = -1L;
        String user = LENDER_USER;

        assertThrows(NotFoundException.class, () -> imageService.getAllImages(locationId, user));
    }

    private String getNonUsedFilename() {
        String projectPath = System.getProperty("user.dir");
        String uploadsPath = projectPath + "/resources/uploads/images/";

        String filename = UUID.randomUUID() + ".png";
        boolean exists = true;

        while (exists) {
            File file = new File(uploadsPath + filename);
            if (file.exists()) {
                filename = UUID.randomUUID() + ".png";
            } else {
                exists = false;
            }
        }
        return filename;
    }

    @Test
    void getImage_givenValidFilename_whenRequested_thenInputStream() {

        this.setupDirectory();

        String projectPath = System.getProperty("user.dir");
        // Don't overwrite existing files
        String filename = this.getNonUsedFilename();
        String savedImage = projectPath + "/resources/uploads/images/" + filename;
        String originalImage = projectPath + "/src/test/resources/images/1.png";

        // Copy originalImage file to resourcePaths
        try {
            Files.copy(Paths.get(originalImage), Paths.get(savedImage), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fail();
            return;
        }

        // Get result from method
        var result = imageService.getImage(filename);
        byte[] resultBytes;

        try {
            resultBytes = result.readAllBytes();
            result.close();
        } catch (IOException e) {
            fail();
            return;
        }

        // Get awaited result
        FileInputStream awaited;
        byte[] awaitedBytes;
        try {
            awaited = new FileInputStream(originalImage);
            awaitedBytes = awaited.readAllBytes();
            awaited.close();
        } catch (IOException e) {
            fail();
            return;
        }

        // Compare length and content
        assertEquals(awaitedBytes.length, resultBytes.length);
        for (int i = 0; i < awaitedBytes.length; i++) {
            assertEquals(awaitedBytes[i], resultBytes[i]);
        }

        // Clean up
        File file = new File(savedImage);
        file.delete();
    }

    @Test
    void getImage_givenNonExistentFilename_whenRequested_thenNotFoundException() {
        String filename = this.getNonUsedFilename();

        Throwable thrown = assertThrows(NotFoundException.class, () -> imageService.getImage(filename));
        assertEquals("Could not find image with name " + filename, thrown.getMessage());

    }

    @Test
    void updateImages_givenValidData_whenUpdated_thenDeleteOldImagesAndAddNewToFiles() {

        this.setupDirectory();

        // Setup phase - existing images
        String projectPath = System.getProperty("user.dir");

        String[] originalImages = new String[] {
            projectPath + "/src/test/resources/images/1.png",
            projectPath + "/src/test/resources/images/2.png",
            projectPath + "/src/test/resources/images/3.png"
        };

        String[] copiedFilenames = new String[] {
            this.getNonUsedFilename(),
            this.getNonUsedFilename(),
            this.getNonUsedFilename()
        };

        String[] savedImages = new String[] {
            projectPath + "/resources/uploads/images/" + copiedFilenames[0],
            projectPath + "/resources/uploads/images/" + copiedFilenames[1],
            projectPath + "/resources/uploads/images/" + copiedFilenames[2]
        };

        String[] base64imagesPaths = new String[] {
            projectPath + "/src/test/resources/images/base64/1.txt",
            projectPath + "/src/test/resources/images/base64/4.txt"
        };

        String[] awaitedExistingImages = new String[] {
            projectPath + "/src/test/resources/images/1.png",
            projectPath + "/src/test/resources/images/4.png"
        };

        for (int i = 0; i < originalImages.length; i++) {
            try {
                Files.copy(Paths.get(originalImages[i]), Paths.get(savedImages[i]), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                fail();
                return;
            }
        }

        // Mock data + request data
        String uploadedImagesPath = projectPath + "/resources/uploads/images/";
        Long locationId = 1L;
        String user = LENDER_USER;

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        List<Image> existingImages = new ArrayList<>();
        existingImages.add(Image.builder()
            .id(1L)
            .url(copiedFilenames[0])
            .location(LOCATION_1)
            .build());

        existingImages.add(Image.builder()
            .id(2L)
            .url(copiedFilenames[1])
            .location(LOCATION_1)
            .build());

        existingImages.add(Image.builder()
            .id(3L)
            .url(copiedFilenames[2])
            .location(LOCATION_1)
            .build());

        // Get BASE64 encoded images for Image1 (position1) and Image4 - new (position0)
        // Read from file
        String image4Base64;
        try {
            image4Base64 = "data:image/png;base64," + Files.readString(Paths.get(base64imagesPaths[1]));
        } catch (IOException e) {
            fail();
            return;
        }

        ImageUpdatedCollectionDto requestContent = ImageUpdatedCollectionDto.builder()
            .locationId(locationId)
            .images(new String[] {
                image4Base64,
                copiedFilenames[0]
            })
            .build();

        // Original state of the directory
        List<String> currentlyExistingFiles = new ArrayList<>();
        Path uploadPathObject = Paths.get(uploadedImagesPath);

        try {
            Files.walk(uploadPathObject).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    currentlyExistingFiles.add(filePath.toString());
                }
            });
        } catch (IOException e) {
            fail();
            return;
        }

        // Response mocking
        when(locationRepository.findById(1L)).thenReturn(Optional.of(LOCATION_1));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).build()));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER2_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-2L).build()));
        when(imageRepository.findByLocationIdOrderByPositionAsc(1L)).thenReturn(existingImages);
        when(imageRepository.save(any())).thenAnswer(invocation -> {
            var arg = invocation.getArgument(0, Image.class);
            arg.setCreatedAt(LocalDateTime.now());
            arg.setUpdatedAt(LocalDateTime.now());
            return arg;
        });

        // Execution
        imageService.updateImages(requestContent, user);

        // Verification
        List<String> currentlyExistingFilesAfterExecution = new ArrayList<>();
        try {
            Files.walk(uploadPathObject).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    currentlyExistingFilesAfterExecution.add(filePath.toString());
                }
            });
        } catch (IOException e) {
            fail();
            return;
        }

        assertEquals(currentlyExistingFilesAfterExecution.size(), currentlyExistingFiles.size() - 1);
        List<String> createdFiles = new ArrayList<>();

        boolean allFilesExist = true;

        // Check if all files exist, that should exist after update
        for (String originalImage : awaitedExistingImages) {
            boolean localFind = false;
            for (String newImage : currentlyExistingFilesAfterExecution) {
                FileInputStream newImageStream;
                byte[] newImageBytes;

                FileInputStream originalImageStream;
                byte[] originalImageBytes;

                try {
                    newImageStream = new FileInputStream(originalImage);
                    newImageBytes = newImageStream.readAllBytes();
                    newImageStream.close();

                    originalImageStream = new FileInputStream(newImage);
                    originalImageBytes = originalImageStream.readAllBytes();
                    originalImageStream.close(); // Close to be able to delete later

                } catch (IOException e) {
                    fail();
                    return;
                }

                if (newImageBytes.length != originalImageBytes.length) {
                    continue;
                }

                boolean identicalContent = true;
                for (int i = 0; i < originalImageBytes.length; i++) {
                    if (newImageBytes[i] != originalImageBytes[i]) {
                        identicalContent = false;
                        break;
                    }
                }

                if (identicalContent) {
                    localFind = true;
                    createdFiles.add(newImage);
                    break;
                }
            }

            if (!localFind) {
                allFilesExist = false;
                break;
            }
        }

        assertTrue(allFilesExist);

        // Clean up, directory should be back in original state
        for (String file : createdFiles) {
            File fileToDelete = new File(file);
            fileToDelete.delete();
        }
    }

    @Test
    void updateImages_givenInvalidId_whenUpdated_thenNotFoundException() {
        // Mock data + request data
        String user = LENDER_USER;
        Long locationId = -1L;

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        ImageUpdatedCollectionDto requestContent = ImageUpdatedCollectionDto.builder()
            .locationId(locationId)
            .images(new String[] {
                "MOCK_BASE64_IMAGE_1",
                "MOCK_IMAGE_1_FILENAME"
            })
            .build();

        // Response mocking
        when(locationRepository.findById(1L)).thenReturn(Optional.of(LOCATION_1));

        // Execution
        Throwable thrown = assertThrows(NotFoundException.class, () -> {
            imageService.updateImages(requestContent, user);
        });

        assertEquals(thrown.getMessage(), "Location " + locationId + " existiert nicht.");
    }

    @Test
    void updateImages_givenValidDataButRequestNotFromOwner_whenUpdated_thenValidationException() {
        // Mock data + request data
        String user = LENDER2_USER;
        Long locationId = 1L;

        Location LOCATION_1 = Location.builder()
            .id(1L)
            .name("LOCATION_1_NAME")
            .description("LOCATION_1_DESCRIPTION")
            .plz(Plz.builder().plz("1111").build())
            .state(AustriaState.W)
            .address("LOCATION_1_STREET")
            .sizeInM2(new BigDecimal(1111))
            .owner(Lender.builder().id(-1L).build())
            .build();

        ImageUpdatedCollectionDto requestContent = ImageUpdatedCollectionDto.builder()
            .locationId(locationId)
            .images(new String[] {
                "MOCK_BASE64_IMAGE_1",
                "MOCK_IMAGE_1_FILENAME"
            })
            .build();

        // Response mocking
        when(locationRepository.findById(1L)).thenReturn(Optional.of(LOCATION_1));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-1L).build()));
        when(applicationUserRepository.findApplicationUserByEmail(LENDER2_USER)).thenReturn(Optional.of(ApplicationUser.builder().id(-2L).build()));

        // Execution
        Throwable thrown = assertThrows(ValidationException.class, () -> {
            imageService.updateImages(requestContent, user);
        });

        assertEquals("User ist nicht der Besitzer der Location.", thrown.getMessage());
    }
}
