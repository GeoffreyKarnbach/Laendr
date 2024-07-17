package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationSummary;
import at.ac.tuwien.sepm.groupphase.backend.mapper.ReputationSummaryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ReputationSummaryMapperTest {

    @Autowired
    private ReputationSummaryMapper reputationSummaryMapper;

    @Test
    public void entityToDto_givenSingleValue_whenReputationSummary_thenReturnMappedDto() {
        var entity = new ReputationSummaryImpl();
        var dto = reputationSummaryMapper.entityToDto(entity);
        assertAll(
            () -> assertEquals(entity.getSubjectId(), dto.getSubjectId()),
            () -> assertEquals(entity.getSubject(), dto.getSubject()),
            () -> assertEquals(entity.getKarma(), dto.getKarma()),
            () -> assertEquals(entity.getAverageRating(), dto.getAverageRating()),
            () -> assertEquals(entity.getRatings(), dto.getRatings())
        );
    }

    @Test
    public void entityToDto_givenList_whenReputationSummary_thenReturnMappedDto() {
        var entity = new ReputationSummaryImpl();
        var dto = reputationSummaryMapper.entityToDto(List.of(entity));
        assertAll(
            () -> assertEquals(entity.getSubjectId(), dto.get(0).getSubjectId()),
            () -> assertEquals(entity.getSubject(), dto.get(0).getSubject()),
            () -> assertEquals(entity.getKarma(), dto.get(0).getKarma()),
            () -> assertEquals(entity.getAverageRating(), dto.get(0).getAverageRating()),
            () -> assertEquals(entity.getRatings(), dto.get(0).getRatings())
        );
    }

    private class ReputationSummaryImpl implements ReputationSummary {

        public Long getSubjectId() {
            return 1L;
        }

        public String getSubject() {
            return "subject";
        }

        public BigDecimal getKarma() {
            return BigDecimal.ONE.divide(BigDecimal.ONE.add(BigDecimal.ONE));
        }

        public BigDecimal getAverageRating() {
            return BigDecimal.ONE.add(BigDecimal.ONE);
        }

        public int getRatings() {
            return 10;
        }

        @Override
        public LocalDateTime getLastChange() {
            return LocalDateTime.of(2020, 5, 1, 12, 34, 56);
        }
    }

}
