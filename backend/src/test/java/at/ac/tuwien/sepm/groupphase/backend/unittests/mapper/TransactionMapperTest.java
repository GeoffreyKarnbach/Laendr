package at.ac.tuwien.sepm.groupphase.backend.unittests.mapper;

import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TransactionMapperTest {

    @Autowired
    private TransactionMapper transactionMapper;

    @Test
    public void entityToDto_given_whenLenderEntity_thenMappedDto() {
        var entity = Transaction.builder()
            .id(1L)
            .initialMessage("initialMessage")
            .createdAt(LocalDateTime.now())
            .timeslot(Timeslot.builder()
                .id(0L)
                .price(BigDecimal.TEN)
                .owningLocation(Location.builder()
                    .owner(Lender.builder()
                        .id(2L)
                        .build())
                    .name("locationName")
                    .id(5L)
                    .removed(false)
                    .build())
                .end(LocalDateTime.now())
                .start(LocalDateTime.now())
                .build())
            .build();

        var dto = transactionMapper.entityToDto(entity);

        assertAll(
            () -> assertEquals(entity.getId(), dto.getId()),
            () -> assertEquals(entity.getCreatedAt(), dto.getCreatedAt()),
            () -> assertEquals(entity.getTimeslot().getId(), dto.getTimeslot().getId()),
            () -> assertEquals(entity.getTimeslot().getEnd(), dto.getTimeslot().getEnd()),
            () -> assertEquals(entity.getTimeslot().getStart(), dto.getTimeslot().getStart()),
            () -> assertEquals(entity.getInitialMessage(), dto.getInitialMessage()),
            () -> assertEquals(entity.getTimeslot().getOwningLocation().getOwner().getId(), dto.getLenderId()),
            () -> assertEquals(entity.getTimeslot().getOwningLocation().getName(), dto.getLocationName()),
            () -> assertEquals(entity.getTimeslot().getOwningLocation().getId(), dto.getLocationId()),
            () -> assertEquals(entity.getTimeslot().getOwningLocation().isRemoved(), dto.getLocationRemoved()),
            () -> assertEquals(entity.getTimeslot().getPrice(), dto.getTotalConcerned())
        );
    }

}
