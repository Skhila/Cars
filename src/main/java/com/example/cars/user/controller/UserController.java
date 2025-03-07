package com.example.cars.user.controller;

import com.example.cars.model.CarDTO;
import com.example.cars.user.model.AppUserDTO;
import com.example.cars.user.model.UserRequest;
import com.example.cars.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.example.cars.security.AuthorizationConstants.ADMIN;
import static com.example.cars.security.AuthorizationConstants.USER;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize(ADMIN)
    @PostMapping
    public void createUser(@RequestBody @Valid UserRequest userRequest) {
        userService.createUser(userRequest);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/{userId}/updateBalance")
    public void updateBalance(@PathVariable Long userId, @RequestParam Long amountInCents) {
        userService.updateBalance(userId, amountInCents);
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/{userId}/cars")
    Page<CarDTO> getUsers(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return userService.getUserCars(page, pageSize, userId);
    }

    @PreAuthorize(USER)
    @PostMapping("/purchase")
    public void purchaseCar(@RequestParam Long carId) {
        userService.purchaseCar(carId);
    }

    @PreAuthorize(USER)
    @PostMapping("/sell")
    public void sellCar(@RequestParam Long carId) {
        userService.sellCar(carId);
    }
}
