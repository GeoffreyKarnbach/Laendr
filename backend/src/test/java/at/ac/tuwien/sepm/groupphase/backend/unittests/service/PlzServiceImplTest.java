package at.ac.tuwien.sepm.groupphase.backend.unittests.service;

import at.ac.tuwien.sepm.groupphase.backend.entity.Plz;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.PlzService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PlzServiceImplTest {

    @MockBean
    private PlzRepository plzRepository;

    @Autowired
    private PlzService plzService;

    @Test
    public void findPlzSuggestions_givenData_whenContainedString_thenMappedDtos() {
        when(plzRepository.findTop10ByPlzStartsWith(any())).thenReturn(List.of(Plz.builder().plz("1234").ort("TEST").build()));

        var results = plzService.findPlzSuggestions("1234");

        assertAll(
            () -> assertEquals(1, results.size()),
            () -> assertEquals("1234", results.get(0).getPlz())
        );
    }

}
