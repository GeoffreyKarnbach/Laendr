package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
@Profile("generateData")
@Slf4j
@AllArgsConstructor
public class PlzDataGenerator {

    private final PlzRepository plzRepository;

    private final DataSource dataSource;

    @PostConstruct
    public void generateData() throws SQLException {
        log.info("Generating data…");
        if (plzRepository.count() == 0) {
            try (var connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/plz.sql"));
                log.info("Finished generating data without error.");
            }
        }
    }
}
