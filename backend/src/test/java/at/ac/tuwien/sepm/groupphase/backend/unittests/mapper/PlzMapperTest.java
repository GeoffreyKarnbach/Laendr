package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import at.ac.tuwien.sepm.groupphase.backend.mapper.PlzMapper;
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
public class PlzMapperTest {

    @Autowired
    private PlzMapper plzMapper;

    @Test
    public void entityToDto_givenNothing_whenEntity_thenDto() {
        var entity = Plz.builder().plz("1234").ort("TEST").build();

        var result = plzMapper.entityToDto(entity);

        assertAll(
            () -> assertEquals(entity.getPlz(), result.getPlz()),
            () -> assertEquals(entity.getOrt(), result.getOrt())
        );
    }

}
