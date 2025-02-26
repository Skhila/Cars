package com.example.cars.controllers;

import com.example.cars.model.CarDTO;
import com.example.cars.model.requests.CarRequest;
import com.example.cars.services.CarsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    CarDTO getCarById(@PathVariable long id) {
        return carsService.findCar(id);
    }

    @PostMapping
    @PreAuthorize(ADMIN)
    ResponseEntity<CarDTO> addCar(@RequestBody @Valid CarRequest request) {
        carsService.addCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{id}")
    @PreAuthorize(ADMIN)
    void updateCar(@PathVariable long id, @RequestBody @Valid CarRequest request) {
        carsService.updateCar(id, request);
    }

    @DeleteMapping("{id}")
    @PreAuthorize(ADMIN)
    void deleteCar(@PathVariable long id) {
        carsService.deleteCar(id);
    }
}
 