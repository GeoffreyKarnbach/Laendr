package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.mapper.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ReviewMapperTest {

    @Autowired
    private ReviewMapper reviewMapper;

    @Test
    public void creationDtoToRenterEntity_givenCreationDto_whenMapping_thenEntityWithValues() {
        var creationDto = ReviewCreationDto.builder().transactionId(1L).rating(3).comment("Comment").build();
        var entity = reviewMapper.creationDtoToLocationEntity(creationDto);

        assertAll(
            () -> assertEquals(creationDto.getRating(), entity.getRating()),
            () -> assertEquals(creationDto.getComment(), entity.getComment())
        );
    }

    @Test
    public void creationDtoToLocationEntity_givenCreationDto_whenMapping_thenEntityWithValues() {
        var creationDto = ReviewCreationDto.builder().transactionId(1L).rating(3).comment("Comment").build();
        var entity = reviewMapper.creationDtoToLocationEntity(creationDto);

        assertAll(
            () -> assertEquals(creationDto.getRating(), entity.getRating()),
            () -> assertEquals(creationDto.getComment(), entity.getComment())
        );
    }
}
