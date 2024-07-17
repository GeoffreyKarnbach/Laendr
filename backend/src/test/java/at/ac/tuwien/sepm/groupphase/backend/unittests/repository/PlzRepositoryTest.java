package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
// This test slice annotation is used instead of @SpringBootTest to load only repository beans instead of
// the entire application context
@DataJpaTest
@ActiveProfiles("test")
public class PlzRepositoryTest implements TestData {

    @Autowired
    private PlzRepository plzRepository;

    @Test
    @Sql("/sql/plz.sql")
    public void findTop10ByPlzStartsWith_givenData_whenContainedString_thenResults() {
        var results = plzRepository.findTop10ByPlzStartsWith("311");

        assertAll(
            () -> assertEquals(1, results.size()),
            () -> assertEquals("3110", results.get(0).getPlz())
        );
    }

}
