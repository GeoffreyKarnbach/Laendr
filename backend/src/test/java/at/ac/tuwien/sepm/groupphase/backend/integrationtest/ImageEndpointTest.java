package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.dto.ImageCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ImageUpdatedCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ImageEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    private static final String LOCATION_BASE_URI = BASE_URI + "/images";
    private static final String projectPath = System.getProperty("user.dir");

    private void setup_test() {

        String fromPath = projectPath + "/src/test/resources/images/";
        String toPath = projectPath + "/resources/uploads/images/";

        String[] filenames = new String[] { "1.png", "2.png", "3.png", "4.png", "5.jpg" };

        File directory = new File(toPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (int i = 0; i < filenames.length; i++) {
            try {
                Files.copy(Paths.get(fromPath + filenames[i]), Paths.get(toPath + filenames[i]), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                fail();
                return;
            }
        }
    }

    private void clean_up_test() {

        String savedPath = projectPath + "/resources/uploads/images/";

        String[] filenames = new String[] { "1.png", "2.png", "3.png", "4.png", "5.jpg" };

        for (String filename: filenames) {
            File fileToDelete = new File(savedPath + filename);
            fileToDelete.delete();
        }
    }

    private void clean_up_test_values(List<String> filenames) {

        for (String filename: filenames) {
            File fileToDelete = new File(filename);
            fileToDelete.delete();
        }
    }

    private MultipartFile[] setup_multipart_data() {

        String fromPath = projectPath + "/src/test/resources/images/";
        String[] filenames = new String[] { "1.png", "2.png", "3.png" };

        MultipartFile[] request = new MultipartFile[3];

        for (int i = 0; i < filenames.length; i++) {
            try {
                request[i] = new MockMultipartFile("images", filenames[i], "image/png", Files.readAllBytes(Paths.get(fromPath + filenames[i])));
            } catch (IOException e) {
                fail();
                return null;
            }
        }

        return request;
    }

    private List<String> getOriginalResourcesContent() {

        List<String> currentlyExistingFiles = new ArrayList<>();

        try {
            Files.walk(Paths.get(projectPath + "/resources/uploads/images/")).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    currentlyExistingFiles.add(filePath.toString());
                }
            });
        } catch (IOException e) {
            fail();
        }

        return currentlyExistingFiles;
    }

    private void checkIfSavedFilesAreCorrectAndCorrectContent(List<String> filenames) {

        String fromPath = projectPath + "/src/test/resources/images/";
        String[] originalImages = new String[] { "1.png", "2.png", "3.png" };

        List<String> originalImagesBase64 = new ArrayList<>();
        List<String> savedImagesBase64 = new ArrayList<>();

        for (int i = 0; i < originalImages.length; i++) {
            try {
                originalImagesBase64.add(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(fromPath + originalImages[i]))));
                savedImagesBase64.add(Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(filenames.get(i)))));
            } catch (IOException e) {
                fail();
            }
        }

        for (String image: savedImagesBase64) {
            assertTrue(originalImagesBase64.contains(image));
        }
    }

    private String getBase64Content(int fileNumber) {

        String filename = projectPath + "/src/test/resources/images/base64/" + fileNumber + ".txt";

        try {
            return "data:image/png;base64," + new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            fail();
            return null;
        }
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void uploadImages_givenData_whenValidDataAndId_thenStatus200AndImagesInResourcesDirectory() throws Exception {
        List<String> originalResourcesContent = this.getOriginalResourcesContent(); // Restore original state

        Long locationId = 1L;
        MultipartFile[] request = this.setup_multipart_data();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(LOCATION_BASE_URI)
                .file((MockMultipartFile) request[0])
                .file((MockMultipartFile) request[1])
                .file((MockMultipartFile) request[2])
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType("multipart/form-data")
                .param("images", request.toString())
                .param("locationId", locationId.toString()))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(201, response.getStatus());

        List<String> currentlyExistingFiles = this.getOriginalResourcesContent();
        assertEquals(originalResourcesContent.size() + 3, currentlyExistingFiles.size());

        List<String> newFiles = new ArrayList<>(currentlyExistingFiles);
        newFiles.removeAll(originalResourcesContent);

        this.checkIfSavedFilesAreCorrectAndCorrectContent(newFiles);
        this.clean_up_test_values(newFiles);
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void uploadImages_givenData_whenValidDataAndInvalidId_thenStatus404() throws Exception {

        Long locationId = -1L;
        MultipartFile[] request = this.setup_multipart_data();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(LOCATION_BASE_URI)
                .file((MockMultipartFile) request[0])
                .file((MockMultipartFile) request[1])
                .file((MockMultipartFile) request[2])
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType("multipart/form-data")
                .param("images", request.toString())
                .param("locationId", locationId.toString()))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(404, response.getStatus());
        assertTrue(response.getContentAsString().contains("Location " + locationId + " existiert nicht."));
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void uploadImages_givenData_whenValidButNotFromOwner_thenStatus422() throws Exception {

        Long locationId = 1L;
        MultipartFile[] request = this.setup_multipart_data();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(LOCATION_BASE_URI)
                .file((MockMultipartFile) request[0])
                .file((MockMultipartFile) request[1])
                .file((MockMultipartFile) request[2])
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER2_USER, LENDER2_ROLES))
                .contentType("multipart/form-data")
                .param("images", request.toString())
                .param("locationId", locationId.toString()))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ValidationErrorDto.class);

        assertEquals(422, response.getStatus());
        assertEquals(result.getMessage(), "User ist nicht der Besitzer der Location.");
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateImages_givenData_whenValidData_thenStatus200AndImagesInResourcesDirectory() throws Exception {

        this.setup_test();

        List<String> originalResourcesContent = this.getOriginalResourcesContent(); // Restore original state

        Long locationId = 2L;

        ImageUpdatedCollectionDto requestBody = ImageUpdatedCollectionDto.builder()
            .locationId(locationId)
            .images(new String[] { "5.jpg", this.getBase64Content(1) })
            .build();
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(204, response.getStatus());

        mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/all/" + locationId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        response = mvcResult.getResponse();
        var result = objectMapper.readValue(response.getContentAsString(), ImageCollectionDto.class);

        List<String> additionalFiles = this.getOriginalResourcesContent();
        additionalFiles.removeAll(originalResourcesContent);

        // Verify that the additional file is the one that was uploaded and is associated to the location
        String additionalFilename = additionalFiles.get(0).substring(additionalFiles.get(0).lastIndexOf('\\') + 1);

        // For linux after / and not after \\
        if (additionalFilename.length() == additionalFiles.get(0).length()) {
            additionalFilename = additionalFiles.get(0).substring(additionalFiles.get(0).lastIndexOf('/') + 1);
        }

        List<String> filenames = Arrays.asList(result.getImages());

        String finalAdditionalFilename = additionalFilename;
        assertAll(
            () -> assertEquals(2, filenames.size()),
            () -> assertTrue(filenames.contains("5.jpg")),
            () -> assertTrue(filenames.contains(finalAdditionalFilename))
        );

        // Clean up
        this.clean_up_test();

        this.clean_up_test_values(additionalFiles);
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateImages_givenData_whenInvalidId_thenStatus422() throws Exception {

        Long locationId = -1L;

        ImageUpdatedCollectionDto requestBody = ImageUpdatedCollectionDto.builder()
            .locationId(locationId)
            .images(new String[] { "5.jpg", this.getBase64Content(1) })
            .build();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(404, response.getStatus()),
            () -> assertTrue(response.getContentAsString().contains("Location " + locationId + " existiert nicht."))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateImages_givenData_whenValidDataNotFromOwner_thenStatus422() throws Exception {

        Long locationId = 2L;

        ImageUpdatedCollectionDto requestBody = ImageUpdatedCollectionDto.builder()
            .locationId(locationId)
            .images(new String[] { "5.jpg", this.getBase64Content(1) })
            .build();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put(LOCATION_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER2_USER, LENDER2_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(422, response.getStatus());
        assertTrue(response.getContentAsString().contains("User ist nicht der Besitzer der Location."));
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getImages_givenId_whenValidAndRequestFromOwner_thenImageCollectionDtoWithOwnerFlagTrue() throws Exception {
        Long locationId = 1L;

        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/all/" + locationId)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        var result = objectMapper.readValue(response.getContentAsString(), ImageCollectionDto.class);

        List<String> filenames = Arrays.asList(result.getImages());
        assertAll(
            () -> assertEquals(200, response.getStatus()),
            () -> assertEquals(3, result.getImages().length),
            () -> assertTrue(filenames.contains("test3.jpg")),
            () -> assertTrue(filenames.contains("test2.jpg")),
            () -> assertTrue(filenames.contains("test.jpg")),
            () -> assertTrue(result.isCallerIsOwner())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getImages_givenId_whenValidAndRequestNotFromOwner_thenImageCollectionDtoWithOwnerFlagFalse() throws Exception {
        Long locationId = 1L;

        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/all/" + locationId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER2_USER, LENDER2_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        var result = objectMapper.readValue(response.getContentAsString(), ImageCollectionDto.class);

        List<String> filenames = Arrays.asList(result.getImages());
        assertAll(
            () -> assertEquals(200, response.getStatus()),
            () -> assertEquals(3, result.getImages().length),
            () -> assertTrue(filenames.contains("test3.jpg")),
            () -> assertTrue(filenames.contains("test2.jpg")),
            () -> assertTrue(filenames.contains("test.jpg")),
            () -> assertFalse(result.isCallerIsOwner())
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getImages_givenId_whenInvalidId_thenNotFoundException() throws Exception {
        Long locationId = -100L;

        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/all/" + locationId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(LENDER_USER, LENDER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(404, response.getStatus()),
            () -> assertTrue(response.getContentAsString().contains("Location " + locationId + " existiert nicht."))
        );
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getImage_givenValidFilename_whenFilenameIsPng_thenInputStreamWithPngMediaType() throws Exception {
        this.setup_test();

        String filename = "4.png";
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/" + filename))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(200, response.getStatus()),
            () -> assertEquals("image/png;charset=UTF-8", response.getContentType())
        );

        // Check if the image is the same as the one in the base64 file
        String image_4_base_64;
        String image_4_path = System.getProperty("user.dir") + "/src/test/resources/images/base64/4.txt";

        try {
            image_4_base_64 = Files.readString(Paths.get(image_4_path));
        } catch (IOException e) {
            fail();
            return;
        }

        byte[] image_4_bytes = Base64.getDecoder().decode(image_4_base_64);
        assertEquals(image_4_bytes.length, response.getContentAsByteArray().length);

        for (int i = 0; i < image_4_bytes.length; i++) {
            assertEquals(image_4_bytes[i], response.getContentAsByteArray()[i]);
        }

        this.clean_up_test();
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getImage_givenValidFilename_whenFilenameIsJpg_thenInputStreamWithJpgMediaType() throws Exception {
        this.setup_test();

        String filename = "5.jpg";
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/" + filename))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(200, response.getStatus()),
            () -> assertEquals("image/jpeg;charset=UTF-8", response.getContentType())
        );

        // Check if the image is the same as the one in the base64 file
        String image_5_base_64;
        String image_5_path = System.getProperty("user.dir") + "/src/test/resources/images/base64/5.txt";

        try {
            image_5_base_64 = Files.readString(Paths.get(image_5_path));
        } catch (IOException e) {
            fail();
            return;
        }

        byte[] image_5_bytes = Base64.getDecoder().decode(image_5_base_64);
        assertEquals(image_5_bytes.length, response.getContentAsByteArray().length);

        for (int i = 0; i < image_5_bytes.length; i++) {
            assertEquals(image_5_bytes[i], response.getContentAsByteArray()[i]);
        }

        this.clean_up_test();
    }

    @Test
    @Transactional
    @Sql("/sql/location/generic_data.sql")
    @Sql("/sql/image/image_data.sql")
    @Sql(scripts = "/sql/clean_up.sql", executionPhase = AFTER_TEST_METHOD)
    public void getImage_givenInvalidFilename_whenRequested_then404Response() throws Exception {
        this.setup_test();

        String filename = "not_existing.jpg";
        MvcResult mvcResult = this.mockMvc.perform(get(LOCATION_BASE_URI + "/" + filename))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(404, response.getStatus()),
            () -> assertTrue(response.getContentAsString().contains("Could not find image with name " + filename))
        );

        this.clean_up_test();
    }

}
