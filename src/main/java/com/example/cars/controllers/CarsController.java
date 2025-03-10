package com.example.cars.controllers;

import com.example.cars.model.CarDTO;
import com.example.cars.model.requests.CarRequest;
import com.example.cars.services.CarsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.cars.security.AuthorizationConstants.ADMIN;
import static com.example.cars.security.AuthorizationConstants.USER_OR_ADMIN;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarsController {
    private final CarsService carsService;

    @GetMapping
    @PreAuthorize(USER_OR_ADMIN)
    Page<CarDTO> getCars(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return carsService.getCars(page, pageSize);
    }

    @GetMapping("{id}")
    @PreAuthorize(USER_OR_ADMIN)
    CarDTO getCarById(@PathVariable Long id) {
        return carsService.findCar(id);
    }

    @PostMapping("/add")
    @PreAuthorize(ADMIN)
    ResponseEntity<CarDTO> addCar(@RequestBody @Valid CarRequest request) {
        CarDTO addedCar = carsService.addCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedCar);
    }

    @PostMapping("/add/withImage")
    @PreAuthorize(ADMIN)
    ResponseEntity<?> addCarWithImage(@RequestPart(value = "image") MultipartFile image,
                                      @RequestParam("request") String carRequestJson) throws JsonProcessingException {
        return carsService.addCarWithImage(carRequestJson, image);
    }

    @PutMapping("{id}")
    @PreAuthorize(ADMIN)
    CarDTO updateCar(@PathVariable Long id, @RequestBody @Valid CarRequest request) {
        return carsService.updateCar(id, request);
    }

    @PatchMapping("/{id}/updatePriceInCents")
    @PreAuthorize(ADMIN)
    CarDTO addCar(@PathVariable Long id, @RequestParam(name = "newPrice") Long priceInCents) {
        return carsService.updateCarPriceInCents(id, priceInCents);
    }

    @PatchMapping("/{id}/updateImage")
    @PreAuthorize(ADMIN)
    CarDTO updateCarImage(@PathVariable Long id, @RequestParam(name = "image") MultipartFile image) {
        return carsService.updateCarImage(id, image);
    }

    @DeleteMapping("{id}")
    @PreAuthorize(ADMIN)
    void deleteCar(@PathVariable Long id) {
        carsService.deleteCar(id);
    }
}
 