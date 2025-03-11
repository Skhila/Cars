package cars.unit;

import com.example.cars.error.NotFoundException;
import com.example.cars.images.service.StorageService;
import com.example.cars.model.CarDTO;
import com.example.cars.model.EngineDTO;
import com.example.cars.model.requests.CarRequest;
import com.example.cars.persistence.Car;
import com.example.cars.persistence.CarRepository;
import com.example.cars.persistence.Engine;
import com.example.cars.services.CarsService;
import com.example.cars.services.EngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarsServiceUnitTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private EngineService engineService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private CarsService carsService;

    @Captor
    private ArgumentCaptor<Car> carCaptor;

    private Car testCar;
    private Engine testEngine;
    private CarRequest validCarRequest;
    private CarDTO carDTO;

    @BeforeEach
    void setUp() {
        // Create test engine
        testEngine = new Engine();
        testEngine.setId(1L);
        testEngine.setHorsePower(300);
        testEngine.setCapacity(3.0);

        // Create test car
        testCar = new Car();
        testCar.setId(1L);
        testCar.setModel("TestModel");
        testCar.setYear(2023);
        testCar.setDriveable(true);
        testCar.setPriceInCents(8000L);
        testCar.setSalesCount(0L);
        testCar.setEngine(testEngine);
        testCar.setImageUrl("https://test-bucket.s3.example.com/images/test-image.jpg");

        // Create valid car request
        validCarRequest = new CarRequest("New Car", 2022, true, 10000L, 1L);

        // Create CarDTO
        EngineDTO engineDTO = new EngineDTO(testEngine.getId(), testEngine.getHorsePower(), testEngine.getCapacity());
        carDTO = new CarDTO(testCar.getId(), testCar.getModel(), testCar.getYear(), testCar.isDriveable(),
                testCar.getPriceInCents(), engineDTO, testCar.getImageUrl(), testCar.getSalesCount());
    }

    @Test
    void testGetCars() {
        // Given
        Page<CarDTO> expectedPage = new PageImpl<>(Arrays.asList(carDTO));
        when(carRepository.findCars(any(Pageable.class))).thenReturn(expectedPage);

        // When
        Page<CarDTO> result = carsService.getCars(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(carDTO.getId(), result.getContent().get(0).getId());
        assertEquals(carDTO.getModel(), result.getContent().get(0).getModel());

        verify(carRepository).findCars(any(Pageable.class));
    }

    @Test
    void testFindCar() {
        // Given
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));

        // When
        CarDTO result = carsService.findCar(testCar.getId());

        // Then
        assertNotNull(result);
        assertEquals(testCar.getId(), result.getId());
        assertEquals(testCar.getModel(), result.getModel());
        assertEquals(testCar.getYear(), result.getYear());
        assertEquals(testCar.isDriveable(), result.isDriveable());
        assertEquals(testCar.getPriceInCents(), result.getPriceInCents());
        assertEquals(testCar.getEngine().getId(), result.getEngine().getId());
        assertEquals(testCar.getImageUrl(), result.getImageUrl());

        verify(carRepository).findById(testCar.getId());
    }

    @Test
    void testFindCarNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(carRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> carsService.findCar(nonExistentId)
        );

        String expectedMessage = "Car with id " + nonExistentId + " not found";
        assertEquals(expectedMessage, exception.getMessage());

        verify(carRepository).findById(nonExistentId);
    }

    @Test
    void testAddCar() {
        // Given
        when(engineService.findEngineById(validCarRequest.getEngineId())).thenReturn(testEngine);
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car car = invocation.getArgument(0);
            car.setId(1L);
            return car;
        });

        // When
        CarDTO result = carsService.addCar(validCarRequest);

        // Then
        assertNotNull(result);
        assertEquals(validCarRequest.getModel(), result.getModel());
        assertEquals(validCarRequest.getYear(), result.getYear());
        assertEquals(validCarRequest.isDriveable(), result.isDriveable());
        assertEquals(validCarRequest.getPriceInCents(), result.getPriceInCents());
        assertEquals(testEngine.getId(), result.getEngine().getId());

        verify(engineService).findEngineById(validCarRequest.getEngineId());
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void testUpdateCar() {
        // Given
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        when(engineService.findEngineById(validCarRequest.getEngineId())).thenReturn(testEngine);
        when(carRepository.save(any(Car.class))).thenReturn(testCar);
        testCar.getEngine().setId(2L);

        // When
        CarDTO result = carsService.updateCar(testCar.getId(), validCarRequest);

        // Then
        assertNotNull(result);
        assertEquals(validCarRequest.getModel(), result.getModel());
        assertEquals(validCarRequest.getYear(), result.getYear());
        assertEquals(validCarRequest.isDriveable(), result.isDriveable());
        assertEquals(validCarRequest.getPriceInCents(), result.getPriceInCents());

        verify(carRepository).findById(testCar.getId());
        verify(engineService).findEngineById(validCarRequest.getEngineId());
        verify(carRepository).save(testCar);
    }

    @Test
    void testUpdateCarWithNewEngine() {
        // Given
        Engine newEngine = new Engine();
        newEngine.setId(2L);
        newEngine.setHorsePower(400);
        newEngine.setCapacity(4.0);

        CarRequest requestWithNewEngine = new CarRequest("Updated Model", 2022, true, 10000L, 2L);

        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        when(engineService.findEngineById(requestWithNewEngine.getEngineId())).thenReturn(newEngine);
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        carsService.updateCar(testCar.getId(), requestWithNewEngine);

        // Then
        verify(carRepository).findById(testCar.getId());
        verify(engineService).findEngineById(requestWithNewEngine.getEngineId());
        verify(carRepository).save(carCaptor.capture());

        Car capturedCar = carCaptor.getValue();
        assertEquals(requestWithNewEngine.getModel(), capturedCar.getModel());
        assertEquals(requestWithNewEngine.getYear(), capturedCar.getYear());
        assertEquals(requestWithNewEngine.isDriveable(), capturedCar.isDriveable());
        assertEquals(requestWithNewEngine.getPriceInCents(), capturedCar.getPriceInCents());
        assertEquals(newEngine, capturedCar.getEngine());
    }

    @Test
    void testUpdateCarImage() {
        // Given
        String oldImageUrl = testCar.getImageUrl();
        String newImageUrl = "https://test-bucket.s3.example.com/images/new-image.jpg";
        MultipartFile mockImage = new MockMultipartFile("image", "new-image.jpg", "image/jpeg", "test image data".getBytes());

        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        when(storageService.uploadImage(mockImage)).thenReturn(newImageUrl);
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        CarDTO result = carsService.updateCarImage(testCar.getId(), mockImage);

        // Then
        assertNotNull(result);
        assertEquals(newImageUrl, result.getImageUrl());

        verify(carRepository).findById(testCar.getId());
        verify(storageService).uploadImage(mockImage);
        verify(storageService).deleteImage(oldImageUrl);
        verify(carRepository).save(testCar);
    }

    @Test
    void testUpdateCarPriceInCents() {
        // Given
        Long newPrice = 15000L;
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        CarDTO result = carsService.updateCarPriceInCents(testCar.getId(), newPrice);

        // Then
        assertNotNull(result);
        assertEquals(newPrice, result.getPriceInCents());

        verify(carRepository).findById(testCar.getId());
        verify(carRepository).save(testCar);
    }

    @Test
    void testUpdateCarPriceInCentsWithSamePrice() {
        // Given
        Long samePrice = testCar.getPriceInCents();
        when(carRepository.findById(testCar.getId())).thenReturn(Optional.of(testCar));
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        CarDTO result = carsService.updateCarPriceInCents(testCar.getId(), samePrice);

        // Then
        assertNotNull(result);
        assertEquals(samePrice, result.getPriceInCents());

        verify(carRepository).findById(testCar.getId());
        verify(carRepository).save(testCar);
    }

    @Test
    void testDeleteCar() {
        // Given
        doNothing().when(carRepository).deleteById(testCar.getId());

        // When
        carsService.deleteCar(testCar.getId());

        // Then
        verify(carRepository).deleteById(testCar.getId());
    }
}