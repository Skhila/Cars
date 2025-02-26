package com.example.cars.controllers;

import com.example.cars.model.EngineDTO;
import com.example.cars.model.requests.EngineRequest;
import com.example.cars.services.EngineService;
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
@RequestMapping("/engines")
@RequiredArgsConstructor
public class EngineController {
    private final EngineService engineService;

    @GetMapping
    @PreAuthorize(USER_OR_ADMIN)
    Page<EngineDTO> getEngines(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false, defaultValue = "0.0") double capacity) {
        return engineService.getEngines(page, pageSize, capacity);
    }

    @PostMapping
    @PreAuthorize(ADMIN)
    ResponseEntity<Void> createEngine(@RequestBody @Valid EngineRequest request) {
        engineService.createEngine(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{id}")
    @PreAuthorize(ADMIN)
    EngineDTO updateEngine(@PathVariable Long id, @RequestBody @Valid EngineRequest request) {
        return engineService.updateEngine(id, request);
    }

    @DeleteMapping("{id}")
    @PreAuthorize(ADMIN)
    ResponseEntity<Void> deleteEngine(@PathVariable Long id) {
        engineService.deleteEngine(id);
        return ResponseEntity.noContent().build();
    }
}
