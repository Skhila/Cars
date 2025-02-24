package com.example.cars.controllers;

import com.example.cars.model.EngineDTO;
import com.example.cars.model.requests.EngineRequest;
import com.example.cars.services.EngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/engines")
@RequiredArgsConstructor
public class EngineController {
    private final EngineService engineService;

    @GetMapping
    Page<EngineDTO> getEngines(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false, defaultValue = "0.0") double capacity) {
        return engineService.getEngines(page, pageSize, capacity);
    }

    @PostMapping
    ResponseEntity<Void> createEngine(@RequestBody @Valid EngineRequest request) {
        engineService.createEngine(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{id}")
    EngineDTO updateEngine(@PathVariable Long id, @RequestBody @Valid EngineRequest request) {
        return engineService.updateEngine(id, request);
    }

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteEngine(@PathVariable Long id) {
        engineService.deleteEngine(id);
        return ResponseEntity.noContent().build();
    }
}
