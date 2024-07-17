package at.ac.tuwien.sepm.groupphase.backend.unittests.repository;


import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ApplicationUserRepositoryTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Test
    @Sql("/sql/user/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    @Transactional
    void findApplicationUserByEmail_givenDataDoesntExists_whenUserExists_ThenFindUser() {
        Optional<ApplicationUser> result = applicationUserRepository.findApplicationUserByEmail("test");
        assertEquals(false, result.isPresent());
    }

    @Test
    @Sql("/sql/user/search_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void findApplicationUserByEmail_givenDataExists_whenUserExists_thenFindTestLender() {
        Optional<ApplicationUser> result = applicationUserRepository.findApplicationUserByEmail("lender@email.com");

        // Guaranteed order
        assertAll(
            () -> assertEquals(true, result.isPresent()),
            () -> assertEquals("test_lender_2", result.get().getName()),
            () -> assertEquals("lender@email.com", result.get().getEmail())
        );
    }

}
