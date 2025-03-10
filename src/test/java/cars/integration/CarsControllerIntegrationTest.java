package cars.integration;

import cars.configuration.TestConfig;
import com.example.cars.CarsApplication;
import com.example.cars.model.requests.CarRequest;
import com.example.cars.persistence.Car;
import com.example.cars.persistence.CarRepository;
import com.example.cars.persistence.Engine;
import com.example.cars.persistence.EngineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestConfig.class)
@SpringBootTest(classes = CarsApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CarsControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private EngineRepository engineRepository;

    private Engine testEngine;
    private Car testCar;

    @BeforeEach
    void setUp() {
        // Create test engine and car
        testEngine = new Engine();
        testEngine.setHorsePower(300);
        testEngine.setCapacity(3.0);
        engineRepository.save(testEngine);

        testCar = new Car();
        testCar.setModel("TestCarModel");
        testCar.setYear(2024);
        testCar.setDriveable(true);
        testCar.setPriceInCents(8000L);
        testCar.setSalesCount(0L);
        testCar.setEngine(testEngine);
        testCar.setImageUrl("https://test-bucket.s3.eu-north-1.amazonaws.com/images/existing-image.jpg");
        carRepository.save(testCar);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetAllCars() throws Exception {
        mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("pageSize", "10")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].model", is(testCar.getModel())));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetCarById() throws Exception {
        mockMvc.perform(get("/cars/{id}", testCar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCar.getId().intValue())))
                .andExpect(jsonPath("$.model", is(testCar.getModel())))
                .andExpect(jsonPath("$.year", is(testCar.getYear())))
                .andExpect(jsonPath("$.driveable", is(testCar.isDriveable())))
                .andExpect(jsonPath("$.priceInCents", is(testCar.getPriceInCents().intValue())))
                .andExpect(jsonPath("$.engine.id", is(testEngine.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetCarByIdWithNonExistentCar() throws Exception {
        mockMvc.perform(get("/cars/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("not-found")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAddCarWithValidRequest() throws Exception {
        CarRequest request = new CarRequest("New Car", 2022, true, 2000000L, testEngine.getId());

        mockMvc.perform(post("/cars/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.model", is(request.getModel())))
                .andExpect(jsonPath("$.year", is(request.getYear())))
                .andExpect(jsonPath("$.driveable", is(request.isDriveable())))
                .andExpect(jsonPath("$.priceInCents", is(request.getPriceInCents().intValue())))
                .andExpect(jsonPath("$.engine.id", is(testEngine.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAddCarWithInvalidRequest() throws Exception {
        CarRequest invalidRequest = new CarRequest("", 1930, true, -100L, 0L);

        mockMvc.perform(post("/cars/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("invalid-request")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAddCarWithImage() throws Exception {
        CarRequest request = new CarRequest("New Car With Image", 2022, true, 2000000L, testEngine.getId());
        String requestJson = objectMapper.writeValueAsString(request);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart("/cars/add/withImage")
                        .file(imageFile)
                        .param("request", requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateCarPrice() throws Exception {
        Long newPrice = 1250000L;

        mockMvc.perform(patch("/cars/{id}/updatePriceInCents", testCar.getId())
                        .param("newPrice", newPrice.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCar.getId().intValue())))
                .andExpect(jsonPath("$.model", is(testCar.getModel())))
                .andExpect(jsonPath("$.priceInCents", is(newPrice.intValue())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateCarImage() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "updated-image.jpg", "image/jpeg", "updated image content".getBytes());

        mockMvc.perform(multipart("/cars/{id}/updateImage", testCar.getId())
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCar.getId().intValue())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteCar() throws Exception {
        mockMvc.perform(delete("/cars/{id}", testCar.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/cars/{id}", testCar.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testAdminEndpointsWithRegularUser() throws Exception {
        CarRequest request = new CarRequest("New Car", 2022, true, 2000000L, testEngine.getId());

        mockMvc.perform(post("/cars/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/cars/{id}", testCar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/cars/{id}", testCar.getId()))
                .andExpect(status().isForbidden());
    }
}
