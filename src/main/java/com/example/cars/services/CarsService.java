package com.example.cars.services;

import com.example.cars.error.ErrorDTO;
import com.example.cars.error.NotFoundException;
import com.example.cars.images.service.StorageService;
import com.example.cars.model.CarDTO;
import com.example.cars.model.EngineDTO;
import com.example.cars.model.requests.CarRequest;
import com.example.cars.persistence.Car;
import com.example.cars.persistence.CarRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarsService {

    private final CarRepository carRepository;
    private final EngineService engineService;
    private final StorageService storageService;

    public Page<CarDTO> getCars(int page, int pageSize) {
        return carRepository.findCars(PageRequest.of(page, pageSize));
    }

    public ResponseEntity<?> addCarWithImage(String carRequestJson, MultipartFile image) {
        CarRequest request;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            request = objectMapper.readValue(carRequestJson, CarRequest.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorDTO("Incorrect JSON body provided", e.getMessage()));
        }

        // Manual validation
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CarRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            List<ErrorDTO> errors = new ArrayList<>();
            for (ConstraintViolation<CarRequest> violation : violations) {
                errors.add(new ErrorDTO(violation.getPropertyPath().toString(), violation.getMessage()));
            }
            return ResponseEntity.badRequest().body(errors);
        }

        Car car = new Car();
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setDriveable(request.isDriveable());
        car.setPriceInCents(request.getPriceInCents());
        car.setEngine(engineService.findEngineById(request.getEngineId()));

        if (image != null && !image.isEmpty()) {
            String imageUrl = storageService.uploadImage(image);
            car.setImageUrl(imageUrl);
        }

        carRepository.save(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapCar(car));
    }

    public CarDTO addCar(CarRequest request) {
        Car car = new Car();
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setDriveable(request.isDriveable());
        car.setPriceInCents(request.getPriceInCents());
        car.setEngine(engineService.findEngineById(request.getEngineId()));

        carRepository.save(car);
        return mapCar(car);
    }

    public CarDTO updateCar(Long id, CarRequest request) {
        Car car = carRepository.findById(id).orElseThrow(() -> buildNotFoundException(id));
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setDriveable(request.isDriveable());
        car.setPriceInCents(request.getPriceInCents());

        if (car.getEngine().getId() != request.getEngineId()) {
            car.setEngine(engineService.findEngineById(request.getEngineId()));
        }

        carRepository.save(car);
        return mapCar(car);
    }

    public CarDTO updateCarImage(Long id, MultipartFile image) {
        Car car = carRepository.findById(id).orElseThrow(() -> buildNotFoundException(id));
        String oldImageUrl = car.getImageUrl();

        car.setImageUrl(storageService.uploadImage(image));

        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            storageService.deleteImage(oldImageUrl);
        }

        carRepository.save(car);
        return mapCar(car);
    }

    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    public CarDTO findCar(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> buildNotFoundException(id));

        return mapCar(car);
    }

    private CarDTO mapCar(Car car) {
        return
                new CarDTO(car.getId(), car.getModel(), car.getYear(), car.isDriveable(), car.getPriceInCents(),
                        new EngineDTO(
                                car.getEngine().getId(),
                                car.getEngine().getHorsePower(),
                                car.getEngine().getCapacity()
                        ),
                        car.getImageUrl(),
                        car.getSalesCount());
    }

    private NotFoundException buildNotFoundException(Long id) {
        return new NotFoundException("Car with id " + id + " not found");
    }
}
