package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.repository.LocationTagRepository;
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
public class TagDataGenerator {

    private final LocationTagRepository locationTagRepository;

    private final DataSource dataSource;

    @PostConstruct
    public void generateData() throws SQLException {
        log.info("Generating data…");
        if (locationTagRepository.count() == 0) {
            try (var connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/tags.sql"));
                log.info("Finished generating data without error.");
            }
        }
    }
}
