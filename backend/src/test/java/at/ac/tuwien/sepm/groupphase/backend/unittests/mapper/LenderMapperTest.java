package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import at.ac.tuwien.sepm.groupphase.backend.mapper.LenderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LenderMapperTest {

    @Autowired
    private LenderMapper lenderMapper;

    @Test
    public void entityToDto_given_whenLenderEntity_thenMappedDto() {
        var entity = Lender.builder()
            .id(1L)
            .description("description")
            .phone("0123456789")
            .createdAt(LocalDateTime.of(2000, 1, 1, 1, 0))
            .updatedAt(LocalDateTime.of(2000, 1, 1, 1, 0))
            .email("email@email.com")
            .owner(ApplicationUser.builder()
                .name("name")
                .state(AustriaState.W)
                .plz(Plz.builder().plz("1010").build())
                .build())
            .build();

        var dto = lenderMapper.entityToDto(entity);

        assertAll(
            () -> assertEquals(entity.getId(), dto.getId()),
            () -> assertEquals(entity.getDescription(), dto.getDescription()),
            () -> assertEquals(entity.getPhone(), dto.getPhone()),
            () -> assertEquals(entity.getCreatedAt(), dto.getCreatedAt()),
            () -> assertEquals(entity.getEmail(), dto.getEmail()),
            () -> assertEquals(entity.getOwner().getName(), dto.getName()),
            () -> assertEquals(entity.getOwner().getState(), dto.getState()),
            () -> assertEquals(entity.getOwner().getPlz().getPlz(), dto.getPlz().getPlz())
        );
    }

}
