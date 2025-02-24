package com.example.cars.controllers;

import com.example.cars.model.CarDTO;
import com.example.cars.model.requests.CarRequest;
import com.example.cars.services.CarsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarsController {
    private final CarsService carsService;

    @GetMapping
    Page<CarDTO> getCars(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return carsService.getCars(page, pageSize);
    }

    @GetMapping("{id}")
    CarDTO getCarById(@PathVariable long id) {
        return carsService.findCar(id);
    }

    @PostMapping
    ResponseEntity<CarDTO> addCar(@RequestBody @Valid CarRequest request) {
        carsService.addCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{id}")
    void updateCar(@PathVariable long id, @RequestBody @Valid CarRequest request) {
        carsService.updateCar(id, request);
    }

    @DeleteMapping("{id}")
    void deleteCar(@PathVariable long id) {
        carsService.deleteCar(id);
    }

}
 