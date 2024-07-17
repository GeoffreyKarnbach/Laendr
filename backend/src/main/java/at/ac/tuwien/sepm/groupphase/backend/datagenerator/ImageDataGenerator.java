package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import at.ac.tuwien.sepm.groupphase.backend.repository.ImageRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

@Slf4j
public class ImageDataGenerator {

    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String IMAGE_CLASSPATH_SRC = "images/";
    private static final String IMAGE_UPLOAD_PATH = USER_DIR + "/resources/uploads/images/";

    private static boolean ensureDirectory() {
        var uploadDirectory = new File(IMAGE_UPLOAD_PATH);
        if (!uploadDirectory.exists()) {
            return uploadDirectory.mkdirs();
        }
        return true;
    }

    private static String createUploadedImage(String filename) {
        var targetFilename = java.util.UUID.randomUUID() + ".jpg";
        var targetFilepath = IMAGE_UPLOAD_PATH + targetFilename;

        try {
            var sourceFile = new ClassPathResource(IMAGE_CLASSPATH_SRC + filename).getFile();
            var targetFile = new File(targetFilepath);
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        } catch (IOException e) {
            log.warn("Error copying source image {}. Aborting", filename);
            return null;
        }

        return targetFilename;
    }

    public static void generateImages(
        Random random,
        List<LocationDataGenerator.TestLocation> locations,
        LocationRepository locationRepository,
        ImageRepository imageRepository
    ) {
        if (!ensureDirectory()) {
            log.warn("Image upload directory could not be created. Aborting image data generator");
        } else {
            for (var location : locations) {
                var imageSet = location.type().getImageSet();
                if (random.nextDouble() < 0.05 || imageSet == null || imageSet.length == 0) {
                    continue;
                }
                var filename = createUploadedImage(DatagenUtil.randomElement(random, imageSet));
                if (filename == null) {
                    return;
                }
                var locationEntity = location.locationEntity();
                var imageEntity = imageRepository.save(Image.builder()
                    .url(filename)
                    .position(0)
                    .location(locationEntity)
                    .build());
                locationEntity.setPrimaryImage(imageEntity);
                locationRepository.save(locationEntity);
            }
        }
    }

}
