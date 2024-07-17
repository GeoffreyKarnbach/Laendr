package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.ImageCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ImageUpdatedCollectionDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface ImageService {

    /**
     * Upload multiple images to the server (if the user is the owner of the location).
     *
     * @param images the image files as an array
     * @param locationId the id of the location the images belong to
     * @param user the user that uploaded the images
     */
    void uploadMultipleImages(MultipartFile[] images, Long locationId, String user);

    /**
     * Get all images belonging to a location and returns a flag to indicate if caller is owner.
     *
     * @param locationId the id of the location
     * @param user the user that requested the images
     * @return an ImageCollectionDto containing all image URLs and the location id and owner flag.
     */
    ImageCollectionDto getAllImages(Long locationId, String user);

    /**
     * Get an image from the server.
     *
     * @param fileName the id of the image
     * @return the image as a byte array
     */
    InputStream getImage(String fileName);

    /**
     * Updates the images of a location (if the user is the owner of the location).
     *
     * @param images the updated images
     * @param user the user that updated the images
     */
    void updateImages(ImageUpdatedCollectionDto images, String user);
}
