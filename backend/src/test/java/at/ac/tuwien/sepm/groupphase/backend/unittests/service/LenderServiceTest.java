package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.dto.LenderViewDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.LenderMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LenderServiceTest {

    @MockBean
    private LenderRepository lenderRepository;

    @MockBean
    private LenderMapper lenderMapper;

    @Autowired
    private LenderService lenderService;

    @Test
    void searchById_givenData_whenFound_thenWrappedLenderViewDtos() {
        long id = 1;
        var lender = Lender.builder()
            .id(id)
            .email("email@email.com")
            .phone("0123456789")
            .owner(ApplicationUser.builder().id(id).build())
            .build();

        var lenderDto = LenderViewDto.builder()
            .id(id)
            .email("email@email.com")
            .phone("0123456789")
            .build();

        when(lenderRepository.findById(1L)).thenReturn(Optional.of(lender));
        when(lenderMapper.entityToDto(lender)).thenReturn(lenderDto);

        var ret = lenderService.getById(1);

        assertAll(
            () -> assertEquals(ret.getEmail(), lender.getEmail()),
            () -> assertEquals(ret.getPhone(), lender.getPhone()),
            () -> assertEquals(ret.getId(), lender.getId())
        );
    }

    @Test
    void searchById_givenData_thenNoneWithIdFound() {
        long id = 1;
        var lender = Lender.builder()
            .id(id)
            .build();

        when(lenderRepository.findById(2L)).thenReturn(Optional.empty());

        Throwable thrown = assertThrows(NotFoundException.class, () -> lenderService.getById(2L));
        assertEquals("Vermieter mit id 2 nicht gefunden", thrown.getMessage());
    }
}
