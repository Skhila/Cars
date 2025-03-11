package cars.integration;

import com.example.cars.CarsApplication;
import com.example.cars.error.NotFoundException;
import com.example.cars.persistence.Car;
import com.example.cars.persistence.CarRepository;
import com.example.cars.persistence.Engine;
import com.example.cars.persistence.EngineRepository;
import com.example.cars.user.model.UserRequest;
import com.example.cars.user.persistence.AppUser;
import com.example.cars.user.persistence.AppUserRepository;
import com.example.cars.user.persistence.Role;
import com.example.cars.user.persistence.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CarsApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private EngineRepository engineRepository;

    private Role userRole;
    private Role adminRole;
    private AppUser testUser;
    private AppUser testAdmin;
    private Engine testEngine;
    private Car testCar;
    private Car expensiveCar;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");
        userRole = roleRepository.saveAndFlush(userRole);

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
        adminRole = roleRepository.saveAndFlush(adminRole);

        testUser = new AppUser();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setBalanceInCents(10000L);
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        testUser.setRoles(userRoles);
        testUser = userRepository.saveAndFlush(testUser);

        testAdmin = new AppUser();
        testAdmin.setUsername("testadmin");
        testAdmin.setPassword("admin123");
        testAdmin.setBalanceInCents(20000L);
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        testAdmin.setRoles(adminRoles);
        testAdmin = userRepository.saveAndFlush(testAdmin);

        testEngine = new Engine();
        testEngine.setHorsePower(300);
        testEngine.setCapacity(2.5);
        testEngine = engineRepository.saveAndFlush(testEngine);

        testCar = new Car();
        testCar.setModel("Test Model");
        testCar.setYear(2023);
        testCar.setDriveable(true);
        testCar.setPriceInCents(5000L);
        testCar.setSalesCount(0L);
        testCar.setEngine(testEngine);
        testCar = carRepository.saveAndFlush(testCar);

        expensiveCar = new Car();
        expensiveCar.setModel("Luxury Model");
        expensiveCar.setYear(2024);
        expensiveCar.setDriveable(true);
        expensiveCar.setPriceInCents(15000L);
        expensiveCar.setSalesCount(0L);
        expensiveCar.setEngine(testEngine);
        expensiveCar = carRepository.saveAndFlush(expensiveCar);
    }

    @AfterEach
    void tearDown() {
        userRepository.findAll().forEach(user -> user.getCars().clear());
        userRepository.findAll().forEach(user -> user.getRoles().clear());
        userRepository.saveAllAndFlush(userRepository.findAll());

        carRepository.deleteAll();
        engineRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    void testCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("newuser");
        userRequest.setPassword("password123");
        userRequest.setBalanceInCents(15000L);
        userRequest.setRoleIds(Set.of(userRole.getId()));

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());

        AppUser createdUser = userRepository.findByUsername("newuser").orElse(null);
        assertNotNull(createdUser);
        assertEquals("newuser", createdUser.getUsername());
        assertEquals(15000L, createdUser.getBalanceInCents());
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    void testGetAllUsersInfo() throws Exception {
        mockMvc.perform(get("/users/info")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].username", containsInAnyOrder("testuser", "testadmin")));
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    void testGetUserInfoById() throws Exception {
        mockMvc.perform(get("/users/info/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.balanceInCents", is(10000)));
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    void testGetUserInfoByIdWithNonExistentUser() throws Exception {
        mockMvc.perform(get("/users/info/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("not found")));
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    void testUpdateBalance() throws Exception {
        mockMvc.perform(patch("/users/{userId}/addFundsToBalance", testUser.getId())
                        .with(csrf())
                        .param("amountInCents", "5000"))
                .andExpect(status().isOk());

        AppUser updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(15000L, updatedUser.getBalanceInCents());
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    void testUpdateBalanceWithInsufficientFunds() throws Exception {
        mockMvc.perform(patch("/users/{userId}/addFundsToBalance", testUser.getId())
                        .with(csrf())
                        .param("amountInCents", "-20000"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage", containsString("Insufficient funds")));

        AppUser updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(10000L, updatedUser.getBalanceInCents());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCurrentUserCars() throws Exception {
        testUser.getCars().add(testCar);
        userRepository.saveAndFlush(testUser);

        mockMvc.perform(get("/users/myCars")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(testCar.getId().intValue())))
                .andExpect(jsonPath("$.content[0].model", is(testCar.getModel())));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testPurchaseCar() throws Exception {
        AppUser user = userRepository.findById(testUser.getId()).orElseThrow(() -> new NotFoundException("user not found"));
        Long initialBalanceInCents = user.getBalanceInCents();
        assertEquals(0, user.getCars().size());

        mockMvc.perform(post("/users/purchase")
                        .with(csrf())
                        .param("carId", testCar.getId().toString()))
                .andExpect(status().isOk());

        assertEquals(1, user.getCars().size());
        assertTrue(user.getCars().contains(testCar));

        assertEquals(initialBalanceInCents - testCar.getPriceInCents(), user.getBalanceInCents());

        Car carAfter = carRepository.findById(testCar.getId()).orElseThrow(() -> new NotFoundException("car not found"));
        assertEquals(1L, carAfter.getSalesCount());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testPurchaseCarWithInsufficientFunds() throws Exception {
        mockMvc.perform(post("/users/purchase")
                        .with(csrf())
                        .param("carId", expensiveCar.getId().toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage", containsString("Insufficient funds")));

        AppUser userAfter = userRepository.findById(testUser.getId()).orElseThrow(() -> new NotFoundException("user not found"));
        assertEquals(0, userAfter.getCars().size());

        assertEquals(testUser.getBalanceInCents(), userAfter.getBalanceInCents());

        Car carAfter = carRepository.findById(expensiveCar.getId()).orElseThrow(() -> new NotFoundException("car not found"));
        assertEquals(0L, carAfter.getSalesCount());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testPurchaseCarWithNonExistentCar() throws Exception {
        mockMvc.perform(post("/users/purchase")
                        .with(csrf())
                        .param("carId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Car with id 999 not found")));

        AppUser userAfter = userRepository.findById(testUser.getId()).orElseThrow(() -> new NotFoundException("user not found"));
        assertEquals(0, userAfter.getCars().size());
        assertEquals(testUser.getBalanceInCents(), userAfter.getBalanceInCents());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testSellCar() throws Exception {
        testUser.getCars().add(testCar);
        userRepository.saveAndFlush(testUser);

        AppUser userBefore = userRepository.findById(testUser.getId()).orElseThrow(() -> new NotFoundException("user not found"));
        assertEquals(1, userBefore.getCars().size());
        long initialBalance = userBefore.getBalanceInCents();

        long expectedAmount = Math.round(testCar.getPriceInCents() * 0.8);

        mockMvc.perform(post("/users/sell")
                        .with(csrf())
                        .param("carId", testCar.getId().toString()))
                .andExpect(status().isOk());

        AppUser userAfter = userRepository.findById(testUser.getId()).orElseThrow(() -> new NotFoundException("user not found"));
        assertEquals(0, userAfter.getCars().size());

        assertEquals(initialBalance + expectedAmount, userAfter.getBalanceInCents());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testSellCarWithNonExistentCar() throws Exception {
        mockMvc.perform(post("/users/sell")
                        .with(csrf())
                        .param("carId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Car with id 999 not found")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testSellCarNotOwnedByUser() throws Exception {
        mockMvc.perform(post("/users/sell")
                        .with(csrf())
                        .param("carId", testCar.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("User has no car with id")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/users/info"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAccessUnauthenticated() throws Exception {
        mockMvc.perform(get("/users/myCars"))
                .andExpect(status().isForbidden());
    }
}