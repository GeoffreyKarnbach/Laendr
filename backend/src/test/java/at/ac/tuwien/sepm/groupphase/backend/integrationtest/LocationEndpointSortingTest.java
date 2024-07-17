package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static at.ac.tuwien.sepm.groupphase.backend.basetest.TestData.BASE_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LocationEndpointSortingTest {

    private static final String LOCATION_BASE_URI = BASE_URI + "/locations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql("/sql/reputation/sort_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void searchByName_givenData_whenSortingByReputation_thenResultIsOrderedByReputation() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/search")
                .queryParam("q", "")
                .queryParam("page", "0")
                .queryParam("pageSize", "3")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<LocationDto> locationDtos = ((List) result.getResult()).stream().map(item -> objectMapper.convertValue(item, LocationDto.class)).toList();

        assertAll(
            () -> assertEquals(-1, locationDtos.get(0).getId()),
            () -> assertEquals(-2, locationDtos.get(1).getId()),
            () -> assertEquals(-3, locationDtos.get(2).getId())
        );
    }

    @Test
    @Sql("/sql/reputation/sort_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    void searchByLender_givenData_whenSortingByReputation_thenResultIsOrderedByReputation() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/search/lender")
                .queryParam("id", "-1")
                .queryParam("includeRemovedLocations", "false")
                .queryParam("page", "0")
                .queryParam("pageSize", "3")
                .queryParam("sort", "RECOMMENDED_DESC")
            )
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        var result = objectMapper.readValue(response.getContentAsString(), PageableDto.class);
        List<LocationDto> locationDtos = ((List) result.getResult()).stream().map(item -> objectMapper.convertValue(item, LocationDto.class)).toList();

        assertAll(
            () -> assertEquals(-1, locationDtos.get(0).getId()),
            () -> assertEquals(-2, locationDtos.get(1).getId()),
            () -> assertEquals(-3, locationDtos.get(2).getId())
        );
    }

}
