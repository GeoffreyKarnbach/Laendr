package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.SignUpDto;
import at.ac.tuwien.sepm.groupphase.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void dtoToEntity_given_whenSignUpDto_thenMappedEntity() {
        var dto = SignUpDto.builder()
            .username("name")
            .email("admin@email.at")
            .originalPassword("pasw")
            .repeatedPassword("pasw")
            .build();

        var entity = userMapper.dtoToEntity(dto);

        assertAll(
            () -> assertEquals(dto.getUsername(), entity.getName()),
            () -> assertEquals(dto.getEmail(), entity.getEmail()),
            () -> assertEquals(dto.getOriginalPassword(), entity.getPassword())
        );
    }
}
