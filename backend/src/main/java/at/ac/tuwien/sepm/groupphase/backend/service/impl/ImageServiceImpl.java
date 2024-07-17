package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.ImageCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ImageUpdatedCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ImageRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ImageService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.ImageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageValidator imageValidator;
    private final ImageRepository imageRepository;
    private final LocationRepository locationRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final String resourcePaths = System.getProperty("user.dir") + "/resources/uploads/images";

    public void uploadMultipleImages(MultipartFile[] images, Long locationId, String user) {

        imageValidator.validateLocationId(locationId);
        imageValidator.validateImageGroup(images, locationId, user);

        int imagePosition = 0;
        for (MultipartFile file : images) {
            imagePosition++;
            this.uploadSingleImage(file, locationId, imagePosition); //Upload all images individually
        }
    }

    @Override
    public ImageCollectionDto getAllImages(Long locationId, String user) {

        imageValidator.validateLocationId(locationId);

        List<String> imageUrls = new ArrayList<>();
        List<Image> images = imageRepository.findByLocationId(locationId);

        for (Image image : images) {
            imageUrls.add(image.getUrl());
        }

        Location location = locationRepository.findById(locationId).get();
        boolean callerIsOwner;

        Optional<ApplicationUser> applicationUser = applicationUserRepository.findApplicationUserByEmail(user);
        if (applicationUser.isPresent()) {
            callerIsOwner = location.getOwner().getId().equals(applicationUser.get().getId());
        } else {
            callerIsOwner = false;
        }

        return new ImageCollectionDto(locationId, imageUrls.toArray(new String[0]), callerIsOwner);
    }

    @Override
    public InputStream getImage(String fileName) {
        String path = this.resourcePaths + "/" + fileName;

        try {
            return new java.io.FileInputStream(path);
        } catch (IOException e) {
            throw new NotFoundException("Could not find image with name " + fileName);
        }
    }

    @Override
    public void updateImages(ImageUpdatedCollectionDto images, String user) {
        imageValidator.validateLocationId(images.getLocationId());
        imageValidator.validateUpdatedGroup(images.getLocationId(), user);

        List<Image> existingImages = imageRepository.findByLocationIdOrderByPositionAsc(images.getLocationId());
        List<Image> updatedImages = new ArrayList<>();

        List<String> filenames = new ArrayList<>();
        for (Image image : existingImages) {
            filenames.add(image.getUrl());
        }

        for (int i = 0; i < images.getImages().length; i++) {
            String filename;

            if (images.getImages()[i].startsWith("data:image")) {
                String uuid = java.util.UUID.randomUUID().toString();
                String extension = images.getImages()[i].split(";")[0].split("/")[1];
                filename = uuid + "." + extension;

                String completePath = this.resourcePaths + "/" + filename;
                // Transform base64 string to image file
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(images.getImages()[i].split(",")[1]);
                try {
                    java.nio.file.Files.write(java.nio.file.Paths.get(completePath), decodedBytes);
                } catch (IOException e) {
                    throw new RuntimeException("Error while writing image: " + e.getMessage());
                }
            } else {
                File f = new File(this.resourcePaths + "/" + images.getImages()[i]);
                if (!f.exists()) {
                    throw new RuntimeException("Image " + images.getImages()[i] + " does not exist!");
                }

                filename = images.getImages()[i];
            }

            if (i < existingImages.size()) {
                // Update existing image
                Image image = existingImages.get(i);
                image.setUrl(filename);
                updatedImages.add(image);

                imageRepository.save(image);
            } else {
                // Create new image
                Image image = new Image();
                image.setUrl(filename);
                image.setPosition(i + 1);
                image.setLocation(locationRepository.findById(images.getLocationId()).get());
                updatedImages.add(image);

                imageRepository.save(image);
            }
        }

        if (updatedImages.size() > 0) {
            Location location = locationRepository.findById(images.getLocationId()).get();
            location.setPrimaryImage(updatedImages.get(0));
            locationRepository.save(location);
        }

        if (updatedImages.size() == 0) {
            Location location = locationRepository.findById(images.getLocationId()).get();
            location.setPrimaryImage(null);
            locationRepository.save(location);
        }

        // Delete all images that are not in the new collection
        for (int i = images.getImages().length; i < existingImages.size(); i++) {
            imageRepository.delete(existingImages.get(i));
        }

        existingImages = new ArrayList<>(updatedImages);

        // Remove all files, that are not used anymore
        for (String filename : filenames) {
            boolean found = existingImages.stream().anyMatch(image -> image.getUrl().equals(filename));

            if (!found) {
                File f = new File(this.resourcePaths + "/" + filename);
                if (!f.delete()) {
                    throw new RuntimeException("Error while deleting image " + filename);
                }
            }
        }
    }

    private void uploadSingleImage(MultipartFile file, Long locationId, int imagePosition) {

        File directory = new File(this.resourcePaths);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Critical Error: Could not create directory " + this.resourcePaths);
            }
        }

        String uuid = java.util.UUID.randomUUID().toString();
        String filename = uuid + "." + (file.getOriginalFilename().split("\\."))[1];
        String completePath = this.resourcePaths + "/" + filename;

        Optional<Location> potentialLocation = locationRepository.findById(locationId);
        Location matchingLocation;

        if (potentialLocation.isPresent()) {
            matchingLocation = potentialLocation.get();
        } else {
            //Should be impossible to reach, as the transactionId is validated before
            throw new RuntimeException("Fatal error: Transaction with id " + locationId + " does not exist!");
        }

        Image imageEntity = new Image();
        imageEntity.setUrl(filename);
        imageEntity.setPosition(imagePosition);
        imageEntity.setLocation(matchingLocation);

        try {
            file.transferTo(new File(completePath));
            imageRepository.save(imageEntity);
            imageRepository.flush();

            matchingLocation.setPrimaryImage(imageEntity);
            locationRepository.save(matchingLocation);
        } catch (IOException e) {
            throw new RuntimeException("Error while uploading image: " + e.getMessage());
        }
    }
}
