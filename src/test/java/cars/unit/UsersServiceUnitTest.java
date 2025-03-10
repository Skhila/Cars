package cars.unit;

import com.example.cars.error.InsufficientFundsException;
import com.example.cars.error.NotFoundException;
import com.example.cars.persistence.Car;
import com.example.cars.persistence.CarRepository;
import com.example.cars.persistence.Engine;
import com.example.cars.user.persistence.AppUser;
import com.example.cars.user.persistence.AppUserRepository;
import com.example.cars.user.service.RoleService;
import com.example.cars.user.service.UserService;
import com.example.cars.user.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceUnitTest {

    @Mock
    private UserUtils userUtils;

    @Mock
    private CarRepository carRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private AppUser testUser;
    private Car testCar;
    private Engine testEngine;
    private static final double DEPRECIATION_FACTOR = 0.8;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setBalanceInCents(10000L);
        testUser.setCars(new HashSet<>());

        // Create test engine
        testEngine = new Engine();
        testEngine.setId(1L);
        testEngine.setHorsePower(200);
        testEngine.setCapacity(2.0);

        // Create test car
        testCar = new Car();
        testCar.setId(1L);
        testCar.setModel("TestModel");
        testCar.setYear(2023);
        testCar.setDriveable(true);
        testCar.setPriceInCents(5000L);
        testCar.setSalesCount(0L);
        testCar.setEngine(testEngine);
    }

    @Test
    void testPurchaseCarWithEnoughMoney() {
        // Given
        when(userUtils.getCurrentUser()).thenReturn(testUser);
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        long initialUserBalance = testUser.getBalanceInCents();
        long carPrice = testCar.getPriceInCents();

        // When
        userService.purchaseCar(testCar.getId());

        // Then
        assertTrue(testUser.getCars().contains(testCar), "Purchased car should be added to user's cars");
        assertEquals(initialUserBalance - carPrice, testUser.getBalanceInCents(),
                "Car price should be reduced from the user's balance");
        assertEquals(1L, testCar.getSalesCount(), "Sales count should be incremented");
        verify(appUserRepository).save(testUser);
        verify(carRepository).save(testCar);
    }

    @Test
    void testPurchaseCarWithoutEnoughMoney() {
        // Given
        testUser.setBalanceInCents(2000L);
        when(userUtils.getCurrentUser()).thenReturn(testUser);
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        long initialBalance = testUser.getBalanceInCents();

        // When & Part of Then
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> userService.purchaseCar(testCar.getId()),
                "InsufficientFundsException should be thrown when user's balance is too low"
        );

        // Verify exception message information
        String expectedMessage = "Insufficient funds to purchase the car";
        assertTrue(exception.getMessage().contains(expectedMessage), "Exception message should start with: " + expectedMessage);
        assertTrue(exception.getMessage().contains(String.valueOf(testCar.getPriceInCents())),
                "Exception message should contain car price");
        assertTrue(exception.getMessage().contains(String.valueOf(testUser.getBalanceInCents())),
                "Exception message should contain user's balance");

        // Verify user info
        assertEquals(0, testUser.getCars().size(), "User's cars should be empty");
        assertEquals(initialBalance, testUser.getBalanceInCents(), "User's balance should not be changed");
        assertEquals(0L, testCar.getSalesCount(), "Car sales count should not be changed");

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void testPurchaseAlreadyOwnedCar() {
        // Given
        testUser.getCars().add(testCar);
        when(userUtils.getCurrentUser()).thenReturn(testUser);
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        long initialBalance = testUser.getBalanceInCents();
        long initialSalesCount = testCar.getSalesCount();

        // When
        userService.purchaseCar(testCar.getId());

        // Then
        assertEquals(1, testUser.getCars().size(), "User's car collection size should remain the same");
        assertEquals(initialBalance, testUser.getBalanceInCents(), "User's balance should remain unchanged");
        assertEquals(initialSalesCount, testCar.getSalesCount(), "Car's sales count should remain unchanged");

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void testPurchaseNonExistentCar() {
        // Given
        long nonExistentCarId = 123123123L;

        when(userUtils.getCurrentUser()).thenReturn(testUser);
        when(carRepository.findById(nonExistentCarId)).thenReturn(Optional.empty());
        long initialUserBalance = testUser.getBalanceInCents();

        // When & part of Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.purchaseCar(nonExistentCarId),
                "NotFoundException should be thrown when car not found"
        );

        // Verify exception message information
        String expectedMessage = "Car with id " + nonExistentCarId + " not found";
        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should contain: " + expectedMessage);
        assertEquals(0, testUser.getCars().size(), "User's cars should be empty");
        assertEquals(initialUserBalance, testUser.getBalanceInCents(), "User's balance should not be changed");

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void testSellCarSuccessfully() {
        // Given
        testUser.getCars().add(testCar);
        when(userUtils.getCurrentUser()).thenReturn(testUser);
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));

        long initialBalance = testUser.getBalanceInCents();
        long expectedCarSalePrice = Math.round(testCar.getPriceInCents() * DEPRECIATION_FACTOR);

        // When
        userService.sellCar(testCar.getId());

        // Then
        assertFalse(testUser.getCars().contains(testCar), "Car should be removed from user's cars");
        assertEquals(initialBalance + expectedCarSalePrice, testUser.getBalanceInCents(),
                "Balance should be increased by depreciated car price");

        verify(appUserRepository).save(testUser);
    }

    @Test
    void testSellNotOwnedCar() {
        // Given
        when(userUtils.getCurrentUser()).thenReturn(testUser);
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        long initialBalance = testUser.getBalanceInCents();

        // When & part of Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.sellCar(testCar.getId()),
                "Should throw NotFoundException when user doesn't own the car"
        );

        // Verify exception message information
        String expectedMessage = "User has no car with id " + testCar.getId();
        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should be equal to this message: " + expectedMessage);
        assertEquals(0, testUser.getCars().size(), "User's cars should remain empty");
        assertEquals(initialBalance, testUser.getBalanceInCents(), "User's balance should not be changed");

        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void testSellNonExistentCar() {
        // Given
        long nonExistentCarId = 123123123L;
        when(userUtils.getCurrentUser()).thenReturn(testUser);
        when(carRepository.findById(nonExistentCarId)).thenReturn(Optional.empty());
        long initialUserBalance = testUser.getBalanceInCents();

        // When & part of Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.sellCar(nonExistentCarId),
                "Should throw NotFoundException when car doesn't exist"
        );

        // Verify exception message information
        String expectedMessage = "Car with id " + nonExistentCarId + " not found";
        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should exactly match the expected format");
        assertEquals(0, testUser.getCars().size(), "User's car collection should remain empty");
        assertEquals(initialUserBalance, testUser.getBalanceInCents(), "User's balance should remain unchanged");

        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void testUpdateBalanceWithPositiveAmount() {
        // Given
        when(appUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        long initialUserBalance = testUser.getBalanceInCents();
        long amountToAdd = 7000L;

        // When
        userService.updateBalance(testUser.getId(), amountToAdd);

        // Then
        assertEquals(initialUserBalance + amountToAdd, testUser.getBalanceInCents(),
                "Balance should be increased by the amount");

        verify(appUserRepository).save(testUser);
    }

    @Test
    void testUpdateBalanceWithNegativeAmountWithSufficientFunds() {
        // Given
        when(appUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        long initialBalance = testUser.getBalanceInCents();
        long amountToSubtract = -3000L;

        // When
        userService.updateBalance(testUser.getId(), amountToSubtract);

        // Then
        assertEquals(initialBalance + amountToSubtract, testUser.getBalanceInCents(),
                "Balance should be decreased by the amount");
        verify(appUserRepository).save(testUser);
    }

    @Test
    void testUpdateBalanceWithNegativeAmountWithInsufficientFunds() {
        // Given
        when(appUserRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        long initialBalance = testUser.getBalanceInCents();
        long amountToSubtract = -100000L;

        // When & part of Then
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> userService.updateBalance(testUser.getId(), amountToSubtract),
                "Should throw InsufficientFundsException when trying to subtract more than available balance"
        );


        // Verify exception message information
        String expectedMessage = "Insufficient funds to decrease the balance";
        assertTrue(exception.getMessage().contains(expectedMessage),
                "Exception message should contain: " + expectedMessage);
        assertTrue(exception.getMessage().contains(String.valueOf(Math.abs(amountToSubtract))),
                "Exception message should contain the requested amount: " + amountToSubtract);
        assertTrue(exception.getMessage().contains(String.valueOf(initialBalance)),
                "Exception message should contain the user's balance: " + initialBalance);
        assertEquals(initialBalance, testUser.getBalanceInCents(), "User's balance should not be changed");

        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void updateBalanceWithNonExistentUser() {
        // Given
        long nonExistentUserId = 123123L;
        when(appUserRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        long amountToAdd = 5000L;

        // When & Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateBalance(nonExistentUserId, amountToAdd),
                "Should throw NotFoundException when user doesn't exist"
        );

        // Verify exception message information
        String expectedMessage = "User with id '" + nonExistentUserId + "' not found";
        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should contain: " + expectedMessage);
        verify(appUserRepository, never()).save(any(AppUser.class));
    }
}
