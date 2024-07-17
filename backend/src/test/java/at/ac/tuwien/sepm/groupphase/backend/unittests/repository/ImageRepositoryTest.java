package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ImageRepositoryTest implements TestData {

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findByLocationId_givenValidId_whenIdFound_thenImageList() {
        var result = imageRepository.findByLocationId(1L);

        List<String> imageNames = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            imageNames.add(result.get(i).getUrl());
        }

        // Order is not guaranteed
        assertAll(
            () -> assertEquals(3, result.size()),
            () -> assertTrue(imageNames.contains("test.jpg")),
            () -> assertTrue(imageNames.contains("test2.jpg")),
            () -> assertTrue(imageNames.contains("test3.jpg"))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findByLocationId_givenInvalidId_whenFound_thenEmptyImageList() {
        var result = imageRepository.findByLocationId(3L);
        assertEquals(0, result.size());
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findByLocationIdOrderByPositionAsc_givenValidId_whenFound_thenImageListSortedPositions() {
        var result = imageRepository.findByLocationIdOrderByPositionAsc(1L);

        // Guaranteed order
        assertAll(
            () -> assertEquals(3, result.size()),
            () -> assertEquals(1, result.get(0).getPosition()),
            () -> assertEquals(2, result.get(1).getPosition()),
            () -> assertEquals(3, result.get(2).getPosition()),
            () -> assertEquals("test.jpg", result.get(0).getUrl()),
            () -> assertEquals("test2.jpg", result.get(1).getUrl()),
            () -> assertEquals("test3.jpg", result.get(2).getUrl())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findByLocationIdOrderByPositionAsc_givenInvalidId_whenNotFound_thenEmptyImageList() {
        var result = imageRepository.findByLocationIdOrderByPositionAsc(3L);
        assertEquals(0, result.size());
    }
}
